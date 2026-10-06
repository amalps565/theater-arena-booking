package com.arena.pricing;

import java.util.List;

public record SectionPrices(long sectionId, long seq, List<long[]> prices) {}
