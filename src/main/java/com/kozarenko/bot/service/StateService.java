package com.kozarenko.bot.service;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StateService {

//  private final List<String> states;
  private final RestService restService;

  public StateService(RestService restService) {
    this.restService = restService;
//    this.states = restService.getStates();
  }

}
