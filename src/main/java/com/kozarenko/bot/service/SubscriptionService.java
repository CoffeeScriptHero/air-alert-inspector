package com.kozarenko.bot.service;

import com.kozarenko.bot.component.StateDataProvider;
import com.kozarenko.bot.model.Subscription;
import com.kozarenko.bot.repository.SubscriptionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class SubscriptionService {

  private final SubscriptionRepository subscriptionRepository;

  public SubscriptionService(SubscriptionRepository subscriptionRepository) {
    this.subscriptionRepository = subscriptionRepository;
  }

  public boolean existsByChatId(Long id) {
    return subscriptionRepository.existsByChatId(id);
  }

  public boolean isSubscriptionExists(Long chatId, Integer stateId) {
    return subscriptionRepository.existsByChatIdAndStateId(chatId, stateId);
  }

  public void saveNewChatSubscriptions(Long chatId) {
    subscriptionRepository.saveAll(
        IntStream.rangeClosed(StateDataProvider.STATE_FIRST_ID, StateDataProvider.STATE_LAST_ID)
            .mapToObj(stateId -> new Subscription(chatId, stateId))
            .collect(Collectors.toList())
    );
  }

  public void saveSubscription(Long chatId, Integer stateId) {
    subscriptionRepository.save(new Subscription(chatId, stateId));
  }

  public void deleteSubscription(Long chatId, Integer stateId) {
    subscriptionRepository.deleteByChatIdAndStateId(chatId, stateId);
  }

  public List<Subscription> getSubscriptions(Long chatId) {
    return subscriptionRepository.getSubscriptionsByChatId(chatId);
  }
}
