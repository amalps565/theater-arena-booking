package com.arena.hold.exception;

import com.arena.common.ApiException;
import org.springframework.http.HttpStatus;

public class SeatNotFoundException extends ApiException {

  public SeatNotFoundException() {
    super(HttpStatus.NOT_FOUND, "SEAT_NOT_FOUND", "One or more of the seats doesn't exist.");
  }
}
