package com.kozarenko.bot.dto;

import com.kozarenko.bot.model.State;

public class StateDto {

  private State state;
  private String notificationId;

  public StateDto() {}

  public State getState() {
    return state;
  }

  public void setState(State state) {
    this.state = state;
  }

  public String getNotificationId() {
    return notificationId;
  }

  public void setNotificationId(String notificationId) {
    this.notificationId = notificationId;
  }
}
