package com.arena.pricing;

import static org.assertj.core.api.Assertions.assertThat;

import com.arena.venue.entity.Section;
import com.arena.venue.entity.Tier;
import org.junit.jupiter.api.Test;

class PricingServiceTest {

  private static final int ROWS = 40;
  private static final int SEATS_PER_ROW = 50;
  private static final long CAPACITY = (long) ROWS * SEATS_PER_ROW;

  private final PricingService pricingService = new PricingService();
  private final Section premium =
      new Section(1L, "Premium", Tier.PREMIUM, 8_000, ROWS, SEATS_PER_ROW);

  @Test
  void frontRowCostsThirtyPercentMoreThanBackRow() {
    long front = pricingService.priceCents(premium, 0, 0);
    long back = pricingService.priceCents(premium, ROWS - 1, 0);

    assertThat(front).isEqualTo(10_400);
    assertThat(back).isEqualTo(8_000);
  }

  @Test
  void sectionMoreThanHalfFullRaisesPriceByFifteenPercent() {
    long occupied = CAPACITY / 2 + 1;

    long price = pricingService.priceCents(premium, 0, occupied);

    assertThat(price).isEqualTo(11_950);
  }

  @Test
  void sectionMoreThanFourFifthsFullRaisesPriceByThirtyFivePercent() {
    long occupied = CAPACITY * 4 / 5 + 1;

    long price = pricingService.priceCents(premium, 0, occupied);

    assertThat(price).isEqualTo(14_050);
  }

  @Test
  void exactlyHalfFullStaysAtNormalDemand() {
    DemandBand band = pricingService.band(premium, CAPACITY / 2);

    assertThat(band).isEqualTo(DemandBand.NORMAL);
  }

  @Test
  void everyPriceIsAMultipleOfFiftyCents() {
    for (int row = 0; row < ROWS; row++) {
      long price = pricingService.priceCents(premium, row, CAPACITY * 3 / 5);

      assertThat(price % 50).as("row %d price %d", row, price).isZero();
    }
  }

  @Test
  void vipFrontRowCostsMoreThanPremiumFrontRow() {
    Section vip = new Section(2L, "VIP", Tier.VIP, 15_000, ROWS, SEATS_PER_ROW);

    long vipPrice = pricingService.priceCents(vip, 0, 0);
    long premiumPrice = pricingService.priceCents(premium, 0, 0);

    assertThat(vipPrice).isGreaterThan(premiumPrice);
  }
}
