package com.kozarenko.bot.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Service
public class SenderService extends DefaultAbsSender {

  private static final String DEFAULT_MENU_TEXT = "Обери функцію, що цікавить тебе.";
  private static final String GROUP = "group";
  private static final String SUPERGROUP = "supergroup";

  private final KeyboardService keyboardService;

  protected SenderService(@Value("${bot.token}") String token, KeyboardService keyboardService) {
    super(new DefaultBotOptions(), token);
    this.keyboardService = keyboardService;
  }

  public void sendStartMessage(Chat chat) {
    String greetingsText = chat.getType().equals(GROUP) || chat.getType().equals(SUPERGROUP)
        ? "Вітаю всіх учасників групи " + chat.getTitle() + "!"
        : "Вітаю, " + chat.getFirstName() + " " + chat.getLastName() + "!";

    try {
      execute(
          SendMessage.builder()
              .chatId(chat.getId())
              .text(greetingsText + " Я бот, що повідомляє про активацію повітряної тривоги в містах та областях.")
              .build()
      );
    } catch (TelegramApiException ex) {
      ex.printStackTrace();
    }
  }

  public void sendMenu(long chatId) {
    sendMenu(chatId, DEFAULT_MENU_TEXT, keyboardService.buildMainKeyboard());
  }

  public void sendMenu(long chatId, String text, InlineKeyboardMarkup keyboardMarkup) {
    try {
      execute(SendMessage.builder()
          .chatId(chatId)
          .text(text)
          .replyMarkup(keyboardMarkup)
          .build()
      );
    } catch (TelegramApiException ex) {
      ex.printStackTrace();
    }
  }
}
