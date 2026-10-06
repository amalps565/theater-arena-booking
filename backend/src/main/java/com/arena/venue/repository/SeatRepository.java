package com.arena.venue.repository;

import com.arena.venue.entity.Seat;
import com.arena.venue.event.SeatChange;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
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

  @Modifying(flushAutomatically = true, clearAutomatically = true)
  @Query(
      """
      update Seat s
         set s.status = com.arena.venue.entity.SeatStatus.HELD,
             s.heldBy = :customerId,
             s.holdExpiresAt = :expiresAt,
             s.heldPriceCents = :priceCents,
             s.seq = s.seq + 1,
             s.version = s.version + 1
       where s.id = :seatId
         and (s.status = com.arena.venue.entity.SeatStatus.AVAILABLE
           or (s.status = com.arena.venue.entity.SeatStatus.HELD and s.holdExpiresAt <= :now))
      """)
  int holdIfFree(
      @Param("seatId") long seatId,
      @Param("customerId") UUID customerId,
      @Param("expiresAt") Instant expiresAt,
      @Param("priceCents") long priceCents,
      @Param("now") Instant now);

  @Modifying(flushAutomatically = true, clearAutomatically = true)
  @Query(
      """
      update Seat s
         set s.status = com.arena.venue.entity.SeatStatus.AVAILABLE,
             s.heldBy = null,
             s.holdExpiresAt = null,
             s.heldPriceCents = null,
             s.seq = s.seq + 1,
             s.version = s.version + 1
       where s.id = :seatId
         and s.status = com.arena.venue.entity.SeatStatus.HELD
         and s.heldBy = :customerId
         and s.holdExpiresAt > :now
      """)
  int releaseIfHeldBy(
      @Param("seatId") long seatId,
      @Param("customerId") UUID customerId,
      @Param("now") Instant now);

  @Query(
      """
      select count(s)
      from Seat s
      where s.heldBy = :customerId
        and s.status = com.arena.venue.entity.SeatStatus.HELD
        and s.holdExpiresAt > :now
      """)
  long countLiveHolds(@Param("customerId") UUID customerId, @Param("now") Instant now);

  @Query(
      """
      select s
      from Seat s
      where s.heldBy = :customerId
        and s.status = com.arena.venue.entity.SeatStatus.HELD
        and s.holdExpiresAt > :now
      order by s.id
      """)
  List<Seat> findLiveHolds(@Param("customerId") UUID customerId, @Param("now") Instant now);

  @Query(
      """
      select new com.arena.venue.event.SeatChange(s.id, s.sectionId, s.status, s.seq)
      from Seat s
      where s.id in :seatIds
      order by s.id
      """)
  List<SeatChange> findChanges(@Param("seatIds") Collection<Long> seatIds);

  @Query(
      """
      select s.id
      from Seat s
      where s.status = com.arena.venue.entity.SeatStatus.HELD
        and s.holdExpiresAt <= :now
      order by s.id
      """)
  List<Long> findExpiredHoldIds(@Param("now") Instant now);

  @Modifying(flushAutomatically = true, clearAutomatically = true)
  @Query(
      """
      update Seat s
         set s.status = com.arena.venue.entity.SeatStatus.AVAILABLE,
             s.heldBy = null,
             s.holdExpiresAt = null,
             s.heldPriceCents = null,
             s.seq = s.seq + 1,
             s.version = s.version + 1
       where s.id in :seatIds
         and s.status = com.arena.venue.entity.SeatStatus.HELD
         and s.holdExpiresAt <= :now
      """)
  int releaseExpired(@Param("seatIds") Collection<Long> seatIds, @Param("now") Instant now);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query(
      """
      select s
      from Seat s
      where s.heldBy = :customerId
        and s.status = com.arena.venue.entity.SeatStatus.HELD
      order by s.id
      """)
  List<Seat> lockHeldBy(@Param("customerId") UUID customerId);
}
