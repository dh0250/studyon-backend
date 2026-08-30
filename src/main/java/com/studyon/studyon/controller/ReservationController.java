package com.studyon.studyon.controller;

import com.studyon.studyon.domain.Reservation;
import com.studyon.studyon.dto.*;
import com.studyon.studyon.service.ReservationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @GetMapping
    public ResponseEntity<List<ReservationSearchResponse>> searchReservations(
            @Valid @RequestParam @NotBlank @Email String guestEmail,
            @Valid @RequestParam @NotBlank @Pattern(regexp = "^[0-9-]+$") String guestPhone
    ) {
        return ResponseEntity.ok(reservationService.searchReservations(guestEmail, guestPhone));
    }

    @PostMapping
    public ResponseEntity<ReservationCreateResponse> createReservation(
            @Valid @RequestBody ReservationCreateRequest request
    ) {
        ReservationCreateResponse response = reservationService.createReservation(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{reservationId}/cancel")
    public ResponseEntity<ReservationCancelResponse> cancelReservation(
            @PathVariable Long reservationId,
            @Valid @RequestBody ReservationCancelRequest request
    ) {
        ReservationCancelResponse response = reservationService.cancelReservation(reservationId, request);
        return ResponseEntity.ok(response);
    }
}
