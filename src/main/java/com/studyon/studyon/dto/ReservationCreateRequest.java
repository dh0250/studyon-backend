package com.studyon.studyon.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record ReservationCreateRequest(
        @NotNull @Positive Long studyRoomId,
        @NotBlank @Size(max = 50) String guestName,
        @NotBlank @Email @Size(max = 255) String guestEmail,
        @NotBlank @Size(max = 20) @Pattern(regexp = "^[0-9-]+$") String guestPhone,
        @NotNull LocalDateTime startAt,
        @NotNull LocalDateTime endAt,
        @NotBlank @Size(max = 50) String purpose
) {
}
