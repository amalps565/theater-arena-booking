package com.arena.venue.dto;

import com.arena.venue.entity.Tier;

public record SectionView(long id, String name, Tier tier, long basePriceCents, long priceSeq) {}
