package com.arena.auth.service;

import com.arena.auth.entity.AppUser;
import com.arena.auth.repository.AppUserRepository;
import java.time.Clock;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DemoUserSeeder implements ApplicationRunner {

  public static final String DEMO_PASSWORD = "arena123";

  private static final List<String[]> DEMO_USERS =
      List.of(
          new String[] {"alice", "Alice Anders"},
          new String[] {"bob", "Bob Brown"},
          new String[] {"carol", "Carol Chen"});

  private final AppUserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final Clock clock;

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    if (userRepository.count() > 0) {
      return;
    }
    String hash = passwordEncoder.encode(DEMO_PASSWORD);
    for (String[] user : DEMO_USERS) {
      userRepository.save(new AppUser(user[0], user[1], hash, clock.instant()));
    }
    log.info("Seeded {} demo accounts", DEMO_USERS.size());
  }
}
