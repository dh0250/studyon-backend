package com.studyon.studyon.dto;

import com.studyon.studyon.domain.ReservationStatus;

import java.time.LocalDateTime;

public record ReservationCancelResponse(
        Long reservationId,
        ReservationStatus status,
        LocalDateTime canceledAt
) {

    public static ReservationCancelResponse from(Long reservationId, ReservationStatus status, LocalDateTime canceledAt) {
        return new ReservationCancelResponse(reservationId, status, canceledAt);
    }
}
