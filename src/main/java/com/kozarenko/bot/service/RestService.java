package com.kozarenko.bot.service;

import com.kozarenko.bot.mapper.StateMapper;
import com.kozarenko.bot.model.State;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import static com.kozarenko.bot.util.Constants.API_KEY_HEADER;
import static com.kozarenko.bot.util.Constants.BASE_API_URL;

@Service
public class RestService {

  private static final String STATES = "/api/states";

  private final RestTemplate restTemplate;

  @Value("${api.key}")
  private String apiKey;

  public RestService(RestTemplateBuilder restTemplateBuilder) {
    this.restTemplate = restTemplateBuilder.build();
  }

  private RequestEntity<Void> createRequestEntity(String path) throws URISyntaxException {
    HttpHeaders headers = new HttpHeaders();
    headers.set(API_KEY_HEADER, apiKey);

    return RequestEntity.get(new URI(BASE_API_URL + path))
        .headers(headers)
        .build();
  }

  public List<State> getStates() throws URISyntaxException {
    StateMapper stateMapper = new StateMapper();

    ResponseEntity<String> response =
        restTemplate.exchange(createRequestEntity(STATES), String.class);

    try {
      return stateMapper.statesFromJson(response.getBody());
    } catch (Exception ex) {
      ex.printStackTrace();
    }

    return List.of();
  }
}
