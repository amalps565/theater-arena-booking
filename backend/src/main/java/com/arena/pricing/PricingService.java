package com.arena.pricing;

import com.arena.venue.entity.Section;
import org.springframework.stereotype.Service;

@Service
public class PricingService {

  private static final long BASIS_POINTS = 10_000;
  private static final long FRONT_ROW_BASIS_POINTS = 13_000;
  private static final long ROW_PREMIUM_BASIS_POINTS = FRONT_ROW_BASIS_POINTS - BASIS_POINTS;
  private static final long ROUNDING_STEP_CENTS = 50;

  public long priceCents(Section section, int rowIndex, long occupiedSeats) {
    long rowBasisPoints = rowMultiplierBasisPoints(rowIndex, section.getRowCount());
    long demandBasisPoints = band(section, occupiedSeats).multiplierBasisPoints();
    long scaled = section.getBasePriceCents() * rowBasisPoints * demandBasisPoints;
    long divisor = BASIS_POINTS * BASIS_POINTS * ROUNDING_STEP_CENTS;
    return (scaled + divisor / 2) / divisor * ROUNDING_STEP_CENTS;
  }

  public DemandBand band(Section section, long occupiedSeats) {
    return DemandBand.of(occupiedSeats, section.capacity());
  }

  private long rowMultiplierBasisPoints(int rowIndex, int rowCount) {
    int backRow = Math.max(rowCount - 1, 1);
    int clampedRow = Math.min(Math.max(rowIndex, 0), backRow);
    return FRONT_ROW_BASIS_POINTS - ROW_PREMIUM_BASIS_POINTS * clampedRow / backRow;
  }
}
