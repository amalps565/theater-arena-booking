package com.arena.hold.dto;

import com.arena.venue.entity.Seat;
import java.time.Instant;

public record HeldSeat(long seatId, long priceCents, Instant expiresAt) {

  public static HeldSeat of(Seat seat) {
    return new HeldSeat(seat.getId(), seat.getHeldPriceCents(), seat.getHoldExpiresAt());
  }
}
