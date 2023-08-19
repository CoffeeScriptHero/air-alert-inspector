package com.kozarenko.bot.model;

import jakarta.persistence.*;

@Entity
@Table(name = "subscription")
public class Subscription {

  @Id
  @Column(name = "id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "chat_id")
  private Long chatId;

  @Column(name = "state_id")
  private Integer stateId;

  public Subscription() {}

  public Subscription(Long chatId, Integer stateId) {
    this.chatId = chatId;
    this.stateId = stateId;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getChatId() {
    return chatId;
  }

  public void setChatId(Long chatId) {
    this.chatId = chatId;
  }

  public Integer getStateId() {
    return stateId;
  }

  public void setStateId(Integer stateId) {
    this.stateId = stateId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Subscription subscription = (Subscription) o;

    if (!id.equals(subscription.id)) return false;
    if (!chatId.equals(subscription.chatId)) return false;
    return stateId.equals(subscription.stateId);
  }

  @Override
  public int hashCode() {
    int result = id.hashCode();
    result = 31 * result + chatId.hashCode();
    result = 31 * result + stateId.hashCode();
    return result;
  }
}
