package com.kozarenko.bot.model;

import jakarta.persistence.*;

import java.util.Set;

@Entity
@Table(name = "state")
public class StateId {

  @Id
  @Column(name = "id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "state_id")
  private Integer stateId;

  @ManyToMany(mappedBy = "subscriptions")
  private Set<ChatId> subscribers;

  public StateId() {}

  public StateId(Integer stateId) {
    this.stateId = stateId;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Integer getStateId() {
    return stateId;
  }

  public void setStateId(Integer stateId) {
    this.stateId = stateId;
  }

  public Set<ChatId> getSubscribers() {
    return subscribers;
  }

  public void setSubscribers(Set<ChatId> subscribers) {
    this.subscribers = subscribers;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    StateId state = (StateId) o;

    if (!id.equals(state.id)) return false;
    return stateId.equals(state.stateId);
  }

  @Override
  public int hashCode() {
    int result = id.hashCode();
    result = 31 * result + stateId.hashCode();
    return result;
  }
}
