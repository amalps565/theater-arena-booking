package com.arena.hold.service;

import com.arena.hold.dto.HeldSeat;
import com.arena.hold.dto.HoldResponse;
import com.arena.hold.exception.HoldLimitReachedException;
import com.arena.hold.exception.HoldNotFoundException;
import com.arena.hold.exception.SeatNotFoundException;
import com.arena.hold.exception.SeatUnavailableException;
import com.arena.pricing.PricingService;
import com.arena.venue.entity.Seat;
import com.arena.venue.entity.Section;
import com.arena.venue.event.SeatsChangedEvent;
import com.arena.venue.repository.SeatRepository;
import com.arena.venue.repository.SectionRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HoldService {

  public static final Duration HOLD_DURATION = Duration.ofSeconds(60);
  public static final int MAX_HELD_SEATS = 8;

  private final SeatRepository seatRepository;
  private final SectionRepository sectionRepository;
  private final PricingService pricingService;
  private final ApplicationEventPublisher events;
  private final Clock clock;

  @Transactional
  public HoldResponse hold(UUID customerId, Collection<Long> requestedSeatIds) {
    List<Long> seatIds = requestedSeatIds.stream().distinct().sorted().toList();
    Instant now = clock.instant();
    if (seatRepository.countLiveHolds(customerId, now) + seatIds.size() > MAX_HELD_SEATS) {
      throw new HoldLimitReachedException(MAX_HELD_SEATS);
    }
    List<Seat> seats =
        seatRepository.findAllById(seatIds).stream()
            .sorted(Comparator.comparing(Seat::getId))
            .toList();
    if (seats.size() != seatIds.size()) {
      throw new SeatNotFoundException();
    }
    Map<Long, Section> sections = sectionsOf(seats);
    Map<Long, Long> occupied = new HashMap<>();
    sections
        .keySet()
        .forEach(id -> occupied.put(id, seatRepository.countOccupiedInSection(id, now)));

    Instant expiresAt = now.plus(HOLD_DURATION);
    List<HeldSeat> held = new ArrayList<>();
    for (Seat seat : seats) {
      Section section = sections.get(seat.getSectionId());
      long price =
          pricingService.priceCents(section, seat.getRowIndex(), occupied.get(section.getId()));
      int updated = seatRepository.holdIfFree(seat.getId(), customerId, expiresAt, price, now);
      if (updated == 0) {
        throw new SeatUnavailableException(seat.label());
      }
      held.add(new HeldSeat(seat.getId(), price, expiresAt));
    }
    publishChanges(seatIds);
    return new HoldResponse(expiresAt, held);
  }

  @Transactional
  public void release(UUID customerId, long seatId) {
    if (seatRepository.releaseIfHeldBy(seatId, customerId, clock.instant()) == 0) {
      throw new HoldNotFoundException();
    }
    publishChanges(List.of(seatId));
  }

  @Transactional(readOnly = true)
  public List<HeldSeat> liveHolds(UUID customerId) {
    return seatRepository.findLiveHolds(customerId, clock.instant()).stream()
        .map(HeldSeat::of)
        .toList();
  }

  private Map<Long, Section> sectionsOf(List<Seat> seats) {
    List<Long> sectionIds = seats.stream().map(Seat::getSectionId).distinct().toList();
    Map<Long, Section> sections = new HashMap<>();
    sectionRepository.findAllById(sectionIds).forEach(s -> sections.put(s.getId(), s));
    return sections;
  }

  private void publishChanges(Collection<Long> seatIds) {
    events.publishEvent(new SeatsChangedEvent(seatRepository.findChanges(seatIds)));
  }
}
