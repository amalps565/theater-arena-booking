package com.arena.hold.dto;

import java.time.Instant;
import java.util.List;

public record HoldResponse(Instant expiresAt, List<HeldSeat> holds) {}
