package com.kozarenko.bot.service;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class RestService {

  private static final String MAP_URL = "https://alerts.com.ua/map.png";
  private final RestTemplate restTemplate;

  public RestService(RestTemplateBuilder restTemplateBuilder) {
    this.restTemplate = restTemplateBuilder.build();
  }

  public String getMap() {
    return restTemplate.getForObject(MAP_URL, String.class);
  }
}
