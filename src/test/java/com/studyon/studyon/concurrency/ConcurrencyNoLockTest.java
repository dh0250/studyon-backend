package com.studyon.studyon.concurrency;

import com.studyon.studyon.common.exception.ReservationConflictException;
import com.studyon.studyon.common.exception.StudyRoomNotFoundException;
import com.studyon.studyon.domain.Reservation;
import com.studyon.studyon.domain.ReservationStatus;
import com.studyon.studyon.domain.StudyRoom;
import com.studyon.studyon.dto.ReservationCreateRequest;
import com.studyon.studyon.repository.ReservationRepository;
import com.studyon.studyon.repository.StudyRoomRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class ConcurrencyNoLockTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private StudyRoomRepository studyRoomRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @BeforeEach
    void setUp() {
        reservationRepository.deleteAllInBatch();
    }

    @AfterEach
    void tearDown() {
        reservationRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("락없이 같은 스터디룸, 같은 시간대 예약 요청 중복 저장 테스트")
    void createsReservationsWithoutLock() throws Exception {
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
        CountDownLatch checkedLatch = new CountDownLatch(2);
        CountDownLatch allowSaveLatch = new CountDownLatch(1);

        try {
            Future<?> firstFuture = executorService.submit(() -> {
                createReservationWithoutLock(
                        firstRequest,
                        checkedLatch,
                        allowSaveLatch
                );
            });

            Future<?> secondFuture = executorService.submit(() -> {
                createReservationWithoutLock(
                        secondRequest,
                        checkedLatch,
                        allowSaveLatch
                );
            });

            assertThat(checkedLatch.await(5, TimeUnit.SECONDS)).isTrue();
            allowSaveLatch.countDown();

            firstFuture.get();
            secondFuture.get();

            assertThat(reservationRepository.count()).isEqualTo(2);
        } finally {
            allowSaveLatch.countDown();
            executorService.shutdown();
        }
    }

    private void createReservationWithoutLock(ReservationCreateRequest request, CountDownLatch checkedLatch, CountDownLatch allowSaveLatch) {
        transactionTemplate.executeWithoutResult(status -> {
            StudyRoom studyRoom = studyRoomRepository.findByIdAndActiveTrue(request.studyRoomId())
                    .orElseThrow(StudyRoomNotFoundException::new);

            boolean overlapping = reservationRepository.existsByStudyRoomIdAndStatusAndStartAtLessThanAndEndAtGreaterThan(
                    request.studyRoomId(),
                    ReservationStatus.CONFIRMED,
                    request.endAt(),
                    request.startAt()
            );

            if (overlapping) {
                throw new ReservationConflictException("이미 예약된 시간대입니다.");
            }

            checkedLatch.countDown();

            await(allowSaveLatch);

            Reservation reservation = Reservation.create(
                    studyRoom,
                    request.guestName(),
                    request.guestEmail(),
                    request.guestPhone(),
                    request.startAt(),
                    request.endAt(),
                    request.purpose()
            );

            reservationRepository.save(reservation);
        });
    }

    private static void await(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }
}
