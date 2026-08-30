package com.studyon.studyon.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ReservationCancelRequest(
        @NotBlank
        @Email
        @Size(max = 255)
        String guestEmail,

        @NotBlank
        @Size(max = 20)
        @Pattern(regexp = "^[0-9-]+$")
        String guestPhone
) {
}
