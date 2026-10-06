package com.arena.pricing;

public enum DemandBand {
  NORMAL(10_000),
  HIGH(11_500),
  PEAK(13_500);

  private final long multiplierBasisPoints;

  DemandBand(long multiplierBasisPoints) {
    this.multiplierBasisPoints = multiplierBasisPoints;
  }

  public long multiplierBasisPoints() {
    return multiplierBasisPoints;
  }

  public static DemandBand of(long occupiedSeats, long capacity) {
    if (capacity > 0 && occupiedSeats * 100 > capacity * 80) {
      return PEAK;
    }
    if (capacity > 0 && occupiedSeats * 100 > capacity * 50) {
      return HIGH;
    }
    return NORMAL;
  }
}
