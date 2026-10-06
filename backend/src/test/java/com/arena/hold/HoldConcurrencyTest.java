package com.arena.hold;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.arena.hold.exception.SeatUnavailableException;
import com.arena.hold.service.HoldService;
import com.arena.support.IntegrationTestSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class HoldConcurrencyTest extends IntegrationTestSupport {

  private static final int CUSTOMERS = 20;
  private static final long SEAT_ID = 101L;

  @Autowired private HoldService holdService;

  @RepeatedTest(5)
  void exactlyOneOfManySimultaneousCustomersHoldsTheSameSeat() throws Exception {
    CountDownLatch startGate = new CountDownLatch(1);
    ExecutorService pool = Executors.newFixedThreadPool(CUSTOMERS);
    List<Future<Outcome>> outcomes = new ArrayList<>();
    for (int i = 0; i < CUSTOMERS; i++) {
      outcomes.add(pool.submit(() -> attemptHold(startGate)));
    }

    startGate.countDown();
    pool.shutdown();
    assertThat(pool.awaitTermination(60, TimeUnit.SECONDS)).isTrue();

    long winners = 0;
    long taken = 0;
    for (Future<Outcome> outcome : outcomes) {
      Outcome result = outcome.get();
      winners += result == Outcome.HELD ? 1 : 0;
      taken += result == Outcome.SEAT_TAKEN ? 1 : 0;
    }
    assertThat(winners).isEqualTo(1);
    assertThat(taken).isEqualTo(CUSTOMERS - 1);
    assertThat(countSeatsWithStatus(SEAT_ID, "HELD")).isEqualTo(1);
  }

  @Test
  void aMultiSeatHoldIsAllOrNothingWhenOneSeatIsTaken() {
    holdService.hold(UUID.randomUUID(), List.of(202L));
    UUID second = UUID.randomUUID();

    assertThrows(
        SeatUnavailableException.class, () -> holdService.hold(second, List.of(201L, 202L, 203L)));

    assertThat(countSeatsWithStatus(201L, "AVAILABLE")).isEqualTo(1);
    assertThat(countSeatsWithStatus(203L, "AVAILABLE")).isEqualTo(1);
    assertThat(holdService.liveHolds(second)).isEmpty();
  }

  private Outcome attemptHold(CountDownLatch startGate) throws InterruptedException {
    startGate.await();
    try {
      holdService.hold(UUID.randomUUID(), List.of(SEAT_ID));
      return Outcome.HELD;
    } catch (SeatUnavailableException taken) {
      return Outcome.SEAT_TAKEN;
    }
  }

  private enum Outcome {
    HELD,
    SEAT_TAKEN
  }
}
