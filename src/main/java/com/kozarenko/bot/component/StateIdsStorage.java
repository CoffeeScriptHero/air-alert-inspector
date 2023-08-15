package com.kozarenko.bot.component;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class StateIdsStorage {

  private final List<Integer> ids = new ArrayList<>();

  public List<Integer> getIds() {
    return ids;
  }

  public void addId(Integer id) {
    ids.add(id);
  }
}
