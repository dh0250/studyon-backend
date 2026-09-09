package com.studyon.studyon.common.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ResponseCode {

    SUCCESS("SUCCESS", "요청에 성공했습니다."),

    VALIDATION_ERROR("VALIDATION_ERROR", "입력값이 올바르지 않습니다."),
    INVALID_DATE("INVALID_DATE", "예약 가능한 날짜 범위를 벗어났습니다."),
    INVALID_RESERVATION("INVALID_RESERVATION", "입력값 또는 예약 정책이 올바르지 않습니다."),
    STUDY_ROOM_NOT_FOUND("STUDY_ROOM_NOT_FOUND", "스터디룸을 찾을 수 없습니다."),
    RESERVATION_CONFLICT("RESERVATION_CONFLICT", "이미 예약된 시간대입니다. 다른 시간을 선택해주세요."),
    RESERVATION_NOT_FOUND("RESERVATION_NOT_FOUND", "예약을 찾을 수 없습니다."),
    RESERVATION_VERIFICATION_FAILED("RESERVATION_VERIFICATION_FAILED", "예약자 정보가 일치하지 않습니다."),
    RESERVATION_CANCELLATION_NOT_ALLOWED(
            "RESERVATION_CANCELLATION_NOT_ALLOWED",
            "예약 시작 1시간 전까지만 취소할 수 있습니다."
    );

    private final String code;
    private final String message;
}
