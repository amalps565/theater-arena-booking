package com.arena.ws;

import com.arena.venue.event.SeatChange;

public record SeatMessage(String type, long seatId, String status, long seq) {

  public static SeatMessage of(SeatChange change) {
    return new SeatMessage("SEAT", change.seatId(), change.status().name(), change.seq());
  }
}
