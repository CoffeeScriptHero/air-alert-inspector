package com.kozarenko.bot.repository;

import com.kozarenko.bot.model.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

  boolean existsByChatId(Long id);

  boolean existsByChatIdAndStateId(Long chatId, Integer stateId);

  List<Subscription> getSubscriptionsByChatId(Long chatId);

  @Transactional
  void deleteByChatIdAndStateId(Long chatId, Integer stateId);
}
