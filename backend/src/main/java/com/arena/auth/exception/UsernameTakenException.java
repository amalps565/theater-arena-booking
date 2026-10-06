package com.arena.auth.exception;

import com.arena.common.ApiException;
import org.springframework.http.HttpStatus;

public class UsernameTakenException extends ApiException {

  public UsernameTakenException() {
    super(HttpStatus.CONFLICT, "USERNAME_TAKEN", "That username is already taken.");
  }
}
