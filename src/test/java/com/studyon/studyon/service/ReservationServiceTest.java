package com.studyon.studyon.service;

import com.studyon.studyon.common.exception.ReservationConflictException;
import com.studyon.studyon.common.exception.ReservationVerificationException;
import com.studyon.studyon.domain.Reservation;
import com.studyon.studyon.domain.ReservationStatus;
import com.studyon.studyon.domain.StudyRoom;
import com.studyon.studyon.dto.ReservationCancelRequest;
import com.studyon.studyon.dto.ReservationCancelResponse;
import com.studyon.studyon.dto.ReservationCreateRequest;
import com.studyon.studyon.dto.ReservationCreateResponse;
import com.studyon.studyon.dto.ReservationSearchResponse;
import com.studyon.studyon.repository.ReservationRepository;
import com.studyon.studyon.repository.StudyRoomRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private StudyRoomRepository studyRoomRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private StudyRoom studyRoom;

    @Mock
    private Reservation reservation;

    @InjectMocks
    private ReservationService reservationService;

    @Test
    @DisplayName("예약 가능한 시간에는 예약을 생성한다")
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
        //어떤 Reservation 객체가 save()에 전달되어도 전달받은 객체를 그대로 반환하게 설정
        given(reservationRepository.save(org.mockito.ArgumentMatchers.any(Reservation.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        ReservationCreateResponse response = reservationService.createReservation(request);

        assertThat(response.studyRoomId()).isEqualTo(1L);
        assertThat(response.guestEmail()).isEqualTo("guest@example.com");
        assertThat(response.guestPhone()).isEqualTo("01012345678");
        assertThat(response.status()).isEqualTo(ReservationStatus.CONFIRMED);
    }

    @Test
    @DisplayName("기존 예약과 시간이 겹치면 예약 생성을 거절한다")
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

    @Test
    @DisplayName("이메일과 전화번호를 정규화하여 예약 목록을 조회한다")
    void searchesReservationsWithNormalizedGuestInformation() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 8, 30, 10, 0);
        given(reservationRepository.findByGuestEmailAndGuestPhoneOrderByCreatedAtDesc(
                "guest@example.com", "01012345678"
        )).willReturn(List.of(reservation));
        given(reservation.getId()).willReturn(1L);
        given(reservation.getStudyRoom()).willReturn(studyRoom);
        given(studyRoom.getId()).willReturn(2L);
        given(studyRoom.getName()).willReturn("4인 1호실");
        given(reservation.getStatus()).willReturn(ReservationStatus.CONFIRMED);
        given(reservation.getCreatedAt()).willReturn(createdAt);

        List<ReservationSearchResponse> responses = reservationService.searchReservations(
                " GUEST@example.com ", "010-1234-5678"
        );

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().reservationId()).isEqualTo(1L);
        assertThat(responses.getFirst().studyRoomName()).isEqualTo("4인 1호실");
        assertThat(responses.getFirst().createdAt()).isEqualTo(createdAt);
    }

    @Test
    @DisplayName("예약자 정보가 일치하면 예약을 취소한다")
    void cancelsReservationWhenGuestInformationMatches() {
        LocalDateTime canceledAt = LocalDateTime.of(2026, 8, 30, 11, 0);
        ReservationCancelRequest request = new ReservationCancelRequest(
                " GUEST@example.com ", "010-1234-5678"
        );
        given(reservationRepository.findById(1L)).willReturn(Optional.of(reservation));
        given(reservation.getGuestEmail()).willReturn("guest@example.com");
        given(reservation.getGuestPhone()).willReturn("01012345678");
        given(reservation.getId()).willReturn(1L);
        given(reservation.getStatus()).willReturn(ReservationStatus.CANCELED);
        given(reservation.getCanceledAt()).willReturn(canceledAt);

        ReservationCancelResponse response = reservationService.cancelReservation(1L, request);

        verify(reservation).cancel();
        assertThat(response.reservationId()).isEqualTo(1L);
        assertThat(response.status()).isEqualTo(ReservationStatus.CANCELED);
        assertThat(response.canceledAt()).isEqualTo(canceledAt);
    }

    @Test
    @DisplayName("예약자 정보가 일치하지 않으면 예약 취소를 거절한다")
    void rejectsCancellationWhenGuestInformationDoesNotMatch() {
        ReservationCancelRequest request = new ReservationCancelRequest(
                "other@example.com", "010-9999-9999"
        );
        given(reservationRepository.findById(1L)).willReturn(Optional.of(reservation));
        given(reservation.getGuestEmail()).willReturn("guest@example.com");

        assertThatThrownBy(() -> reservationService.cancelReservation(1L, request))
                .isInstanceOf(ReservationVerificationException.class);
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
