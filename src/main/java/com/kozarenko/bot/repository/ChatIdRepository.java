package com.kozarenko.bot.repository;

import com.kozarenko.bot.model.ChatId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatIdRepository extends JpaRepository<ChatId, Long> {

  boolean existsByChatId(Long id);

}
