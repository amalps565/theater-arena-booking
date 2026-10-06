package com.arena.hold;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.arena.hold.dto.HoldResponse;
import com.arena.hold.exception.SeatUnavailableException;
import com.arena.hold.service.HoldService;
import com.arena.order.dto.OrderResponse;
import com.arena.order.exception.HoldExpiredException;
import com.arena.order.service.CheckoutService;
import com.arena.support.IntegrationTestSupport;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class HoldExpiryTest extends IntegrationTestSupport {

  private static final long SEAT_ID = 305L;
  private static final Duration PAST_EXPIRY = Duration.ofSeconds(61);

  @Autowired private HoldService holdService;
  @Autowired private CheckoutService checkoutService;

  @Test
  void anExpiredHoldIsFreeForAnotherCustomerBeforeTheJobRuns() {
    holdService.hold(UUID.randomUUID(), List.of(SEAT_ID));
    clock.advance(PAST_EXPIRY);
    UUID second = UUID.randomUUID();

    holdService.hold(second, List.of(SEAT_ID));

    assertThat(holdService.liveHolds(second)).extracting("seatId").containsExactly(SEAT_ID);
  }

  @Test
  void theExpiryJobReleasesAbandonedHolds() {
    holdService.hold(UUID.randomUUID(), List.of(SEAT_ID));
    clock.advance(PAST_EXPIRY);

    int released = holdService.releaseExpiredHolds();

    assertThat(released).isEqualTo(1);
    assertThat(countSeatsWithStatus(SEAT_ID, "AVAILABLE")).isEqualTo(1);
  }

  @Test
  void theExpiryJobLeavesLiveHoldsAlone() {
    holdService.hold(UUID.randomUUID(), List.of(SEAT_ID));
    clock.advance(Duration.ofSeconds(30));

    int released = holdService.releaseExpiredHolds();

    assertThat(released).isZero();
    assertThat(countSeatsWithStatus(SEAT_ID, "HELD")).isEqualTo(1);
  }

  @Test
  void checkoutBuysHeldSeatsAtTheFrozenPrice() {
    UUID customer = UUID.randomUUID();
    HoldResponse hold = holdService.hold(customer, List.of(SEAT_ID, SEAT_ID + 1));
    clock.advance(Duration.ofSeconds(30));

    OrderResponse order = checkoutService.checkout(customer);

    long heldTotal = hold.holds().stream().mapToLong(h -> h.priceCents()).sum();
    assertThat(order.totalCents()).isEqualTo(heldTotal);
    assertThat(order.items()).hasSize(2);
    assertThat(countSeatsWithStatus(SEAT_ID, "SOLD")).isEqualTo(1);
  }

  @Test
  void checkoutAfterTheHoldExpiredFailsAndBuysNothing() {
    UUID customer = UUID.randomUUID();
    holdService.hold(customer, List.of(SEAT_ID));
    clock.advance(PAST_EXPIRY);

    assertThrows(HoldExpiredException.class, () -> checkoutService.checkout(customer));

    assertThat(countSeatsWithStatus(SEAT_ID, "SOLD")).isZero();
  }

  @Test
  void aSoldSeatCanNeverBeHeldAgain() {
    UUID buyer = UUID.randomUUID();
    holdService.hold(buyer, List.of(SEAT_ID));
    checkoutService.checkout(buyer);
    clock.advance(Duration.ofHours(1));

    assertThrows(
        SeatUnavailableException.class,
        () -> holdService.hold(UUID.randomUUID(), List.of(SEAT_ID)));
  }
}
