package com.kozarenko.bot.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kozarenko.bot.mapper.StateMapper;
import com.kozarenko.bot.model.State;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class SSEService {

  private static final String NULL_STR = "null";
  private static final String SSE_CLOSED_MESSAGE = "Global SSE Stream for is closed.";

  private final SSEClientService sseClientService;
  private final SubscriptionService subscriptionService;
  private final SenderService senderService;
  private final StateMapper stateMapper;

  public SSEService(SSEClientService sseClientService,
                    SubscriptionService subscriptionService,
                    SenderService senderService,
                    StateMapper stateMapper) {
    this.sseClientService = sseClientService;
    this.subscriptionService = subscriptionService;
    this.senderService = senderService;
    this.stateMapper = stateMapper;
  }

  @PostConstruct
  public void init() {
    sseClientService.streamEvents().subscribe(
        this::handleData,
        System.err::println,
        () -> System.out.println(SSE_CLOSED_MESSAGE)
    );
  }

  private void handleData(String data) {
    if (data == null || data.equals(NULL_STR)) {
      return;
    }

    try {
      State state = stateMapper.stateFromJson(data);
      List<Long> subscribedChats = subscriptionService.getChatsSubscribedToState(state.getId());
      List<CompletableFuture<Void>> futures = new ArrayList<>();

      for (Long chatId : subscribedChats) {
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> senderService.sendAlertMessage(chatId, state));
        futures.add(future);
      }

      CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    } catch (JsonProcessingException ex) {
      ex.printStackTrace();
    }
  }
}
