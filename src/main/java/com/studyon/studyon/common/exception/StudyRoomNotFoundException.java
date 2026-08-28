package com.studyon.studyon.common.exception;

public class StudyRoomNotFoundException extends RuntimeException {

    public StudyRoomNotFoundException() {
        super("스터디룸을 찾을 수 없습니다.");
    }
}
