package com.studyon.studyon.common.exception;

import com.studyon.studyon.common.response.ApiResponse;
import com.studyon.studyon.common.response.ResponseCode;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValid(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse(ResponseCode.VALIDATION_ERROR.getMessage());
        return createError(HttpStatus.BAD_REQUEST, ResponseCode.VALIDATION_ERROR, message);
    }

    @ExceptionHandler({HandlerMethodValidationException.class, ConstraintViolationException.class})
    public ResponseEntity<ApiResponse<Void>> handleValidationException(Exception exception) {
        return createError(HttpStatus.BAD_REQUEST, ResponseCode.VALIDATION_ERROR, ResponseCode.VALIDATION_ERROR.getMessage());
    }

    @ExceptionHandler(InvalidDateException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidDate(InvalidDateException exception) {
        return createError(HttpStatus.BAD_REQUEST, ResponseCode.INVALID_DATE, exception.getMessage());
    }

    @ExceptionHandler(StudyRoomNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleStudyRoomNotFound(StudyRoomNotFoundException exception) {
        return createError(HttpStatus.NOT_FOUND, ResponseCode.STUDY_ROOM_NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(InvalidReservationException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidReservation(InvalidReservationException exception) {
        return createError(HttpStatus.BAD_REQUEST, ResponseCode.INVALID_RESERVATION, exception.getMessage());
    }

    @ExceptionHandler(ReservationConflictException.class)
    public ResponseEntity<ApiResponse<Void>> handleReservationConflict(ReservationConflictException exception) {
        return createError(HttpStatus.CONFLICT, ResponseCode.RESERVATION_CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(ReservationNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleReservationNotFound(ReservationNotFoundException exception) {
        return createError(HttpStatus.NOT_FOUND, ResponseCode.RESERVATION_NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(ReservationVerificationException.class)
    public ResponseEntity<ApiResponse<Void>> handleReservationVerification(ReservationVerificationException exception) {
        return createError(
                HttpStatus.BAD_REQUEST,
                ResponseCode.RESERVATION_VERIFICATION_FAILED,
                exception.getMessage()
        );
    }

    @ExceptionHandler(ReservationCancellationNotAllowedException.class)
    public ResponseEntity<ApiResponse<Void>> handleReservationCancellationNotAllowed(
            ReservationCancellationNotAllowedException exception
    ) {
        return createError(
                HttpStatus.BAD_REQUEST,
                ResponseCode.RESERVATION_CANCELLATION_NOT_ALLOWED,
                exception.getMessage()
        );
    }

    private ResponseEntity<ApiResponse<Void>> createError(
            HttpStatus status,
            ResponseCode responseCode,
            String message
    ) {
        return ResponseEntity.status(status).body(ApiResponse.error(responseCode, message));
    }
}
