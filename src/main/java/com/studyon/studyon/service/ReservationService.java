package com.studyon.studyon.service;

import com.studyon.studyon.common.exception.*;
import com.studyon.studyon.domain.Reservation;
import com.studyon.studyon.domain.ReservationStatus;
import com.studyon.studyon.domain.StudyRoom;
import com.studyon.studyon.dto.*;
import com.studyon.studyon.repository.ReservationRepository;
import com.studyon.studyon.repository.StudyRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final StudyRoomRepository studyRoomRepository;
    private final ReservationRepository reservationRepository;

    @Transactional(readOnly = true)
    public List<ReservationSearchResponse> searchReservations(String guestEmail, String guestPhone) {

        String normalizedEmail = guestEmail
                .trim()
                .toLowerCase(Locale.ROOT);

        String normalizedPhone = guestPhone
                .replaceAll("\\D", "");

        return reservationRepository
                .findByGuestEmailAndGuestPhoneOrderByCreatedAtDesc(normalizedEmail, normalizedPhone)
                .stream()
                .map(ReservationSearchResponse::from)
                .toList();
    }

    @Transactional
    public ReservationCreateResponse createReservation(
            ReservationCreateRequest request
    ) {
        validateRequest(request);

        StudyRoom studyRoom = studyRoomRepository.findForUpdateByIdAndActiveTrue(request.studyRoomId())
                .orElseThrow(StudyRoomNotFoundException::new);

        validateOperatingHours(request, studyRoom);

        boolean overlapping = reservationRepository
                .existsByStudyRoomIdAndStatusAndStartAtLessThanAndEndAtGreaterThan(
                        studyRoom.getId(),
                        ReservationStatus.CONFIRMED,
                        request.endAt(),
                        request.startAt()
                );

        if (overlapping) {
            throw new ReservationConflictException("이미 예약된 시간대입니다. 다른 시간을 선택해주세요.");
        }

        Reservation reservation = Reservation.create(
                studyRoom,
                request.guestName().trim(),
                request.guestEmail().trim().toLowerCase(Locale.ROOT),
                request.guestPhone().replaceAll("\\D", ""),
                request.startAt(),
                request.endAt(),
                request.purpose().trim()
        );

        Reservation savedReservation =
                reservationRepository.save(reservation);

        return ReservationCreateResponse.from(savedReservation);
    }

    @Transactional
    public ReservationCancelResponse cancelReservation(Long reservationId, ReservationCancelRequest request) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(ReservationNotFoundException::new);

        String normalizedEmail = request.guestEmail()
                .trim()
                .toLowerCase(Locale.ROOT);

        String normalizedPhone = request.guestPhone()
                .replaceAll("\\D", "");

        if (!reservation.getGuestEmail().equals(normalizedEmail)
                || !reservation.getGuestPhone().equals(normalizedPhone)) {
            throw new ReservationVerificationException();
        }

        if (reservation.getStatus() == ReservationStatus.CANCELED) {
            return ReservationCancelResponse.from(reservation.getId(), reservation.getStatus(), reservation.getCanceledAt());
        }

        if (reservation.getStartAt().minusHours(1).isBefore(LocalDateTime.now())) {
            throw new ReservationCancellationNotAllowedException();
        }

        reservation.cancel();

        return ReservationCancelResponse.from(reservation.getId(), reservation.getStatus(), reservation.getCanceledAt());
    }

    private void validateRequest(ReservationCreateRequest request) {
        LocalDateTime now = LocalDateTime.now();
        LocalDate reservationDate = request.startAt().toLocalDate();

        // 시작 시간은 현재보다 이후여야 한다.
        if (!request.startAt().isAfter(now)) {
            throw new InvalidReservationException(
                    "현재 이후의 시간만 예약할 수 있습니다."
            );
        }

        // 시작과 종료는 같은 날짜여야 한다.
        if (!request.startAt().toLocalDate()
                .equals(request.endAt().toLocalDate())) {
            throw new InvalidReservationException(
                    "예약 시작과 종료는 같은 날짜여야 합니다."
            );
        }

        // 종료 시간은 시작 시간보다 이후여야 한다.
        if (!request.endAt().isAfter(request.startAt())) {
            throw new InvalidReservationException(
                    "종료 시간은 시작 시간보다 이후여야 합니다."
            );
        }

        // 오늘부터 3개월 이내만 예약할 수 있다.
        LocalDate lastReservationDate =
                LocalDate.now().plusMonths(3);

        if (reservationDate.isAfter(lastReservationDate)) {
            throw new InvalidReservationException(
                    "예약 가능한 날짜 범위를 벗어났습니다."
            );
        }

        // 정각 단위로만 예약할 수 있다.
        if (request.startAt().getMinute() != 0
                || request.endAt().getMinute() != 0
                || request.startAt().getSecond() != 0
                || request.endAt().getSecond() != 0) {
            throw new InvalidReservationException(
                    "예약은 1시간 단위로만 가능합니다."
            );
        }

        long durationMinutes = Duration.between(request.startAt(), request.endAt()).toMinutes();

        // 최소 1시간, 최대 4시간까지만 예약할 수 있다.
        if (durationMinutes < 60 || durationMinutes > 240 || durationMinutes % 60 != 0) {
            throw new InvalidReservationException(
                    "이용 시간은 1시간 이상 4시간 이하여야 합니다."
            );
        }

        String normalizedPhone = request.guestPhone().replaceAll("\\D", "");
        if (normalizedPhone.length() < 10 || normalizedPhone.length() > 11) {
            throw new InvalidReservationException(
                    "전화번호를 올바르게 입력해주세요."
            );
        }

        String purpose = request.purpose();

        if (purpose == null
                || purpose.isBlank()
                || purpose.trim().length() > 50) {
            throw new InvalidReservationException(
                    "이용 목적은 1자 이상 50자 이하여야 합니다."
            );
        }
    }

    private void validateOperatingHours(ReservationCreateRequest request, StudyRoom studyRoom) {
        if (request.startAt().toLocalTime()
                .isBefore(studyRoom.getOpenTime())
                || request.endAt().toLocalTime()
                .isAfter(studyRoom.getCloseTime())) {
            throw new InvalidReservationException(
                    "운영시간 안에서만 예약할 수 있습니다."
            );
        }
    }
}
