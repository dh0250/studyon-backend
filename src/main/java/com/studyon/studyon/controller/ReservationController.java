package com.studyon.studyon.controller;

import com.studyon.studyon.dto.*;
import com.studyon.studyon.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
@Tag(name = "예약", description = "비회원 예약 생성·조회·취소 API")
public class ReservationController {

    private final ReservationService reservationService;

    @GetMapping
    @Operation(summary = "예약 목록 조회", description = "이메일과 전화번호가 일치하는 예약을 최신 생성 순으로 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "예약 목록 조회 성공"),
            @ApiResponse(responseCode = "400", description = "이메일 또는 전화번호 형식이 올바르지 않음")
    })
    public ResponseEntity<List<ReservationSearchResponse>> searchReservations(
            @Parameter(description = "예약자 이메일", example = "guest@example.com")
            @RequestParam @NotBlank @Email String guestEmail,
            @Parameter(description = "예약자 전화번호", example = "010-1234-5678")
            @RequestParam @NotBlank @Pattern(regexp = "^[0-9-]+$") String guestPhone
    ) {
        return ResponseEntity.ok(reservationService.searchReservations(guestEmail, guestPhone));
    }

    @PostMapping
    @Operation(summary = "비회원 예약 생성", description = "같은 스터디룸·시간대에 확정 예약이 없을 때 예약을 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "예약 생성 성공"),
            @ApiResponse(responseCode = "400", description = "입력값 또는 예약 정책 위반"),
            @ApiResponse(responseCode = "404", description = "운영 중인 스터디룸을 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "이미 예약된 시간대")
    })
    public ResponseEntity<ReservationCreateResponse> createReservation(
            @Valid @RequestBody ReservationCreateRequest request
    ) {
        ReservationCreateResponse response = reservationService.createReservation(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{reservationId}/cancel")
    @Operation(summary = "예약 취소", description = "예약자 정보를 확인한 뒤 예약 상태를 CANCELED로 변경합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "예약 취소 성공"),
            @ApiResponse(responseCode = "400", description = "입력값이 올바르지 않거나 예약자 정보가 일치하지 않음"),
            @ApiResponse(responseCode = "404", description = "예약을 찾을 수 없음")
    })
    public ResponseEntity<ReservationCancelResponse> cancelReservation(
            @Parameter(description = "예약 ID", example = "1")
            @PathVariable Long reservationId,
            @Valid @RequestBody ReservationCancelRequest request
    ) {
        ReservationCancelResponse response = reservationService.cancelReservation(reservationId, request);
        return ResponseEntity.ok(response);
    }
}
