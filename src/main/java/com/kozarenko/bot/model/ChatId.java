package com.kozarenko.bot.model;

import jakarta.persistence.*;

import java.util.Set;

@Entity
@Table(name = "chat")
public class ChatId {

  @Id
  @Column(name = "id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "chat_id")
  private Long chatId;

  @ManyToMany
  @JoinTable(
      name = "chat_state",
      joinColumns = @JoinColumn(name = "chat_id"),
      inverseJoinColumns = @JoinColumn(name = "state_id")
  )
  private Set<StateId> subscriptions;

  public ChatId() {}

  public ChatId(Long chatId) {
    this.chatId = chatId;
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

  public Set<StateId> getSubscriptions() {
    return subscriptions;
  }

  public void setSubscriptions(Set<StateId> subscriptions) {
    this.subscriptions = subscriptions;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ChatId chatId = (ChatId) o;

    if (!id.equals(chatId.id)) return false;
    return this.chatId.equals(chatId.chatId);
  }

  @Override
  public int hashCode() {
    int result = id.hashCode();
    result = 31 * result + chatId.hashCode();
    return result;
  }
}
