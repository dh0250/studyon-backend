package com.studyon.studyon.studyroom.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record AvailabilityResponse(
        Long studyRoomId,
        LocalDate date,
        List<TimeSlotResponse> slots
) {

    public record TimeSlotResponse(
            @JsonFormat(pattern = "HH:mm") LocalTime startTime,
            @JsonFormat(pattern = "HH:mm") LocalTime endTime,
            boolean available
    ) {
    }
}
