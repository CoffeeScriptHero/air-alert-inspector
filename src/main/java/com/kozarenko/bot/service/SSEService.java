package com.kozarenko.bot.service;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SSEService {

  private final SSEClientService sseClientService;
  private final SenderService senderService;

  public SSEService(SSEClientService sseClientService, SenderService senderService) {
    this.sseClientService = sseClientService;
    this.senderService = senderService;
  }

  public void connectForUser(long userId, List<Integer> ids) {
    sseClientService.streamEvents().subscribe(
        this::handleEvent,
        error -> System.err.println("ERROR"),
        () -> System.out.println("~~~~~~~ SSE Stream for user is closed")
    );
  }

  private void handleEvent(String event) {
    System.out.println("RECEIVED EVENT: " + event);
  }
}
