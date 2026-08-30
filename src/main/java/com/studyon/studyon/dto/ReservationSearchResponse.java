package com.studyon.studyon.dto;

import com.studyon.studyon.domain.Reservation;
import com.studyon.studyon.domain.ReservationStatus;

import java.time.LocalDateTime;

public record ReservationSearchResponse(
        Long reservationId,
        Long studyRoomId,
        String studyRoomName,
        LocalDateTime startAt,
        LocalDateTime endAt,
        String purpose,
        ReservationStatus status,
        LocalDateTime canceledAt,
        LocalDateTime createdAt
) {
    public static ReservationSearchResponse from(Reservation reservation) {
        return new ReservationSearchResponse(
                reservation.getId(),
                reservation.getStudyRoom().getId(),
                reservation.getStudyRoom().getName(),
                reservation.getStartAt(),
                reservation.getEndAt(),
                reservation.getPurpose(),
                reservation.getStatus(),
                reservation.getCanceledAt(),
                reservation.getCreatedAt()
        );
    }
}
