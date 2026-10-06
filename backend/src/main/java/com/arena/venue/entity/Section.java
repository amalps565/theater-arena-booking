package com.arena.venue.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "section")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Section {

  @Id private Long id;

  @Column(nullable = false)
  private String name;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.VARCHAR)
  @Column(nullable = false)
  private Tier tier;

  @Column(name = "base_price_cents", nullable = false)
  private long basePriceCents;

  @Column(name = "row_count", nullable = false)
  private int rowCount;

  @Column(name = "seats_per_row", nullable = false)
  private int seatsPerRow;

  public Section(
      Long id, String name, Tier tier, long basePriceCents, int rowCount, int seatsPerRow) {
    this.id = id;
    this.name = name;
    this.tier = tier;
    this.basePriceCents = basePriceCents;
    this.rowCount = rowCount;
    this.seatsPerRow = seatsPerRow;
  }

  public long capacity() {
    return (long) rowCount * seatsPerRow;
  }
}
