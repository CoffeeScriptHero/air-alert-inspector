package com.kozarenko.bot.service;

import com.kozarenko.bot.model.Subscription;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class UpdateService {

  private static final String COMMAND_START = "/start";
  private static final String COMMAND_MENU = "/menu";

  private final SSEService sseService;
  private final SubscriptionService subscriptionService;
  private final SenderService senderService;
  private final CallbackService callbackService;

  public UpdateService(SenderService senderService,
                       SubscriptionService subscriptionService,
                       CallbackService callbackService,
                       SSEService sseService) {
    this.senderService = senderService;
    this.subscriptionService = subscriptionService;
    this.callbackService = callbackService;
    this.sseService = sseService;
  }

  public void handleUpdate(Update update, long botId) {

    if (update.hasMessage()) {
      Message message = update.getMessage();

      if (botAddedToGroup(message, botId)) {
        generateSubscriptions(message.getChat().getId());
        senderService.sendStartMessage(message.getChat());
        senderService.sendMenu(message.getChat().getId());
        return;
      }

      if (update.getMessage().hasText()) {
        Chat chat = message.getChat();

        switch (message.getText()) {
          case COMMAND_START -> {
            generateSubscriptions(chat.getId());
            senderService.sendStartMessage(chat);
            senderService.sendMenu(chat.getId());
            sseService.connectForUser(
                update.getMessage().getFrom().getId(), subscriptionService.retrieveStateIdsForChat(chat.getId())
            );
          }
          case COMMAND_MENU -> senderService.sendMenu(chat.getId());
        }
      }
    } else if (update.hasCallbackQuery()) {
      callbackService.handleCallback(update.getCallbackQuery());
    }
  }

  private void generateSubscriptions(Long chatId) {
    if (!subscriptionService.existsByChatId(chatId)) {
      subscriptionService.subscribeToAllStates(chatId);
    }
  }

  private boolean botAddedToGroup(Message message, long botId) {
    return Optional.ofNullable(message.getNewChatMembers())
        .orElseGet(Collections::emptyList)
        .stream()
        .anyMatch(u -> u.getId().equals(botId));
  }
}