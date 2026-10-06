package com.arena.ws;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.arena.hold.exception.SeatUnavailableException;
import com.arena.hold.service.HoldService;
import com.arena.support.IntegrationTestSupport;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

class SeatPublishingTest extends IntegrationTestSupport {

  private static final long SEAT_ID = 410L;

  @Autowired private HoldService holdService;
  @MockitoBean private VenueBroadcaster broadcaster;

  @Test
  void aSuccessfulHoldPublishesThatSeatAsHeld() {
    holdService.hold(UUID.randomUUID(), List.of(SEAT_ID));

    ArgumentCaptor<Object> sent = ArgumentCaptor.forClass(Object.class);
    verify(broadcaster, atLeastOnce()).send(sent.capture());
    assertThat(sent.getAllValues())
        .filteredOn(SeatMessage.class::isInstance)
        .map(SeatMessage.class::cast)
        .singleElement()
        .satisfies(
            message -> {
              assertThat(message.seatId()).isEqualTo(SEAT_ID);
              assertThat(message.status()).isEqualTo("HELD");
            });
  }

  @Test
  void aFailedHoldPublishesNothing() {
    holdService.hold(UUID.randomUUID(), List.of(SEAT_ID));
    clearInvocations(broadcaster);

    assertThrows(
        SeatUnavailableException.class,
        () -> holdService.hold(UUID.randomUUID(), List.of(SEAT_ID + 1, SEAT_ID)));

    verifyNoInteractions(broadcaster);
  }

  @Test
  void releasingAHoldPublishesTheSeatAsAvailable() {
    UUID customer = UUID.randomUUID();
    holdService.hold(customer, List.of(SEAT_ID));
    clearInvocations(broadcaster);

    holdService.release(customer, SEAT_ID);

    verify(broadcaster).send(any(SeatMessage.class));
  }
}
