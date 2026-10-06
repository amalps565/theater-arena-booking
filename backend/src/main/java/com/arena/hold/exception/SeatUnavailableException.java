package com.arena.hold.exception;

import com.arena.common.ApiException;
import org.springframework.http.HttpStatus;

public class SeatUnavailableException extends ApiException {

  public SeatUnavailableException(String seatLabel) {
    super(
        HttpStatus.CONFLICT,
        "SEAT_TAKEN",
        "Seat " + seatLabel + " was just taken by someone else.");
  }
}
