package com.arena.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ApiException.class)
  public ResponseEntity<ErrorResponse> handleApi(ApiException exception) {
    return ResponseEntity.status(exception.getStatus())
        .body(new ErrorResponse(exception.getCode(), exception.getMessage()));
  }

  @ExceptionHandler({
    MethodArgumentNotValidException.class,
    HandlerMethodValidationException.class,
    HttpMessageNotReadableException.class,
    MethodArgumentTypeMismatchException.class
  })
  public ResponseEntity<ErrorResponse> handleInvalidRequest(Exception exception) {
    return ResponseEntity.badRequest()
        .body(
            new ErrorResponse(
                "VALIDATION_FAILED", "The request is missing or has invalid fields."));
  }

  @ExceptionHandler(ConcurrencyFailureException.class)
  public ResponseEntity<ErrorResponse> handleConcurrency(ConcurrencyFailureException exception) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(new ErrorResponse("SEAT_TAKEN", "A seat you picked was just taken by someone else."));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleUnexpected(Exception exception) {
    log.error("Unexpected error", exception);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ErrorResponse("INTERNAL_ERROR", "Something went wrong. Please try again."));
  }
}
