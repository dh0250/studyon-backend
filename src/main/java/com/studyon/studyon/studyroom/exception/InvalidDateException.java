package com.studyon.studyon.studyroom.exception;

public class InvalidDateException extends RuntimeException {

    public InvalidDateException() {
        super("예약 가능한 날짜 범위를 벗어났습니다.");
    }
}
