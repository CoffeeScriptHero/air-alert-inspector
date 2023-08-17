package com.kozarenko.bot.model;

public class State {

  private String name;
  private int id;

  public State() {}

  public State(String name, int id) {
    this.name = name;
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  @Override
  public String toString() {
    return String.format("State{name='%s', id=%d}", name, id);
  }
}
