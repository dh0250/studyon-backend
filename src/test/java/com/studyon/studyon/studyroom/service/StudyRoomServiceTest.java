package com.studyon.studyon.studyroom.service;

import com.studyon.studyon.reservation.domain.ReservationStatus;
import com.studyon.studyon.reservation.repository.ReservationRepository;
import com.studyon.studyon.reservation.repository.ReservationRepository.ReservedTime;
import com.studyon.studyon.studyroom.domain.StudyRoom;
import com.studyon.studyon.studyroom.dto.AvailabilityResponse;
import com.studyon.studyon.studyroom.exception.InvalidDateException;
import com.studyon.studyon.studyroom.exception.StudyRoomNotFoundException;
import com.studyon.studyon.studyroom.repository.StudyRoomRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class StudyRoomServiceTest {

    @Mock
    private StudyRoomRepository studyRoomRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private StudyRoom studyRoom;

    @Mock
    private ReservedTime reservedTime;

    @InjectMocks
    private StudyRoomService studyRoomService;

    @Test
    void marksReservedHourlySlotUnavailable() {
        LocalDate date = LocalDate.now().plusDays(1);
        given(studyRoomRepository.findByIdAndActiveTrue(1L)).willReturn(Optional.of(studyRoom));
        given(studyRoom.getOpenTime()).willReturn(LocalTime.of(6, 0));
        given(studyRoom.getCloseTime()).willReturn(LocalTime.of(23, 0));
        given(reservedTime.getStartAt()).willReturn(date.atTime(7, 0));
        given(reservedTime.getEndAt()).willReturn(date.atTime(8, 0));
        given(reservationRepository.findOverlapping(
                eq(1L),
                eq(ReservationStatus.CONFIRMED),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).willReturn(List.of(reservedTime));

        AvailabilityResponse response = studyRoomService.getAvailability(1L, date);

        assertThat(response.slots()).hasSize(17);
        assertThat(response.slots().get(0).available()).isTrue();
        assertThat(response.slots().get(1).available()).isFalse();
        assertThat(response.slots().get(2).available()).isTrue();
    }

    @Test
    void rejectsPastDate() {
        assertThatThrownBy(() -> studyRoomService.getAvailability(1L, LocalDate.now().minusDays(1)))
                .isInstanceOf(InvalidDateException.class);
    }

    @Test
    void rejectsMissingStudyRoom() {
        LocalDate date = LocalDate.now().plusDays(1);
        given(studyRoomRepository.findByIdAndActiveTrue(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> studyRoomService.getAvailability(999L, date))
                .isInstanceOf(StudyRoomNotFoundException.class);
    }
}
