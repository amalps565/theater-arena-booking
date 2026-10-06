package com.arena.common;

import java.util.UUID;

public final class CustomerIds {

  public static final String HEADER = "X-Customer-Id";

  private CustomerIds() {}

  public static UUID parse(String raw) {
    if (raw == null || raw.isBlank()) {
      throw new InvalidCustomerIdException();
    }
    try {
      return UUID.fromString(raw.trim());
    } catch (IllegalArgumentException invalid) {
      throw new InvalidCustomerIdException();
    }
  }
}
