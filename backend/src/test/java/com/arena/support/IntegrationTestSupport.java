package com.arena.support;

import java.time.Instant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest(properties = "arena.holds.expiry-interval-ms=3600000")
@Import(IntegrationTestSupport.TestClockConfig.class)
public abstract class IntegrationTestSupport {

  protected static final Instant START = Instant.parse("2026-10-06T12:00:00Z");

  @Autowired protected MutableClock clock;
  @Autowired protected JdbcTemplate jdbcTemplate;

  @BeforeEach
  void resetClock() {
    clock.set(START);
  }

  @AfterEach
  void resetSeats() {
    jdbcTemplate.update(
        "update seat set status = 'AVAILABLE', held_by = null, hold_expires_at = null,"
            + " held_price_cents = null");
  }

  protected int countSeatsWithStatus(long seatId, String status) {
    Integer count =
        jdbcTemplate.queryForObject(
            "select count(*) from seat where id = ? and status = ?", Integer.class, seatId, status);
    return count == null ? 0 : count;
  }

  @TestConfiguration
  public static class TestClockConfig {

    @Bean
    @Primary
    public MutableClock mutableClock() {
      return new MutableClock(START);
    }
  }
}
