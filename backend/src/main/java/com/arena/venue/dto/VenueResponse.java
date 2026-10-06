package com.arena.venue.dto;

import java.util.List;

public record VenueResponse(
    List<SectionView> sections, List<String> seatFields, List<Object[]> seats) {

  public static final List<String> SEAT_FIELDS =
      List.of("id", "sectionId", "x", "y", "row", "number", "status", "priceCents", "seq");
}
