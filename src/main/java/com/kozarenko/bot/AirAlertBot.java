package com.kozarenko.bot;

import com.kozarenko.bot.service.RestService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.security.auth.callback.Callback;
import java.util.ArrayList;
import java.util.List;

@Component
public class AirAlertBot extends TelegramLongPollingBot {

  private RestService restService;

  @Value("${bot.token}")
  private String token;

  @Value("${bot.username}")
  private String username;

  @Value("${bot.id}")
  private Long id;

  public AirAlertBot(RestService restService) {
    this.restService = restService;
  }

  @Override
  public void onUpdateReceived(Update update) {
    if (update.hasMessage()) {
      Message message = update.getMessage();

      if (botAddedToGroup(message)) {
        return;
      }

      if (update.getMessage().hasText()) {
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
      }
    } else if (update.hasCallbackQuery()) {
      onCallback(update.getCallbackQuery());
    }
  }

  @Override
  public String getBotUsername() {
    return username;
  }

  @Override
  public String getBotToken() {
    return token;
  }

  public Long getId() {
    return id;
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

  private boolean botAddedToGroup(Message message) {
    if (message.getNewChatMembers() != null && message.getNewChatMembers().size() > 0) {
      if (message.getNewChatMembers().stream().anyMatch(u -> u.getId().equals(getId()))) {
        onStart(message.getChat());
        return true;
      }
    }
    return false;
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
        try {
          execute(SendPhoto.builder()
                  .photo(new InputFile("https://alerts.com.ua/map.png"))
                  .chatId(callbackQuery.getMessage().getChatId())
                  .build());
        } catch (TelegramApiException ex) {
          ex.printStackTrace();
        }
    }
//
//    AnswerCallbackQuery answer = new AnswerCallbackQuery();
//    answer.setCallbackQueryId(callbackQuery.getId());
//    answer.setText(callbackQuery.getData());
//
//    try {
//      execute(answer);
//    } catch (TelegramApiException e) {
//      throw new RuntimeException(e);
//    }
  }
}
