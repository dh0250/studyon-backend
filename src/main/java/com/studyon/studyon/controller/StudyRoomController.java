package com.studyon.studyon.controller;

import com.studyon.studyon.dto.AvailabilityResponse;
import com.studyon.studyon.dto.StudyRoomResponse;
import com.studyon.studyon.service.StudyRoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "스터디룸", description = "운영 중인 스터디룸과 예약 가능 시간 조회 API")
public class StudyRoomController {

    private final StudyRoomService studyRoomService;

    @GetMapping
    @Operation(summary = "스터디룸 목록 조회", description = "운영 중인 스터디룸 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "스터디룸 목록 조회 성공")
    public ResponseEntity<List<StudyRoomResponse>> getStudyRooms() {
        return ResponseEntity.ok(studyRoomService.getStudyRooms());
    }

    @GetMapping("/{studyRoomId}/availability")
    @Operation(summary = "예약 가능 시간 조회", description = "선택한 날짜의 1시간 단위 예약 가능 시간을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "예약 가능 시간 조회 성공"),
            @ApiResponse(responseCode = "400", description = "예약 가능한 날짜 범위를 벗어남"),
            @ApiResponse(responseCode = "404", description = "운영 중인 스터디룸을 찾을 수 없음")
    })
    public ResponseEntity<AvailabilityResponse> getAvailability(
            @Parameter(description = "스터디룸 ID", example = "1")
            @PathVariable Long studyRoomId,
            @Parameter(description = "조회 날짜", example = "2026-09-08")
            @RequestParam LocalDate date
    ) {
        return ResponseEntity.ok(studyRoomService.getAvailability(studyRoomId, date));
    }
}
