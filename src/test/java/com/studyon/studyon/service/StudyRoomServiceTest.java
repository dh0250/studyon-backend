package com.studyon.studyon.service;

import com.studyon.studyon.common.exception.InvalidDateException;
import com.studyon.studyon.common.exception.StudyRoomNotFoundException;
import com.studyon.studyon.domain.ReservationStatus;
import com.studyon.studyon.domain.StudyRoom;
import com.studyon.studyon.dto.AvailabilityResponse;
import com.studyon.studyon.repository.ReservationRepository;
import com.studyon.studyon.repository.StudyRoomRepository;
import org.junit.jupiter.api.DisplayName;
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
    private ReservationRepository.ReservedTime reservedTime;

    @InjectMocks
    private StudyRoomService studyRoomService;

    @Test
    @DisplayName("예약된 1시간 슬롯은 예약 불가능으로 표시한다")
    void marksReservedHourlySlotUnavailable() {
        LocalDate date = LocalDate.now().plusDays(1);
        given(studyRoomRepository.findByIdAndActiveTrue(1L)).willReturn(Optional.of(studyRoom));
        given(studyRoom.getOpenTime()).willReturn(LocalTime.of(6, 0));
        given(studyRoom.getCloseTime()).willReturn(LocalTime.of(23, 0));
        given(reservedTime.getStartAt()).willReturn(date.atTime(7, 0));
        given(reservedTime.getEndAt()).willReturn(date.atTime(8, 0));
        given(reservationRepository.findByStudyRoomIdAndStatusAndStartAtLessThanAndEndAtGreaterThan(
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
    @DisplayName("과거 날짜의 예약 가능 시간 조회를 거절한다")
    void rejectsPastDate() {
        assertThatThrownBy(() -> studyRoomService.getAvailability(1L, LocalDate.now().minusDays(1)))
                .isInstanceOf(InvalidDateException.class);
    }

    @Test
    @DisplayName("존재하지 않는 스터디룸의 예약 가능 시간 조회를 거절한다")
    void rejectsMissingStudyRoom() {
        LocalDate date = LocalDate.now().plusDays(1);
        given(studyRoomRepository.findByIdAndActiveTrue(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> studyRoomService.getAvailability(999L, date))
                .isInstanceOf(StudyRoomNotFoundException.class);
    }
}
