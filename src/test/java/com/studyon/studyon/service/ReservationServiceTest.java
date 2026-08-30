package com.studyon.studyon.service;

import com.studyon.studyon.common.exception.ReservationConflictException;
import com.studyon.studyon.domain.Reservation;
import com.studyon.studyon.domain.ReservationStatus;
import com.studyon.studyon.domain.StudyRoom;
import com.studyon.studyon.dto.ReservationCreateRequest;
import com.studyon.studyon.dto.ReservationCreateResponse;
import com.studyon.studyon.repository.ReservationRepository;
import com.studyon.studyon.repository.StudyRoomRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private StudyRoomRepository studyRoomRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private StudyRoom studyRoom;

    @InjectMocks
    private ReservationService reservationService;

    @Test
    void createsReservationWhenTimeIsAvailable() {
        LocalDateTime startAt = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endAt = startAt.plusHours(2);
        ReservationCreateRequest request = request(startAt, endAt);

        given(studyRoomRepository.findForUpdateByIdAndActiveTrue(1L)).willReturn(Optional.of(studyRoom));
        given(studyRoom.getId()).willReturn(1L);
        given(studyRoom.getName()).willReturn("4인 1호실");
        given(studyRoom.getOpenTime()).willReturn(LocalTime.of(6, 0));
        given(studyRoom.getCloseTime()).willReturn(LocalTime.of(23, 0));
        given(reservationRepository
                .existsByStudyRoomIdAndStatusAndStartAtLessThanAndEndAtGreaterThan(
                        1L, ReservationStatus.CONFIRMED, endAt, startAt
                )).willReturn(false);
        given(reservationRepository.save(org.mockito.ArgumentMatchers.any(Reservation.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        ReservationCreateResponse response = reservationService.createReservation(request);

        assertThat(response.studyRoomId()).isEqualTo(1L);
        assertThat(response.guestEmail()).isEqualTo("guest@example.com");
        assertThat(response.guestPhone()).isEqualTo("01012345678");
        assertThat(response.status()).isEqualTo(ReservationStatus.CONFIRMED);
    }

    @Test
    void rejectsOverlappingReservation() {
        LocalDateTime startAt = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endAt = startAt.plusHours(2);
        ReservationCreateRequest request = request(startAt, endAt);

        given(studyRoomRepository.findForUpdateByIdAndActiveTrue(1L)).willReturn(Optional.of(studyRoom));
        given(studyRoom.getId()).willReturn(1L);
        given(studyRoom.getOpenTime()).willReturn(LocalTime.of(6, 0));
        given(studyRoom.getCloseTime()).willReturn(LocalTime.of(23, 0));
        given(reservationRepository
                .existsByStudyRoomIdAndStatusAndStartAtLessThanAndEndAtGreaterThan(
                        1L, ReservationStatus.CONFIRMED, endAt, startAt
                )).willReturn(true);

        assertThatThrownBy(() -> reservationService.createReservation(request))
                .isInstanceOf(ReservationConflictException.class);
    }

    private ReservationCreateRequest request(LocalDateTime startAt, LocalDateTime endAt) {
        return new ReservationCreateRequest(
                1L,
                "홍길동",
                "GUEST@example.com",
                "010-1234-5678",
                startAt,
                endAt,
                "프로젝트 회의"
        );
    }
}
