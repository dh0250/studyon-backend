package com.studyon.studyon.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidDateException.class)
    public ResponseEntity<ProblemDetail> handleInvalidDate(InvalidDateException exception) {
        return createProblem(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(StudyRoomNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleStudyRoomNotFound(StudyRoomNotFoundException exception) {
        return createProblem(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(InvalidReservationException.class)
    public ResponseEntity<ProblemDetail> handleInvalidReservation(InvalidReservationException exception) {
        return createProblem(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(ReservationConflictException.class)
    public ResponseEntity<ProblemDetail> handleReservationConflict(ReservationConflictException exception) {
        return createProblem(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(ReservationNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleReservationNotFound(ReservationNotFoundException exception) {
        return createProblem(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(ReservationVerificationException.class)
    public ResponseEntity<ProblemDetail> handleReservationVerification(ReservationVerificationException exception) {
        return createProblem(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    private ResponseEntity<ProblemDetail> createProblem(HttpStatus status, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        return ResponseEntity.status(status).body(problem);
    }
}
