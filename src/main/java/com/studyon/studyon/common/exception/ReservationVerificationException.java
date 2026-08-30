package com.studyon.studyon.common.exception;

public class ReservationVerificationException extends RuntimeException {
    public ReservationVerificationException() {
        super("예약자 정보가 일치하지 않습니다.");
    }
}
