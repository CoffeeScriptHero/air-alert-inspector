package com.kozarenko.bot.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import static com.kozarenko.bot.util.Constants.API_KEY_HEADER;
import static com.kozarenko.bot.util.Constants.BASE_API_URL;

@Service
public class SSEClientService {

  private static final String API_STATES_LIVE = "/api/states/live";

  @Value("${api.key}")
  private String apiKey;

  private final WebClient webClient;

  public SSEClientService() {
    this.webClient = WebClient.create(BASE_API_URL);
  }

  public Flux<String> streamEvents() {
    return webClient.get()
        .uri(API_STATES_LIVE)
        .header(API_KEY_HEADER, apiKey)
        .retrieve()
        .bodyToFlux(String.class);
  }
}
