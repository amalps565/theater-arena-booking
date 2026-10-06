package com.arena.venue.service;

import com.arena.pricing.PricingService;
import com.arena.pricing.SectionPriceService;
import com.arena.venue.dto.SectionView;
import com.arena.venue.dto.VenueResponse;
import com.arena.venue.entity.Section;
import com.arena.venue.repository.SeatRepository;
import com.arena.venue.repository.SeatRow;
import com.arena.venue.repository.SectionOccupancy;
import com.arena.venue.repository.SectionRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VenueService {

  private final SectionRepository sectionRepository;
  private final SeatRepository seatRepository;
  private final PricingService pricingService;
  private final SectionPriceService sectionPriceService;
  private final Clock clock;

  @Transactional(readOnly = true)
  public VenueResponse snapshot() {
    Instant now = clock.instant();
    List<Section> sections = sectionRepository.findAllByOrderByIdAsc();
    Map<Long, Section> sectionsById =
        sections.stream().collect(Collectors.toMap(Section::getId, Function.identity()));
    Map<Long, Long> occupied =
        seatRepository.countOccupiedBySection(now).stream()
            .collect(
                Collectors.toMap(SectionOccupancy::sectionId, SectionOccupancy::occupiedSeats));
    List<Object[]> seats =
        seatRepository.findAllRows().stream()
            .map(row -> toWire(row, sectionsById.get(row.sectionId()), occupied, now))
            .toList();
    List<SectionView> sectionViews =
        sections.stream()
            .map(
                s ->
                    new SectionView(
                        s.getId(),
                        s.getName(),
                        s.getTier(),
                        s.getBasePriceCents(),
                        sectionPriceService.priceSeq(s.getId())))
            .toList();
    return new VenueResponse(sectionViews, VenueResponse.SEAT_FIELDS, seats);
  }

  private Object[] toWire(SeatRow row, Section section, Map<Long, Long> occupied, Instant now) {
    long price =
        pricingService.priceCents(
            section, row.rowIndex(), occupied.getOrDefault(section.getId(), 0L));
    return new Object[] {
      row.id(),
      row.sectionId(),
      row.x(),
      row.y(),
      row.rowLabel(),
      row.seatNumber(),
      row.statusAt(now).name(),
      price,
      row.seq()
    };
  }
}
