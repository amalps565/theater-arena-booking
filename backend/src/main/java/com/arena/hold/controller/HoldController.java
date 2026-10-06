package com.arena.hold.controller;

import com.arena.common.CustomerIds;
import com.arena.hold.dto.HoldRequest;
import com.arena.hold.dto.HoldResponse;
import com.arena.hold.dto.MyHoldsResponse;
import com.arena.hold.service.HoldService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/holds")
@RequiredArgsConstructor
public class HoldController {

  private final HoldService holdService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public HoldResponse hold(
      @RequestHeader(name = CustomerIds.HEADER, required = false) String customerId,
      @Valid @RequestBody HoldRequest request) {
    return holdService.hold(CustomerIds.parse(customerId), request.seatIds());
  }

  @DeleteMapping("/{seatId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void release(
      @RequestHeader(name = CustomerIds.HEADER, required = false) String customerId,
      @PathVariable long seatId) {
    holdService.release(CustomerIds.parse(customerId), seatId);
  }

  @GetMapping("/me")
  public MyHoldsResponse myHolds(
      @RequestHeader(name = CustomerIds.HEADER, required = false) String customerId) {
    return new MyHoldsResponse(holdService.liveHolds(CustomerIds.parse(customerId)));
  }
}
