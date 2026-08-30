package com.studyon.studyon.dto;

import com.studyon.studyon.domain.Reservation;
import com.studyon.studyon.domain.ReservationStatus;

import java.time.LocalDateTime;

public record ReservationCreateResponse(
        Long reservationId,
        Long studyRoomId,
        String studyRoomName,
        String guestName,
        String guestEmail,
        String guestPhone,
        LocalDateTime startAt,
        LocalDateTime endAt,
        String purpose,
        ReservationStatus status
) {

    public static ReservationCreateResponse from(Reservation reservation) {
        return new ReservationCreateResponse(
                reservation.getId(),
                reservation.getStudyRoom().getId(),
                reservation.getStudyRoom().getName(),
                reservation.getGuestName(),
                reservation.getGuestEmail(),
                reservation.getGuestPhone(),
                reservation.getStartAt(),
                reservation.getEndAt(),
                reservation.getPurpose(),
                reservation.getStatus()
        );
    }
}
