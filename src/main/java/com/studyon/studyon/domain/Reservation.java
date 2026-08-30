package com.studyon.studyon.domain;
import com.studyon.studyon.common.exception.ReservationConflictException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "reservations")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_room_id", nullable = false)
    private StudyRoom studyRoom;

    @Column(name = "guest_name", nullable = false, length = 50)
    private String guestName;

    @Column(name = "guest_email", nullable = false, length = 255)
    private String guestEmail;

    @Column(name = "guest_phone", nullable = false, length = 20)
    private String guestPhone;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    @Column(nullable = false, length = 50)
    private String purpose;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReservationStatus status;

    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static Reservation create(
            StudyRoom studyRoom,
            String guestName,
            String guestEmail,
            String guestPhone,
            LocalDateTime startAt,
            LocalDateTime endAt,
            String purpose
    ) {
        Reservation reservation = new Reservation();
        reservation.studyRoom = studyRoom;
        reservation.guestName = guestName;
        reservation.guestEmail = guestEmail;
        reservation.guestPhone = guestPhone;
        reservation.startAt = startAt;
        reservation.endAt = endAt;
        reservation.purpose = purpose;
        reservation.status = ReservationStatus.CONFIRMED;
        return reservation;
    }

    public void cancel() {
        if (status == ReservationStatus.CANCELED) {
            return;
        }

        status = ReservationStatus.CANCELED;
        canceledAt = LocalDateTime.now();
    }
}
