package com.arena.venue.event;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public record SeatsChangedEvent(List<SeatChange> changes) {

  public SeatsChangedEvent {
    changes = List.copyOf(changes);
  }

  public static SeatsChangedEvent of(List<SeatChange> changes) {
    return new SeatsChangedEvent(changes);
  }

  public Set<Long> sectionIds() {
    Set<Long> ids = new TreeSet<>();
    changes.forEach(change -> ids.add(change.sectionId()));
    return ids;
  }
}
