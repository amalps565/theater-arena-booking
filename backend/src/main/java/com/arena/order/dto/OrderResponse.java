package com.arena.order.dto;

import com.arena.order.entity.CustomerOrder;
import java.util.List;

public record OrderResponse(long orderId, long totalCents, List<OrderLine> items) {

  public static OrderResponse of(CustomerOrder order) {
    List<OrderLine> lines =
        order.getItems().stream()
            .map(item -> new OrderLine(item.getSeatId(), item.getPriceCents()))
            .toList();
    return new OrderResponse(order.getId(), order.getTotalCents(), lines);
  }

  public record OrderLine(long seatId, long priceCents) {}
}
