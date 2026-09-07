package com.studyon.studyon.concurrency;

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
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class ConcurrencyOptimisticLockTest {

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
    @DisplayName("낙관적 락을 사용해서 같은 시간대, 같은 스터디룸  예약 요청 중복 저장을 방지한다.")
    void createReservationWithOptimisticLock() throws Exception {
        StudyRoom studyRoom = studyRoomRepository.findAllByActiveTrue().getFirst();
        Long studyRoomId = studyRoom.getId();
        LocalDateTime startAt = LocalDate.now().plusDays(1).atTime(10, 0);

        ReservationCreateRequest firstRequest = new ReservationCreateRequest(
                studyRoomId,
                "루피",
                "lp@naver.com",
                "01088887777",
                startAt,
                startAt.plusHours(1),
                "낙관적 락 동시성 테스트"
        );

        ReservationCreateRequest secondRequest = new ReservationCreateRequest(
                studyRoomId,
                "나미",
                "nm@naver.com",
                "01055552222",
                startAt,
                startAt.plusHours(1),
                "낙관적 락 동시성 테스트"
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
                    assertThat(e.getCause()).isInstanceOf(ObjectOptimisticLockingFailureException.class);
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

    private void await(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }
}
