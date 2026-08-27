package com.studyon.studyon.studyroom.service;

import com.studyon.studyon.studyroom.dto.StudyRoomResponse;
import com.studyon.studyon.studyroom.repository.StudyRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudyRoomService {

    private final StudyRoomRepository studyRoomRepository;

    @Transactional(readOnly = true)
    public List<StudyRoomResponse> getStudyRooms() {
        return studyRoomRepository.findAllByActiveTrue()
                .stream()
                .map(StudyRoomResponse::from)
                .toList();
    }


}
