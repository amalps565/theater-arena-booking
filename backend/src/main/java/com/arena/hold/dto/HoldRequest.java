package com.arena.hold.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record HoldRequest(@NotEmpty @Size(max = 8) List<@NotNull Long> seatIds) {}
