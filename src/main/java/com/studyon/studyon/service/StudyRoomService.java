package com.studyon.studyon.service;

import com.studyon.studyon.common.exception.InvalidDateException;
import com.studyon.studyon.common.exception.StudyRoomNotFoundException;
import com.studyon.studyon.domain.Reservation;
import com.studyon.studyon.domain.ReservationStatus;
import com.studyon.studyon.domain.StudyRoom;
import com.studyon.studyon.dto.AvailabilityResponse;
import com.studyon.studyon.dto.AvailabilityResponse.TimeSlotResponse;
import com.studyon.studyon.dto.StudyRoomResponse;
import com.studyon.studyon.repository.ReservationRepository;
import com.studyon.studyon.repository.StudyRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class StudyRoomService {

    private final StudyRoomRepository studyRoomRepository;
    private final ReservationRepository reservationRepository;

    @Transactional(readOnly = true)
    public List<StudyRoomResponse> getStudyRooms() {
        return studyRoomRepository.findAllByActiveTrue()
                .stream()
                .map(StudyRoomResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public AvailabilityResponse getAvailability(Long studyRoomId, LocalDate date) {
        // 오늘부터 3개월 이내의 날짜만 예약할 수 있다.
        LocalDate today = LocalDate.now();
        if (date.isBefore(today) || date.isAfter(today.plusMonths(3))) {
            throw new InvalidDateException();
        }

        StudyRoom studyRoom = studyRoomRepository.findByIdAndActiveTrue(studyRoomId)
                .orElseThrow(StudyRoomNotFoundException::new);

        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();
        // 선택한 날짜와 겹치는 확정 예약 시간을 조회한다.
        List<ReservationRepository.ReservedTime> reservedTimes = reservationRepository
                .findByStudyRoomIdAndStatusAndStartAtLessThanAndEndAtGreaterThan(
                        studyRoomId,
                        ReservationStatus.CONFIRMED,
                        dayEnd,
                        dayStart
                );
        LocalDateTime now = LocalDateTime.now();

        // 운영시간을 1시간 단위로 나누고, 각 시간의 예약 가능 여부를 확인한다.
        List<TimeSlotResponse> slots = Stream.iterate(
                        studyRoom.getOpenTime(),
                        startTime -> startTime.isBefore(studyRoom.getCloseTime()),
                        startTime -> startTime.plusHours(1)
                )
                .map(startTime -> toTimeSlot(date, startTime, reservedTimes, now))
                .toList();

        return new AvailabilityResponse(studyRoomId, date, slots);
    }

    private TimeSlotResponse toTimeSlot(
            LocalDate date,
            LocalTime startTime,
            List<ReservationRepository.ReservedTime> reservedTimes,
            LocalDateTime now
    ) {
        // 날짜와 시간을 결합하여 검사할 1시간짜리 예약 구간을 만든다.
        LocalDateTime slotStart = date.atTime(startTime);
        LocalDateTime slotEnd = slotStart.plusHours(1);

        // 기존 예약 중 현재 시간대와 하나라도 겹치는 예약이 있는지 확인한다.
        boolean reserved = reservedTimes.stream()
                .anyMatch(reservedTime ->
                        reservedTime.getStartAt().isBefore(slotEnd)
                                && reservedTime.getEndAt().isAfter(slotStart)
                );

        // 지나지 않았고 기존 예약과 겹치지 않는 경우에만 예약 가능하다.
        return new TimeSlotResponse(startTime, startTime.plusHours(1), !slotStart.isBefore(now) && !reserved);
    }
}
