package com.arena.ws;

import com.arena.pricing.SectionPriceService;
import com.arena.venue.event.SeatsChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class SeatEventListener {

  private final VenueBroadcaster broadcaster;
  private final SectionPriceService sectionPriceService;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onSeatsChanged(SeatsChangedEvent event) {
    try {
      event.changes().forEach(change -> broadcaster.send(SeatMessage.of(change)));
      event
          .sectionIds()
          .forEach(
              sectionId ->
                  sectionPriceService
                      .pricesIfBandChanged(sectionId)
                      .map(PricesMessage::of)
                      .ifPresent(broadcaster::send));
    } catch (RuntimeException failure) {
      log.error("Could not broadcast {} seat changes", event.changes().size(), failure);
    }
  }
}
