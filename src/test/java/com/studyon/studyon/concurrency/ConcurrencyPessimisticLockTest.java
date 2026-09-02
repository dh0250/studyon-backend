package com.studyon.studyon.concurrency;

import com.studyon.studyon.common.exception.ReservationConflictException;
import com.studyon.studyon.domain.StudyRoom;
import com.studyon.studyon.dto.ReservationCreateRequest;
import com.studyon.studyon.repository.ReservationRepository;
import com.studyon.studyon.repository.StudyRoomRepository;
import com.studyon.studyon.service.ReservationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class ConcurrencyPessimisticLockTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private StudyRoomRepository studyRoomRepository;

    @Autowired
    private ReservationService reservationService;

    @BeforeEach
    void setUp() {
        reservationRepository.deleteAllInBatch();
    }

    @AfterEach
    void tearDown() {
        reservationRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("비관적 락으로 같은 시간대 예약의 중복 저장을 방지한다")
    void createReservationWithPessimisticLock() throws Exception {
        StudyRoom studyRoom = studyRoomRepository.findAllByActiveTrue().getFirst();
        Long studyRoomId = studyRoom.getId();
        LocalDateTime startAt = LocalDate.now().plusDays(1).atTime(10, 0);

        ReservationCreateRequest firstRequest = new ReservationCreateRequest(
                studyRoomId,
                "신짱구",
                "zzang@naver.com",
                "01011112222",
                startAt,
                startAt.plusHours(1),
                "중복 저장 테스트 요청 1"
        );

        ReservationCreateRequest secondRequest = new ReservationCreateRequest(
                studyRoomId,
                "봉미선",
                "bong@gmail.com",
                "01033332222",
                startAt,
                startAt.plusHours(1),
                "중복 저장 테스트 요청 2"
        );

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        CountDownLatch readyLatch = new CountDownLatch(2);
        CountDownLatch startLatch = new CountDownLatch(1);

        try {
            Future<?> firstFuture = executorService.submit(() -> {
                readyLatch.countDown();
                await(startLatch);

                reservationService.createReservation(firstRequest);
            });

            Future<?> secondFuture = executorService.submit(() -> {
                readyLatch.countDown();
                await(startLatch);

                reservationService.createReservation(secondRequest);
            });

            assertThat(readyLatch.await(5, TimeUnit.SECONDS)).isTrue();
            startLatch.countDown();

            int successCount = 0;
            int conflictCount = 0;

            for (Future<?> future : List.of(firstFuture, secondFuture)) {
                try {
                    future.get();
                    successCount++;
                } catch (ExecutionException e) {
                    assertThat(e.getCause()).isInstanceOf(ReservationConflictException.class);
                    conflictCount++;
                }
            }

            assertThat(successCount).isEqualTo(1);
            assertThat(conflictCount).isEqualTo(1);
            assertThat(reservationRepository.count()).isEqualTo(1);
        } finally {
            startLatch.countDown();
            executorService.shutdown();
        }
    }

    private static void await(CountDownLatch startLatch) {
        try {
            startLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

}