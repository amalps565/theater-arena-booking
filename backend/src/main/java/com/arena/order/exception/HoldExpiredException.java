package com.arena.order.exception;

import com.arena.common.ApiException;
import org.springframework.http.HttpStatus;

public class HoldExpiredException extends ApiException {

  public HoldExpiredException() {
    super(
        HttpStatus.GONE,
        "HOLD_EXPIRED",
        "Your hold expired before checkout. Pick your seats again to continue.");
  }
}
