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

  private final SenderService senderService;
  private final CallbackService callbackService;

  public UpdateService(SenderService senderService, CallbackService callbackService) {
    this.senderService = senderService;
    this.callbackService = callbackService;
  }

  public void handleUpdate(Update update, long botId) {
    if (update.hasMessage()) {
      Message message = update.getMessage();

      if (botAddedToGroup(message, botId)) {
        senderService.sendStartMessage(message.getChat());
        senderService.sendMenu(message.getChat().getId());
        return;
      }

      if (update.getMessage().hasText()) {
        Chat chat = message.getChat();

        if (message.getText().equals(COMMAND_START)) {
          senderService.sendStartMessage(chat);
          senderService.sendMenu(chat.getId());
        }
      }
    } else if (update.hasCallbackQuery()) {
      callbackService.handleCallback(update.getCallbackQuery());
    }
  }

  private boolean botAddedToGroup(Message message, long botId) {
    return Optional.ofNullable(message.getNewChatMembers())
        .orElseGet(Collections::emptyList)
        .stream()
        .anyMatch(u -> u.getId().equals(botId));
  }
}