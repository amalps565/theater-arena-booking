package com.arena.ws;

import com.arena.config.WebSocketConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VenueBroadcaster {

  private final SimpMessagingTemplate messagingTemplate;

  public void send(Object message) {
    messagingTemplate.convertAndSend(WebSocketConfig.VENUE_TOPIC, message);
  }
}
