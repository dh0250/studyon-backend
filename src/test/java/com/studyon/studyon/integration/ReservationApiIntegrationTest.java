package com.studyon.studyon.integration;

import com.studyon.studyon.domain.StudyRoom;
import com.studyon.studyon.dto.ReservationCancelRequest;
import com.studyon.studyon.dto.ReservationCreateRequest;
import com.studyon.studyon.repository.ReservationRepository;
import com.studyon.studyon.repository.StudyRoomRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class ReservationApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private StudyRoomRepository studyRoomRepository;

    @BeforeEach
    void setUp() {
        reservationRepository.deleteAllInBatch();
    }

    @AfterEach
    void tearDown() {
        reservationRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("예약 생성 API는 유효한 요청이면 201 Created와 예약 정보를 반환한다")
    void createsReservation() throws Exception {
        StudyRoom studyRoom = activeStudyRoom();

        mockMvc.perform(post("/api/v1/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest(studyRoom))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.studyRoomId").value(studyRoom.getId()))
                .andExpect(jsonPath("$.guestEmail").value("api-integration-test@example.com"))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    @DisplayName("동일한 시간대의 예약 생성 API는 409 Conflict를 반환한다")
    void rejectsOverlappingReservation() throws Exception {
        StudyRoom studyRoom = activeStudyRoom();
        ReservationCreateRequest request = createRequest(studyRoom);

        createReservation(studyRoom);

        mockMvc.perform(post("/api/v1/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("이미 예약된 시간대입니다. 다른 시간을 선택해주세요."));
    }

    @Test
    @DisplayName("예약 조회 API는 이메일과 전화번호가 일치하는 예약 목록을 반환한다")
    void searchesReservations() throws Exception {
        StudyRoom studyRoom = activeStudyRoom();

        createReservation(studyRoom);

        mockMvc.perform(get("/api/v1/reservations")
                        .param("guestEmail", "API-INTEGRATION-TEST@EXAMPLE.COM")
                        .param("guestPhone", "010-1234-5678"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].studyRoomId").value(studyRoom.getId()))
                .andExpect(jsonPath("$[0].status").value("CONFIRMED"));
    }

    @Test
    @DisplayName("예약 취소 API는 예약자 정보가 일치하면 예약 상태를 CANCELED로 변경한다")
    void cancelsReservation() throws Exception {
        StudyRoom studyRoom = activeStudyRoom();

        createReservation(studyRoom);

        Long reservationId = reservationRepository.findAll().getFirst().getId();
        ReservationCancelRequest request = new ReservationCancelRequest(
                "api-integration-test@example.com",
                "01012345678"
        );

        mockMvc.perform(patch("/api/v1/reservations/{reservationId}/cancel", reservationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservationId").value(reservationId))
                .andExpect(jsonPath("$.status").value("CANCELED"));
    }

    private void createReservation(StudyRoom studyRoom) throws Exception {
        mockMvc.perform(post("/api/v1/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest(studyRoom))))
                .andExpect(status().isCreated());
    }

    private StudyRoom activeStudyRoom() {
        return studyRoomRepository.findAllByActiveTrue().getFirst();
    }

    private ReservationCreateRequest createRequest(StudyRoom studyRoom) {
        LocalDateTime startAt = LocalDate.now().plusDays(1).atTime(10, 0);

        return new ReservationCreateRequest(
                studyRoom.getId(),
                "API 테스트 사용자",
                "api-integration-test@example.com",
                "01012345678",
                startAt,
                startAt.plusHours(1),
                "API 통합 테스트"
        );
    }
}
