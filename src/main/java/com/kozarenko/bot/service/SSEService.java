package com.kozarenko.bot.service;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SSEService {

  private final SSEClientService sseClientService;

  public SSEService(SSEClientService sseClientService) {
    this.sseClientService = sseClientService;
  }

  public void connectForUser(long userId, List<Integer> ids) {
    ids.forEach(id -> sseClientService.streamEvents(id).subscribe(
        event -> {
          System.out.println("RECEIVED EVENT FOR USER " + userId + ": " + event);
        },
        error -> System.err.println("ERROR"),
        () -> System.out.println("~~~~~~~ SSE Stream for user is closed")
    ));
  }
}
