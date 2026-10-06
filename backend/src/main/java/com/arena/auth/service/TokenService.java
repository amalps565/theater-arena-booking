package com.arena.auth.service;

import com.arena.auth.dto.AuthResponse;
import com.arena.auth.dto.UserView;
import com.arena.auth.entity.AppUser;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenService {

  public static final Duration TOKEN_LIFETIME = Duration.ofHours(8);
  static final String ISSUER = "theater-arena-booking";

  private final JwtEncoder jwtEncoder;
  private final Clock clock;

  public AuthResponse issue(AppUser user) {
    Instant now = clock.instant();
    Instant expiresAt = now.plus(TOKEN_LIFETIME);
    JwtClaimsSet claims =
        JwtClaimsSet.builder()
            .issuer(ISSUER)
            .subject(user.getId().toString())
            .issuedAt(now)
            .expiresAt(expiresAt)
            .claim("username", user.getUsername())
            .claim("name", user.getDisplayName())
            .build();
    JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
    String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    return new AuthResponse(token, expiresAt, UserView.of(user));
  }
}
