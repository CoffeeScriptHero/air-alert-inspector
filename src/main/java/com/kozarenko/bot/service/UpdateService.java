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

  private final SSEService sseService;
  private final ChatIdService chatIdService;
  private final SenderService senderService;
  private final CallbackService callbackService;

  public UpdateService(SenderService senderService,
                       ChatIdService chatIdService,
                       CallbackService callbackService,
                       SSEService sseService) {
    this.senderService = senderService;
    this.chatIdService = chatIdService;
    this.callbackService = callbackService;
    this.sseService = sseService;
  }

  public void handleUpdate(Update update, long botId) {

    if (update.hasMessage()) {
      Message message = update.getMessage();

      if (botAddedToGroup(message, botId)) {
        saveChat(message.getChat().getId());
        senderService.sendStartMessage(message.getChat());
        senderService.sendMenu(message.getChat().getId());
        return;
      }

      if (update.getMessage().hasText()) {
        Chat chat = message.getChat();

        switch (message.getText()) {
          case COMMAND_START -> {
            saveChat(message.getChat().getId());
            senderService.sendStartMessage(chat);
            senderService.sendMenu(chat.getId());
//            sseService.connectForUser(update.getMessage().getFrom().getId(), List.of(4, 11, 25));
          }
          case COMMAND_MENU -> senderService.sendMenu(chat.getId());
        }
      }
    } else if (update.hasCallbackQuery()) {
      callbackService.handleCallback(update.getCallbackQuery());
    }
  }

  private void saveChat(Long id) {
    if (!chatIdService.existsByChatId(id)) {
      chatIdService.save(id);
    }
  }

  private boolean botAddedToGroup(Message message, long botId) {
    return Optional.ofNullable(message.getNewChatMembers())
        .orElseGet(Collections::emptyList)
        .stream()
        .anyMatch(u -> u.getId().equals(botId));
  }
}