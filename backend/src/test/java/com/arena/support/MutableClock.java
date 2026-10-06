package com.arena.support;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicReference;

public class MutableClock extends Clock {

  private final AtomicReference<Instant> current;

  public MutableClock(Instant start) {
    this.current = new AtomicReference<>(start);
  }

  public void advance(Duration duration) {
    current.updateAndGet(instant -> instant.plus(duration));
  }

  public void set(Instant instant) {
    current.set(instant);
  }

  @Override
  public ZoneId getZone() {
    return ZoneOffset.UTC;
  }

  @Override
  public Clock withZone(ZoneId zone) {
    return this;
  }

  @Override
  public Instant instant() {
    return current.get();
  }
}
