package com.kozarenko.bot.dto;

import com.kozarenko.bot.model.State;

import java.util.List;

public class StatesDto {

  private List<State> states;
  private String lastUpdate;

  public StatesDto() {}

  public List<State> getStates() {
    return states;
  }

  public void setStates(List<State> states) {
    this.states = states;
  }

  public String getLastUpdate() {
    return lastUpdate;
  }

  public void setLastUpdate(String lastUpdate) {
    this.lastUpdate = lastUpdate;
  }
}
