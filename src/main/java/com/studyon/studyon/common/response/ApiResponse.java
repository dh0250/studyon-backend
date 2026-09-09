package com.studyon.studyon.common.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ApiResponse<T> {

    private final String code;
    private final String message;
    private final T data;

    public static <T> ApiResponse<T> success(T data) {
        return of(ResponseCode.SUCCESS, data);
    }

    public static <T> ApiResponse<T> error(ResponseCode responseCode) {
        return of(responseCode, null);
    }

    public static <T> ApiResponse<T> error(ResponseCode responseCode, String message) {
        return ApiResponse.<T>builder()
                .code(responseCode.getCode())
                .message(message)
                .data(null)
                .build();
    }

    private static <T> ApiResponse<T> of(ResponseCode responseCode, T data) {
        return ApiResponse.<T>builder()
                .code(responseCode.getCode())
                .message(responseCode.getMessage())
                .data(data)
                .build();
    }
}
