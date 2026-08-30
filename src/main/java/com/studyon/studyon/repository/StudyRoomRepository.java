package com.studyon.studyon.repository;

import com.studyon.studyon.domain.StudyRoom;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudyRoomRepository extends JpaRepository<StudyRoom, Long> {

    List<StudyRoom> findAllByActiveTrue();

    Optional<StudyRoom> findByIdAndActiveTrue(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<StudyRoom> findForUpdateByIdAndActiveTrue(Long id);
}
