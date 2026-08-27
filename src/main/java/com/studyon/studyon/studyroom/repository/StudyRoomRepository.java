package com.studyon.studyon.studyroom.repository;

import com.studyon.studyon.studyroom.domain.StudyRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudyRoomRepository extends JpaRepository<StudyRoom, Long> {
    List<StudyRoom> findAllByActiveTrue();

    
}
