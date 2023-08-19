package com.kozarenko.bot.service;

import com.kozarenko.bot.model.ChatId;
import com.kozarenko.bot.repository.ChatIdRepository;
import org.springframework.stereotype.Service;

@Service
public class ChatIdService {

  private final ChatIdRepository chatIdRepository;

  public ChatIdService(ChatIdRepository chatIdRepository) {
    this.chatIdRepository = chatIdRepository;
  }

  public boolean existsByChatId(Long id) {
    return chatIdRepository.existsByChatId(id);
  }

  public void save(Long id) {
    chatIdRepository.save(new ChatId(id));
  }
}
