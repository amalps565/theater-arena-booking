package com.arena.hold.exception;

import com.arena.common.ApiException;
import org.springframework.http.HttpStatus;

public class HoldNotFoundException extends ApiException {

  public HoldNotFoundException() {
    super(HttpStatus.NOT_FOUND, "HOLD_NOT_FOUND", "You don't hold that seat.");
  }
}
