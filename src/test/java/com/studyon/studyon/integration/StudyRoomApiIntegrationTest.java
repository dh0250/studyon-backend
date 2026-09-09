package com.studyon.studyon.integration;

import com.studyon.studyon.domain.StudyRoom;
import com.studyon.studyon.repository.StudyRoomRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class StudyRoomApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StudyRoomRepository studyRoomRepository;

    @Test
    @DisplayName("스터디룸 목록 조회 API는 운영 중인 스터디룸 목록을 반환한다")
    void getsStudyRooms() throws Exception {
        mockMvc.perform(get("/api/v1/study-rooms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isNotEmpty());
    }

    @Test
    @DisplayName("예약 가능 시간 조회 API는 선택한 날짜의 시간 슬롯을 반환한다")
    void getsAvailability() throws Exception {
        StudyRoom studyRoom = studyRoomRepository.findAllByActiveTrue().getFirst();

        mockMvc.perform(get("/api/v1/study-rooms/{studyRoomId}/availability", studyRoom.getId())
                        .param("date", LocalDate.now().plusDays(1).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studyRoomId").value(studyRoom.getId()))
                .andExpect(jsonPath("$.slots").isArray())
                .andExpect(jsonPath("$.slots").isNotEmpty());
    }

    @Test
    @DisplayName("존재하지 않는 스터디룸의 예약 가능 시간 조회 API는 404 Not Found를 반환한다")
    void rejectsMissingStudyRoom() throws Exception {
        mockMvc.perform(get("/api/v1/study-rooms/{studyRoomId}/availability", Long.MAX_VALUE)
                        .param("date", LocalDate.now().plusDays(1).toString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("스터디룸을 찾을 수 없습니다."));
    }
}
