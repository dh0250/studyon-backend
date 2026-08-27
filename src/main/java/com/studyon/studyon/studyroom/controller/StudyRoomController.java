package com.studyon.studyon.studyroom.controller;

import com.studyon.studyon.studyroom.dto.StudyRoomResponse;
import com.studyon.studyon.studyroom.service.StudyRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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


}
