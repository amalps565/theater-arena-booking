package com.arena.order.service;

import com.arena.order.dto.OrderResponse;
import com.arena.order.entity.CustomerOrder;
import com.arena.order.exception.HoldExpiredException;
import com.arena.order.exception.NothingToCheckOutException;
import com.arena.order.repository.OrderRepository;
import com.arena.venue.entity.Seat;
import com.arena.venue.event.SeatChange;
import com.arena.venue.event.SeatsChangedEvent;
import com.arena.venue.repository.SeatRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CheckoutService {

  private final SeatRepository seatRepository;
  private final OrderRepository orderRepository;
  private final ApplicationEventPublisher events;
  private final Clock clock;

  @Transactional
  public OrderResponse checkout(UUID customerId) {
    Instant now = clock.instant();
    List<Seat> seats = seatRepository.lockHeldBy(customerId);
    if (seats.isEmpty()) {
      throw new NothingToCheckOutException();
    }
    if (seats.stream().anyMatch(seat -> seat.isHoldExpiredAt(now))) {
      throw new HoldExpiredException();
    }
    CustomerOrder order = new CustomerOrder(customerId, now);
    for (Seat seat : seats) {
      order.addItem(seat.getId(), seat.getHeldPriceCents());
      seat.markSold();
    }
    CustomerOrder saved = orderRepository.save(order);
    events.publishEvent(new SeatsChangedEvent(seats.stream().map(SeatChange::of).toList()));
    return OrderResponse.of(saved);
  }
}
