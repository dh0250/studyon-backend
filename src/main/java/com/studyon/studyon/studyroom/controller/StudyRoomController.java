package com.studyon.studyon.studyroom.controller;

import com.studyon.studyon.studyroom.dto.AvailabilityResponse;
import com.studyon.studyon.studyroom.dto.StudyRoomResponse;
import com.studyon.studyon.studyroom.service.StudyRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/study-rooms")
@RequiredArgsConstructor
public class StudyRoomController {

    private final StudyRoomService studyRoomService;

    @GetMapping
    public ResponseEntity<List<StudyRoomResponse>> getStudyRooms() {
        return ResponseEntity.ok(studyRoomService.getStudyRooms());
    }

    @GetMapping("/{studyRoomId}/availability")
    public ResponseEntity<AvailabilityResponse> getAvailability(
            @PathVariable Long studyRoomId,
            @RequestParam LocalDate date
    ) {
        return ResponseEntity.ok(studyRoomService.getAvailability(studyRoomId, date));
    }
}
