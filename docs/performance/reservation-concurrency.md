# 예약 동시성 제어와 검증 전략

## 문제

비회원 예약은 여러 사용자가 같은 스터디룸의 같은 시간대를 동시에 선택할 수 있다. 단순히 예약 가능 여부를 조회한 뒤 저장하면, 두 요청이 모두 중복 검사를 통과해 중복 예약이 만들어지는 경쟁 상태가 발생한다.

이를 재현하고, 애플리케이션 락과 데이터베이스 제약을 함께 적용해 중복 예약을 방지했다.

## 테스트 구성

| 구분 | 도구·범위 | 검증 내용 |
| --- | --- | --- |
| 단위 테스트 | JUnit 5, Mockito | 예약 생성·취소·조회와 예약 가능 시간 계산의 서비스 로직 |
| API 통합 테스트 | Spring Boot, MockMvc, PostgreSQL 테스트 DB | HTTP 요청부터 컨트롤러·서비스·리포지토리까지의 응답과 예외 처리 |
| 동시성 통합 테스트 | JUnit 5, ExecutorService, CountDownLatch | 같은 스터디룸·시간대에 동시에 요청했을 때 중복 저장 방지 여부 |
| 부하 테스트 | k6 | 실제 HTTP 요청 100건을 동시에 전송한 결과와 응답 시간 |

관련 코드: [서비스 단위 테스트](../../src/test/java/com/studyon/studyon/service), [API 통합 테스트](../../src/test/java/com/studyon/studyon/integration), [동시성 테스트](../../src/test/java/com/studyon/studyon/concurrency), [k6 스크립트](../../tests/k6/reservation-lock.js)

## JUnit 5 동시성 테스트

실제 PostgreSQL 테스트 DB를 사용했다. `ExecutorService`로 요청 스레드를 만들고, `CountDownLatch`로 두 요청의 시작 시점을 맞췄다.

### 1. 락 미적용: 중복 예약 재현

`ConcurrencyNoLockTest`는 두 요청이 모두 중복 검사까지 통과한 뒤 저장되도록 제어한다. 그 결과 같은 스터디룸·시간대의 예약이 2건 저장되는 것을 확인했다.

### 2. 비관적 락

`PESSIMISTIC_WRITE`로 스터디룸 행을 먼저 잠근다. 먼저 락을 획득한 요청만 예약을 생성하고, 다음 요청은 락 해제 후 중복 예약을 확인해 `ReservationConflictException`을 받는다.

- 성공: 1건
- 충돌: 1건
- DB 저장: 1건

### 3. 낙관적 락

스터디룸에 `@Version`을 두고 `OPTIMISTIC_FORCE_INCREMENT`를 적용했다. 동시에 읽은 두 요청 중 먼저 커밋한 요청만 성공하며, 나머지 요청은 버전 충돌로 `ObjectOptimisticLockingFailureException`이 발생한다.

- 성공: 1건
- 충돌: 1건
- DB 저장: 1건

애플리케이션 락과 별개로 PostgreSQL의 `EXCLUDE USING gist` 제약을 추가해, `CONFIRMED` 상태의 겹치는 예약이 DB에 저장되는 것도 최종적으로 차단했다.

## k6 동시 부하 테스트

같은 스터디룸(ID 1)과 같은 시간대(다음 날 10:00~11:00)에 100개의 HTTP 예약 요청을 동시에 전송했다. 각 요청은 서로 다른 예약자 정보를 사용하며, `201 Created`와 `409 Conflict`만 정상 응답으로 판단했다.

| 항목 | 조건 |
| --- | --- |
| 실행 방식 | `shared-iterations` |
| 가상 사용자(VU) | 100명 |
| 총 요청 수 | 100건 |
| 기대 결과 | 성공 1건, 충돌 99건, 예상 밖 오류 0건 |
| 반복 횟수 | 락 방식별 3회 |

### 결과 요약

각 방식 모두 성공 1건, 충돌 99건, 예상 밖 오류 0건으로 데이터 정합성을 지켰다. 초기 JVM·DB 상태의 영향을 줄이기 위해 3회 실행 결과 중 중앙값을 비교했다.

| 방식 | 평균 응답 시간 | 중앙값 | p95 | 처리량 |
| --- | ---: | ---: | ---: | ---: |
| 비관적 락 | 40.00 ms | 41.00 ms | 59.53 ms | 1,503 req/s |
| 낙관적 락 | 22.54 ms | 22.93 ms | 30.59 ms | 2,685 req/s |

이번 로컬 환경의 높은 경합 시나리오에서는 낙관적 락이 비관적 락보다 평균 응답 시간이 약 43.6% 낮고, p95가 약 48.6% 낮았다. 반면 낙관적 락은 충돌 요청에 대한 재시도 정책이 필요할 수 있으므로, 단순히 수치만으로 운영 방식을 결정하지 않고 트래픽 특성과 충돌 빈도를 함께 고려해야 한다.

원본 실행 수치와 3회 결과는 [성능 비교 문서](reservation-lock-comparison.md) 및 `results/`의 JSON 파일에 보관했다.

### 배포 환경 검증

로컬 Mac에서 `https://studyon.duckdns.org`로 요청을 전송해, 인터넷·Nginx·EC2 Spring Boot 컨테이너·RDS PostgreSQL을 포함한 실제 배포 경로를 검증했다. 운영 코드인 비관적 락을 대상으로 1회 실행했다.

| 항목 | 결과 |
| --- | ---: |
| 가상 사용자(VU) / 총 요청 | 100 / 100건 |
| 예약 성공(201) | 1건 |
| 중복 예약 차단(409) | 99건 |
| 예상 밖 오류 / HTTP 실패 | 0건 / 0% |
| 평균 / 중앙값 / p95 응답 시간 | 669.16ms / 666.08ms / 957.85ms |
| 최대 응답 시간 | 1.02s |

배포 환경에서도 같은 시간대에 예약이 1건만 생성되어 정합성 조건을 만족했다. 이 수치는 비관적 락 단독 1회 측정이며, 로컬의 낙관적 락과 절대 성능을 비교하는 용도는 아니다.

## 재현 방법

테스트 DB에서 기존 예약을 비운 뒤 애플리케이션을 `test` 프로필로 실행한다.

```bash
SPRING_PROFILES_ACTIVE=test ./gradlew bootRun
```

다른 터미널에서 k6를 실행한다.

```bash
k6 run tests/k6/reservation-lock.js
```

실행 결과를 JSON으로 남기려면 다음과 같이 실행한다.

```bash
k6 run --summary-export docs/performance/results/lock-100vu.json tests/k6/reservation-lock.js
```

## 한계와 다음 단계

락 방식 간 상대 비교는 로컬 단일 서버·단일 PostgreSQL 환경에서 얻었다. 비관적 락은 AWS EC2·RDS 배포 환경에서도 별도로 검증했지만, 낙관적 락은 배포하지 않아 두 방식의 배포 환경 절대 성능 비교는 포함하지 않는다. 여러 애플리케이션 인스턴스와 낙관적 락 재시도 정책은 이후 검증 대상이다.
