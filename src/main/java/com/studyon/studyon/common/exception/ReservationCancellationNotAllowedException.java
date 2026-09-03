package com.studyon.studyon.common.exception;

public class ReservationCancellationNotAllowedException extends RuntimeException {
    public ReservationCancellationNotAllowedException() {
        super("예약 시작 1시간 전까지만 취소할 수 있습니다.");
    }
}
