package com.arena.order.controller;

import com.arena.common.CustomerIds;
import com.arena.order.dto.OrderResponse;
import com.arena.order.service.CheckoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/checkout")
@RequiredArgsConstructor
public class CheckoutController {

  private final CheckoutService checkoutService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public OrderResponse checkout(
      @RequestHeader(name = CustomerIds.HEADER, required = false) String customerId) {
    return checkoutService.checkout(CustomerIds.parse(customerId));
  }
}
