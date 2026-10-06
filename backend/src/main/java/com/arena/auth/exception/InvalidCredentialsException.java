package com.arena.auth.exception;

import com.arena.common.ApiException;
import org.springframework.http.HttpStatus;

public class InvalidCredentialsException extends ApiException {

  public InvalidCredentialsException() {
    super(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Wrong username or password.");
  }
}
