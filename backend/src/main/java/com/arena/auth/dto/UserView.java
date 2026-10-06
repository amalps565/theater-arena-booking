package com.arena.auth.dto;

import com.arena.auth.entity.AppUser;
import java.util.UUID;

public record UserView(UUID id, String username, String displayName) {

  public static UserView of(AppUser user) {
    return new UserView(user.getId(), user.getUsername(), user.getDisplayName());
  }
}
