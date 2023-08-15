package com.kozarenko.bot;

import com.kozarenko.bot.service.RestService;
import com.kozarenko.bot.service.StateService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.net.URISyntaxException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class AirAlertBot extends TelegramLongPollingBot {

  private static final String DEFAULT_MENU_TEXT = "Обери функцію, що цікавить тебе.";
  private static final String API_MAP_URL = "https://alerts.com.ua/map.png";
  private static final String CALLBACK_MAP = "map";
  private static final String CALLBACK_BACK = "back";
  private static final String CALLBACK_HISTORY = "history";
  private static final String CALLBACK_MANAGE = "manage";

  @Value("${bot.token}")
  private String token;

  @Value("${bot.username}")
  private String username;

  @Value("${bot.id}")
  private long id;

  private RestService restService;

  private StateService stateService;

  private int menuMessageId;

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
        Chat chat = message.getChat();

        switch (message.getText()) {
          case "/start":
            sendStartMessage(chat);
            sendMenu(chat.getId());
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
      handleCallback(update.getCallbackQuery());
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

  public void sendStartMessage(Chat chat) {
    String greetingsText = chat.getType().equals("group") || chat.getType().equals("supergroup")
        ? "Вітаю всіх учасників групи " + chat.getTitle() + "!"
        : "Вітаю, " + chat.getFirstName() + " " + chat.getLastName() + "!";

    SendMessage sendMessage = SendMessage.builder()
        .chatId(chat.getId())
        .text(greetingsText + " Я бот, що повідомляє про активацію повітряної тривоги в містах та областях.")
        .build();

    try {
      execute(sendMessage);
    } catch (TelegramApiException ex) {
      ex.printStackTrace();
    }
  }

  private void sendMenu(Long chatId) {
    sendMenu(chatId, DEFAULT_MENU_TEXT);
  }

  private void sendMenu(Long chatId, String text) {
    try {
      Message msg = execute(SendMessage.builder()
          .chatId(chatId)
          .text(text)
          .replyMarkup(buildMenu())
          .build()
      );

      menuMessageId = msg.getMessageId();

    } catch (TelegramApiException ex) {
      ex.printStackTrace();
    }
  }

  private boolean botAddedToGroup(Message msg) {
    if (msg.getNewChatMembers() != null && msg.getNewChatMembers().size() > 0) {
      if (msg.getNewChatMembers().stream().anyMatch(u -> u.getId().equals(getId()))) {
        sendStartMessage(msg.getChat());
        sendMenu(msg.getChat().getId());
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

    row1.add(InlineKeyboardButton.builder().text("\uD83D\uDDFA️ Мапа тривог").callbackData(CALLBACK_MAP).build());
    row1.add(InlineKeyboardButton.builder().text("Історія тривог").callbackData(CALLBACK_HISTORY).build());

    row2.add(InlineKeyboardButton.builder().text("Менеджер областей").callbackData(CALLBACK_MANAGE).build());
    row2.add(InlineKeyboardButton.builder().text("Кнопка 4").callbackData("button4").build());

    keyboard.add(row1);
    keyboard.add(row2);

    inlineKeyboardMarkup.setKeyboard(keyboard);

    return inlineKeyboardMarkup;
  }

  private InlineKeyboardMarkup buildGoBackMenu() {
    InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
    List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

    keyboard.add(
        Collections.singletonList(
            InlineKeyboardButton.builder()
                .text("⬅ Повернутись назад")
                .callbackData(CALLBACK_BACK)
                .build()
        )
    );

    inlineKeyboardMarkup.setKeyboard(keyboard);
    return inlineKeyboardMarkup;
  }

  public void handleCallback(CallbackQuery callbackQuery) {
    switch (callbackQuery.getData()) {
      case CALLBACK_MAP -> onMapOption(callbackQuery);
      case CALLBACK_BACK-> onBackOption(callbackQuery);
      case CALLBACK_HISTORY -> onHistoryOption(callbackQuery);
    }
  }

  private void onMapOption(CallbackQuery callbackQuery) {
    try {
      execute(
          DeleteMessage.builder()
              .chatId(callbackQuery.getMessage().getChatId())
              .messageId(menuMessageId)
              .build()
      );

      final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");

      Message msg = execute(
          SendPhoto.builder()
              .photo(new InputFile(API_MAP_URL + "?t=" + System.currentTimeMillis()))
              .chatId(callbackQuery.getMessage().getChatId())
              .caption(String.format("Мапа України станом на *%s*", LocalTime.now().format(dtf)))
              .parseMode("Markdown")
              .replyMarkup(buildGoBackMenu())
              .build()
      );

      menuMessageId = msg.getMessageId();
    } catch (TelegramApiException ex) {
      ex.printStackTrace();
    }
  }

  private void onHistoryOption(CallbackQuery callbackQuery) {
    try {
      restService.getAirAlertHistory();
    } catch (URISyntaxException ex) {
      ex.printStackTrace();
    }
  }

  private void onBackOption(CallbackQuery callbackQuery) {
    try {
      execute(
          DeleteMessage.builder()
              .chatId(callbackQuery.getMessage().getChatId())
              .messageId(menuMessageId)
              .build()
      );

      sendMenu(callbackQuery.getMessage().getChatId());
    } catch (TelegramApiException ex) {
      ex.printStackTrace();
    }
  }
}
