package com.arena.venue.repository;

import com.arena.venue.entity.SeatStatus;
import java.time.Instant;

public record SeatRow(
    long id,
    long sectionId,
    int x,
    int y,
    String rowLabel,
    int rowIndex,
    int seatNumber,
    SeatStatus status,
    Instant holdExpiresAt,
    long seq) {

  public SeatStatus statusAt(Instant now) {
    boolean expired = status == SeatStatus.HELD && !holdExpiresAt.isAfter(now);
    return expired ? SeatStatus.AVAILABLE : status;
  }
}
