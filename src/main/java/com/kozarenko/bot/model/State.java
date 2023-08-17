package com.kozarenko.bot.model;

public class State {

  private static final String KYIV = "м. Київ";

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

  public String getNameOfState() {
    String space = " ";
    if (!name.equals(KYIV) && name.contains(space)) {
      return name.substring(0, name.indexOf(space));
    }
    return name;
  }

  @Override
  public String toString() {
    return String.format("State{name='%s', id=%d}", name, id);
  }
}
