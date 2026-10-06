package com.arena.hold.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class HoldExpiryJob {

  private final HoldService holdService;

  @Scheduled(
      initialDelayString = "${arena.holds.expiry-interval-ms:5000}",
      fixedRateString = "${arena.holds.expiry-interval-ms:5000}")
  public void releaseExpiredHolds() {
    int released = holdService.releaseExpiredHolds();
    if (released > 0) {
      log.info("Released {} expired seat holds", released);
    }
  }
}
