package com.arena.hold.exception;

import com.arena.common.ApiException;
import org.springframework.http.HttpStatus;

public class HoldLimitReachedException extends ApiException {

  public HoldLimitReachedException(int maxSeats) {
    super(
        HttpStatus.CONFLICT,
        "HOLD_LIMIT_REACHED",
        "You can hold at most " + maxSeats + " seats at once.");
  }
}
