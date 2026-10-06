package com.arena.pricing;

import com.arena.venue.entity.Section;
import com.arena.venue.repository.SeatRepository;
import com.arena.venue.repository.SectionRepository;
import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SectionPriceService {

  private final SectionRepository sectionRepository;
  private final SeatRepository seatRepository;
  private final PricingService pricingService;
  private final Clock clock;

  private final Map<Long, DemandBand> publishedBands = new ConcurrentHashMap<>();
  private final Map<Long, Long> priceSeqs = new ConcurrentHashMap<>();

  public long priceSeq(long sectionId) {
    return priceSeqs.getOrDefault(sectionId, 0L);
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
  public synchronized Optional<SectionPrices> pricesIfBandChanged(long sectionId) {
    Optional<Section> found = sectionRepository.findById(sectionId);
    if (found.isEmpty()) {
      return Optional.empty();
    }
    Section section = found.get();
    long occupied = seatRepository.countOccupiedInSection(sectionId, clock.instant());
    DemandBand band = pricingService.band(section, occupied);
    DemandBand previous = publishedBands.getOrDefault(sectionId, DemandBand.NORMAL);
    if (band == previous) {
      return Optional.empty();
    }
    publishedBands.put(sectionId, band);
    long seq = priceSeqs.merge(sectionId, 1L, Long::sum);
    List<long[]> prices =
        seatRepository.findPositionsInSection(sectionId).stream()
            .map(
                seat ->
                    new long[] {
                      seat.id(), pricingService.priceCents(section, seat.rowIndex(), occupied)
                    })
            .toList();
    return Optional.of(new SectionPrices(sectionId, seq, prices));
  }
}
