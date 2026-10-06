package com.arena.venue.event;

import com.arena.venue.entity.Seat;
import com.arena.venue.entity.SeatStatus;

public record SeatChange(long seatId, long sectionId, SeatStatus status, long seq) {

  public static SeatChange of(Seat seat) {
    return new SeatChange(seat.getId(), seat.getSectionId(), seat.getStatus(), seat.getSeq());
  }
}
