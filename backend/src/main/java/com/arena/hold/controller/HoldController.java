package com.arena.hold.controller;

import com.arena.common.CurrentCustomer;
import com.arena.hold.dto.HoldRequest;
import com.arena.hold.dto.HoldResponse;
import com.arena.hold.dto.MyHoldsResponse;
import com.arena.hold.service.HoldService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
      @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody HoldRequest request) {
    return holdService.hold(CurrentCustomer.id(jwt), request.seatIds());
  }

  @DeleteMapping("/{seatId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void release(@AuthenticationPrincipal Jwt jwt, @PathVariable long seatId) {
    holdService.release(CurrentCustomer.id(jwt), seatId);
  }

  @GetMapping("/me")
  public MyHoldsResponse myHolds(@AuthenticationPrincipal Jwt jwt) {
    return new MyHoldsResponse(holdService.liveHolds(CurrentCustomer.id(jwt)));
  }
}
