package com.arena.venue.controller;

import com.arena.venue.dto.VenueResponse;
import com.arena.venue.service.VenueService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/venue")
@RequiredArgsConstructor
public class VenueController {

  private final VenueService venueService;

  @GetMapping
  public VenueResponse venue() {
    return venueService.snapshot();
  }
}
