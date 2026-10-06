package com.arena.auth.service;

import com.arena.auth.dto.AuthResponse;
import com.arena.auth.dto.LoginRequest;
import com.arena.auth.dto.RegisterRequest;
import com.arena.auth.dto.UserView;
import com.arena.auth.entity.AppUser;
import com.arena.auth.exception.InvalidCredentialsException;
import com.arena.auth.exception.UsernameTakenException;
import com.arena.auth.repository.AppUserRepository;
import java.time.Clock;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

  private final AppUserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final TokenService tokenService;
  private final Clock clock;
  private final String unknownUserHash;

  public AuthService(
      AppUserRepository userRepository,
      PasswordEncoder passwordEncoder,
      TokenService tokenService,
      Clock clock) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.tokenService = tokenService;
    this.clock = clock;
    this.unknownUserHash = passwordEncoder.encode(UUID.randomUUID().toString());
  }

  public static String normalize(String username) {
    return username.trim().toLowerCase(Locale.ROOT);
  }

  @Transactional(readOnly = true)
  public AuthResponse login(LoginRequest request) {
    Optional<AppUser> user = userRepository.findByUsername(normalize(request.username()));
    String hash = user.map(AppUser::getPasswordHash).orElse(unknownUserHash);
    boolean matches = passwordEncoder.matches(request.password(), hash);
    if (user.isEmpty() || !matches) {
      throw new InvalidCredentialsException();
    }
    return tokenService.issue(user.get());
  }

  @Transactional
  public AuthResponse register(RegisterRequest request) {
    String username = normalize(request.username());
    if (userRepository.existsByUsername(username)) {
      throw new UsernameTakenException();
    }
    AppUser user =
        userRepository.save(
            new AppUser(
                username,
                request.displayName().trim(),
                passwordEncoder.encode(request.password()),
                clock.instant()));
    return tokenService.issue(user);
  }

  @Transactional(readOnly = true)
  public UserView me(UUID userId) {
    return userRepository
        .findById(userId)
        .map(UserView::of)
        .orElseThrow(InvalidCredentialsException::new);
  }
}
