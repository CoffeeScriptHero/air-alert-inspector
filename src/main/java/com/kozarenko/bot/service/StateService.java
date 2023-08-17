package com.kozarenko.bot.service;

import com.kozarenko.bot.model.State;
import org.springframework.stereotype.Service;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

@Service
public class StateService {

  private List<State> states;
  private final RestService restService;

  public StateService(RestService restService) {
    this.restService = restService;

    try {
      this.states = restService.getStates();
    } catch (URISyntaxException ex) {
      this.states = getDefaultStates();
      ex.printStackTrace();
    }
  }

  public List<State> getStates() {
    return states;
  }

  private List<State> getDefaultStates() {
    return Arrays.asList(
        new State("Вінницька", 1),
        new State("Волинська", 2),
        new State("Дніпропетровська", 3),
        new State("Донецька", 4),
        new State("Житомирська", 5),
        new State("Закарпатська", 6),
        new State("Запорізька", 7),
        new State("Івано-Франківська", 8),
        new State("Київська", 9),
        new State("Кіровоградська", 10),
        new State("Луганська", 11),
        new State("Львівська", 12),
        new State("Миколаївська", 13),
        new State("Одеська", 14),
        new State("Полтавська", 15),
        new State("Рівненська", 16),
        new State("Сумська", 17),
        new State("Тернопільська", 18),
        new State("Харківська", 19),
        new State("Херсонська", 20),
        new State("Хмельницька", 21),
        new State("Черкаська", 22),
        new State("Чернівецька", 23),
        new State("Чернігівська", 24),
        new State("м. Київ", 25)
    );
  }
}
