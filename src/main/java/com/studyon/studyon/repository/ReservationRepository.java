package com.studyon.studyon.repository;

import com.studyon.studyon.domain.Reservation;
import com.studyon.studyon.domain.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<ReservedTime> findByStudyRoomIdAndStatusAndStartAtLessThanAndEndAtGreaterThan(
            Long studyRoomId,
            ReservationStatus status,
            LocalDateTime dayEnd,
            LocalDateTime dayStart
    );

    boolean existsByStudyRoomIdAndStatusAndStartAtLessThanAndEndAtGreaterThan(
            Long studyRoomId,
            ReservationStatus status,
            LocalDateTime requestedEndAt,
            LocalDateTime requestedStartAt
    );

    List<Reservation> findByGuestEmailAndGuestPhoneOrderByCreatedAtDesc(String normalizedEmail, String normalizedPhone);

    interface ReservedTime {
        LocalDateTime getStartAt();
        LocalDateTime getEndAt();
    }

}
