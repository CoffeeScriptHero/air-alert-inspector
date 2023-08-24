package com.kozarenko.bot.service;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Collections;
import java.util.Optional;

@Service
public class UpdateService {

  private static final String COMMAND_START = "/start";
  private static final String COMMAND_MENU = "/menu";
  private static final String COMMAND_HELP = "/help";

  private final SubscriptionService subscriptionService;
  private final SenderService senderService;
  private final CallbackService callbackService;

  public UpdateService(SenderService senderService,
                       SubscriptionService subscriptionService,
                       CallbackService callbackService) {
    this.senderService = senderService;
    this.subscriptionService = subscriptionService;
    this.callbackService = callbackService;
  }

  public void handleUpdate(Update update, long botId) {

    if (update.hasMessage()) {
      Message message = update.getMessage();

      if (botAddedToGroup(message, botId)) {
        onNewUser(message.getChat());
        return;
      }

      if (update.getMessage().hasText()) {
        Chat chat = message.getChat();

        switch (message.getText()) {
          case COMMAND_START -> onNewUser(chat);
          case COMMAND_MENU -> senderService.sendMenu(chat.getId());
          case COMMAND_HELP -> senderService.sendHelpMessage(chat.getId());
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

  private void onNewUser(Chat chat) {
    generateSubscriptions(chat.getId());
    senderService.sendStartMessage(chat);
    senderService.sendMenu(chat.getId());
  }

  private boolean botAddedToGroup(Message message, long botId) {
    return Optional.ofNullable(message.getNewChatMembers())
        .orElseGet(Collections::emptyList)
        .stream()
        .anyMatch(u -> u.getId().equals(botId));
  }
}