package com.studyon.studyon.repository;

import com.studyon.studyon.domain.Reservation;
import com.studyon.studyon.domain.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("""
            SELECT r.startAt AS startAt, r.endAt AS endAt
            FROM Reservation r
            WHERE r.studyRoom.id = :studyRoomId
              AND r.status = :status
              AND r.startAt < :dayEnd
              AND r.endAt > :dayStart
            """)
    List<ReservedTime> findOverlapping(
            @Param("studyRoomId") Long studyRoomId,
            @Param("status") ReservationStatus status,
            @Param("dayStart") LocalDateTime dayStart,
            @Param("dayEnd") LocalDateTime dayEnd
    );

    interface ReservedTime {

        LocalDateTime getStartAt();

        LocalDateTime getEndAt();
    }
}
