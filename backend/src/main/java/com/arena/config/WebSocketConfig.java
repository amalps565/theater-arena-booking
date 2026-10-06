package com.arena.config;

import jakarta.annotation.PreDestroy;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  public static final String VENUE_TOPIC = "/topic/venue";
  private static final long HEARTBEAT_MS = 10_000;

  private static final String BEARER_PREFIX = "Bearer ";

  private final ThreadPoolTaskScheduler heartbeatScheduler = heartbeatScheduler();
  private final JwtDecoder jwtDecoder;

  public WebSocketConfig(JwtDecoder jwtDecoder) {
    this.jwtDecoder = jwtDecoder;
  }

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry
        .addEndpoint("/ws")
        .setAllowedOriginPatterns("http://localhost:*", "http://127.0.0.1:*");
  }

  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {
    registry
        .enableSimpleBroker("/topic")
        .setHeartbeatValue(new long[] {HEARTBEAT_MS, HEARTBEAT_MS})
        .setTaskScheduler(heartbeatScheduler);
    registry.setApplicationDestinationPrefixes("/app");
  }

  @Override
  public void configureClientInboundChannel(ChannelRegistration registration) {
    registration.interceptors(new SignedInSubscriberInterceptor(jwtDecoder));
  }

  @PreDestroy
  void stopHeartbeats() {
    heartbeatScheduler.shutdown();
  }

  private static ThreadPoolTaskScheduler heartbeatScheduler() {
    ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
    scheduler.setPoolSize(1);
    scheduler.setThreadNamePrefix("ws-heartbeat-");
    scheduler.initialize();
    return scheduler;
  }

  static final class SignedInSubscriberInterceptor implements ChannelInterceptor {

    private final JwtDecoder jwtDecoder;

    SignedInSubscriberInterceptor(JwtDecoder jwtDecoder) {
      this.jwtDecoder = jwtDecoder;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
      StompHeaderAccessor accessor =
          MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
      if (accessor == null) {
        return message;
      }
      StompCommand command = accessor.getCommand();
      if (StompCommand.CONNECT.equals(command)) {
        accessor.setUser(new JwtAuthenticationToken(authenticate(message, accessor)));
      }
      if (StompCommand.SEND.equals(command)) {
        throw new MessageDeliveryException(message, "Clients may only subscribe.");
      }
      if (StompCommand.SUBSCRIBE.equals(command)
          && !VENUE_TOPIC.equals(accessor.getDestination())) {
        throw new MessageDeliveryException(message, "Only " + VENUE_TOPIC + " can be subscribed.");
      }
      return message;
    }

    private Jwt authenticate(Message<?> message, StompHeaderAccessor accessor) {
      String header = accessor.getFirstNativeHeader("Authorization");
      if (header == null || !header.startsWith(BEARER_PREFIX)) {
        throw new MessageDeliveryException(message, "Sign in before connecting.");
      }
      try {
        return jwtDecoder.decode(header.substring(BEARER_PREFIX.length()));
      } catch (JwtException invalid) {
        throw new MessageDeliveryException(message, "Your sign-in has expired or is invalid.");
      }
    }
  }
}
