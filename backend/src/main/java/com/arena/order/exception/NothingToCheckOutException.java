package com.arena.order.exception;

import com.arena.common.ApiException;
import org.springframework.http.HttpStatus;

public class NothingToCheckOutException extends ApiException {

  public NothingToCheckOutException() {
    super(
        HttpStatus.BAD_REQUEST,
        "NOTHING_TO_CHECK_OUT",
        "You have no seats on hold. Holds last 60 seconds.");
  }
}
