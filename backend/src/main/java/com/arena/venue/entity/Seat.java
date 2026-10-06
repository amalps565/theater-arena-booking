package com.arena.venue.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "seat")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Seat {

  @Id private Long id;

  @Column(name = "section_id", nullable = false)
  private Long sectionId;

  @Column(name = "row_label", nullable = false)
  private String rowLabel;

  @Column(name = "row_index", nullable = false)
  private int rowIndex;

  @Column(name = "seat_number", nullable = false)
  private int seatNumber;

  @Column(nullable = false)
  private int x;

  @Column(nullable = false)
  private int y;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.VARCHAR)
  @Column(nullable = false)
  private SeatStatus status;

  @Column(name = "held_by")
  private UUID heldBy;

  @Column(name = "hold_expires_at")
  private Instant holdExpiresAt;

  @Column(name = "held_price_cents")
  private Long heldPriceCents;

  @Version private long version;

  @Column(nullable = false)
  private long seq;

  public String label() {
    return rowLabel + seatNumber;
  }

  public boolean isHoldExpiredAt(Instant now) {
    return status == SeatStatus.HELD && !holdExpiresAt.isAfter(now);
  }

  public void markSold() {
    status = SeatStatus.SOLD;
    heldBy = null;
    holdExpiresAt = null;
    seq++;
  }
}
