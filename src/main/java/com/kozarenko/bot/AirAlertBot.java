package com.kozarenko.bot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

@Component
public class AirAlertBot extends TelegramLongPollingBot {

  @Value("${bot.token}")
  private String token;

  @Value("${bot.username}")
  private String username;

  @Override
  public void onUpdateReceived(Update update) {
    if (update.hasMessage() && update.getMessage().hasText()) {
      Message message = update.getMessage();

      switch (message.getText()) {
        case "/start":
          onStart(message.getChat());
          break;
        default:
          try {
            execute(SendMessage.builder().text("Wow!").chatId(message.getChatId()).build());
          } catch (TelegramApiException e) {
            throw new RuntimeException(e);
          }
      }

    } else if (update.hasCallbackQuery()) {
      onCallback(update.getCallbackQuery());
    }
  }

  @Override
  public String getBotUsername() {
    return username;
  }

  public void onStart(Chat chat) {
    String greetingsText = chat.getType().equals("group") || chat.getType().equals("supergroup")
            ? "Вітаю всіх учасників групи " + chat.getTitle() + "!"
            : "Вітаю, " + chat.getFirstName() + " " + chat.getLastName() + "!";

    SendMessage sendMessage = SendMessage.builder()
            .chatId(chat.getId())
            .text(greetingsText
                    + " Я бот, що повідомляє про активацію повітряної тривоги в містах та областях.")
            .replyMarkup(buildMenu())
            .build();

    try {
      execute(sendMessage);
    } catch (TelegramApiException ex) {
      ex.printStackTrace();
    }
  }

  private InlineKeyboardMarkup buildMenu() {
    InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
    List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

    List<InlineKeyboardButton> row1 = new ArrayList<>();
    List<InlineKeyboardButton> row2 = new ArrayList<>();

    row1.add(InlineKeyboardButton.builder().text("\uD83D\uDDFA️ Мапа тривог").callbackData("map").build());
    row1.add(InlineKeyboardButton.builder().text("Кнопка 2").callbackData("button2").build());

    row2.add(InlineKeyboardButton.builder().text("Кнопка 3").callbackData("button3").build());
    row2.add(InlineKeyboardButton.builder().text("Кнопка 4").callbackData("button4").build());

    keyboard.add(row1);
    keyboard.add(row2);

    inlineKeyboardMarkup.setKeyboard(keyboard);

    return inlineKeyboardMarkup;
  }

  public void onCallback(CallbackQuery callbackQuery) {

    switch (callbackQuery.getData()) {
      case "map":

    }

    AnswerCallbackQuery answer = new AnswerCallbackQuery();
    answer.setCallbackQueryId(callbackQuery.getId());
    answer.setText(callbackQuery.getData());

    try {
      execute(answer);
    } catch (TelegramApiException e) {
      throw new RuntimeException(e);
    }
  }
}
