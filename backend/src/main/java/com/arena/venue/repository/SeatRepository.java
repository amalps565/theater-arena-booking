package com.arena.venue.repository;

import com.arena.venue.entity.Seat;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SeatRepository extends JpaRepository<Seat, Long> {

  @Query(
      """
      select new com.arena.venue.repository.SeatRow(
        s.id, s.sectionId, s.x, s.y, s.rowLabel, s.rowIndex, s.seatNumber,
        s.status, s.holdExpiresAt, s.seq)
      from Seat s
      order by s.id
      """)
  List<SeatRow> findAllRows();

  @Query(
      """
      select new com.arena.venue.repository.SectionOccupancy(s.sectionId, count(s))
      from Seat s
      where s.status = com.arena.venue.entity.SeatStatus.SOLD
         or (s.status = com.arena.venue.entity.SeatStatus.HELD and s.holdExpiresAt > :now)
      group by s.sectionId
      """)
  List<SectionOccupancy> countOccupiedBySection(@Param("now") Instant now);

  @Query(
      """
      select count(s)
      from Seat s
      where s.sectionId = :sectionId
        and (s.status = com.arena.venue.entity.SeatStatus.SOLD
          or (s.status = com.arena.venue.entity.SeatStatus.HELD and s.holdExpiresAt > :now))
      """)
  long countOccupiedInSection(@Param("sectionId") long sectionId, @Param("now") Instant now);
}
