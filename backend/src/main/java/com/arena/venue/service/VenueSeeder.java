package com.arena.venue.service;

import com.arena.venue.entity.Tier;
import com.arena.venue.repository.SectionRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class VenueSeeder implements ApplicationRunner {

  static final int ROWS_PER_SECTION = 40;
  static final int SEATS_PER_ROW = 50;
  static final int SEAT_PITCH = 12;
  static final int SECTION_GAP = 60;
  static final int STAGE_DEPTH = 120;

  private static final List<SectionPlan> PLAN =
      List.of(
          new SectionPlan("Left Premium", Tier.PREMIUM, 8_000, 0, 0),
          new SectionPlan("Center VIP", Tier.VIP, 15_000, 1, 0),
          new SectionPlan("Right Premium", Tier.PREMIUM, 8_000, 2, 0),
          new SectionPlan("Left Balcony", Tier.STANDARD, 4_500, 0, 1),
          new SectionPlan("Center Balcony", Tier.STANDARD, 4_500, 1, 1),
          new SectionPlan("Right Balcony", Tier.STANDARD, 4_500, 2, 1));

  private final SectionRepository sectionRepository;
  private final JdbcTemplate jdbcTemplate;

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    if (sectionRepository.count() > 0) {
      return;
    }
    List<Object[]> sections = new ArrayList<>();
    List<Object[]> seats = new ArrayList<>();
    long seatId = 1;
    for (int index = 0; index < PLAN.size(); index++) {
      SectionPlan plan = PLAN.get(index);
      long sectionId = index + 1L;
      sections.add(
          new Object[] {
            sectionId,
            plan.name(),
            plan.tier().name(),
            plan.basePriceCents(),
            ROWS_PER_SECTION,
            SEATS_PER_ROW
          });
      int originX = plan.column() * (SEATS_PER_ROW * SEAT_PITCH + SECTION_GAP);
      int originY = STAGE_DEPTH + plan.tierRow() * (ROWS_PER_SECTION * SEAT_PITCH + SECTION_GAP);
      for (int row = 0; row < ROWS_PER_SECTION; row++) {
        for (int number = 1; number <= SEATS_PER_ROW; number++) {
          int x = originX + (number - 1) * SEAT_PITCH + SEAT_PITCH / 2;
          int y = originY + row * SEAT_PITCH + SEAT_PITCH / 2;
          seats.add(
              new Object[] {seatId++, sectionId, rowLabel(row), row, number, x, y, "AVAILABLE"});
        }
      }
    }
    jdbcTemplate.batchUpdate(
        "insert into section (id, name, tier, base_price_cents, row_count, seats_per_row)"
            + " values (?, ?, ?, ?, ?, ?)",
        sections);
    jdbcTemplate.batchUpdate(
        "insert into seat (id, section_id, row_label, row_index, seat_number, x, y, status)"
            + " values (?, ?, ?, ?, ?, ?, ?, ?)",
        seats);
    log.info("Seeded {} sections and {} seats", sections.size(), seats.size());
  }

  static String rowLabel(int rowIndex) {
    char letter = (char) ('A' + rowIndex % 26);
    return rowIndex < 26 ? String.valueOf(letter) : "A" + letter;
  }

  private record SectionPlan(
      String name, Tier tier, long basePriceCents, int column, int tierRow) {}
}
