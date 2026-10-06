package com.arena.ws;

import com.arena.pricing.SectionPrices;
import java.util.List;

public record PricesMessage(String type, long sectionId, long seq, List<long[]> prices) {

  public static PricesMessage of(SectionPrices sectionPrices) {
    return new PricesMessage(
        "PRICES", sectionPrices.sectionId(), sectionPrices.seq(), sectionPrices.prices());
  }
}
