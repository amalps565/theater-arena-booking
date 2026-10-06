package com.arena.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;

@Slf4j
@Configuration
public class SecurityConfig {

  private static final int MIN_KEY_BYTES = 32;
  private static final String UNAUTHENTICATED_BODY =
      "{\"code\":\"UNAUTHENTICATED\",\"message\":\"Please sign in to continue.\"}";

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable())
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/register")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/venue", "/actuator/health")
                    .permitAll()
                    .requestMatchers("/ws", "/ws/**", "/error")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .oauth2ResourceServer(
            oauth -> oauth.jwt(jwt -> {}).authenticationEntryPoint(unauthenticated()))
        .exceptionHandling(e -> e.authenticationEntryPoint(unauthenticated()));
    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecretKey jwtSigningKey(@Value("${arena.auth.jwt-secret:}") String configured) {
    byte[] bytes = configured.getBytes(StandardCharsets.UTF_8);
    if (bytes.length < MIN_KEY_BYTES) {
      if (!configured.isEmpty()) {
        log.warn(
            "arena.auth.jwt-secret is shorter than {} bytes; using a random key", MIN_KEY_BYTES);
      }
      bytes = new byte[MIN_KEY_BYTES];
      new SecureRandom().nextBytes(bytes);
      log.info("Signing sign-in tokens with a random key; tokens end when the server restarts");
    }
    return new SecretKeySpec(bytes, "HmacSHA256");
  }

  @Bean
  public JwtEncoder jwtEncoder(SecretKey jwtSigningKey) {
    return new NimbusJwtEncoder(new ImmutableSecret<>(jwtSigningKey));
  }

  @Bean
  public JwtDecoder jwtDecoder(SecretKey jwtSigningKey, Clock clock) {
    NimbusJwtDecoder decoder =
        NimbusJwtDecoder.withSecretKey(jwtSigningKey).macAlgorithm(MacAlgorithm.HS256).build();
    JwtTimestampValidator timestamps = new JwtTimestampValidator(Duration.ofSeconds(30));
    timestamps.setClock(clock);
    decoder.setJwtValidator(timestamps);
    return decoder;
  }

  private static AuthenticationEntryPoint unauthenticated() {
    return (request, response, exception) -> {
      response.setStatus(HttpStatus.UNAUTHORIZED.value());
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
      response.getWriter().write(UNAUTHENTICATED_BODY);
    };
  }
}
