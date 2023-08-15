package com.kozarenko.bot.runner;

import com.kozarenko.bot.component.StateIdsStorage;
import com.kozarenko.bot.service.SSEClientService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class SSEClientRunner implements CommandLineRunner {

  private final SSEClientService sseClientService;
  private final StateIdsStorage stateIdsStorage;

  public SSEClientRunner(SSEClientService sseClientService, StateIdsStorage stateIdsStorage) {
    this.sseClientService = sseClientService;
    this.stateIdsStorage = stateIdsStorage;
  }

  private void listenEndpoint(int stateId) {
    sseClientService.streamEvents(stateId).subscribe(
        event -> System.out.println("~~~~~~~~ RECEIVED EVENT: " + event),
        error -> System.err.println("~~~~~~~~ ERROR OCCURED: " + error),
        () -> System.out.println("~~~~~~~~ SSE STREAM CLOSED.")
    );
  }

  @Override
  public void run(String... args) throws Exception {
    stateIdsStorage.getIds().forEach(this::listenEndpoint);
  }
}
