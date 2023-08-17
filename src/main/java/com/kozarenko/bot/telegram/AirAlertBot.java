package com.kozarenko.bot.telegram;

import com.kozarenko.bot.service.KeyboardService;
import com.kozarenko.bot.service.RestService;
import com.kozarenko.bot.component.StateDataProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.net.URISyntaxException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import static com.kozarenko.bot.util.Constants.*;

@Component
public class AirAlertBot extends TelegramLongPollingBot {

  private static final String DEFAULT_MENU_TEXT = "Обери функцію, що цікавить тебе.";
  private static final String API_MAP_URL = "https://alerts.com.ua/map.png";
  private static final String CALLBACK_MAP = "map";
  private static final String CALLBACK_MENU = "menu";
  private static final String CALLBACK_HISTORY = "history";

  @Value("${bot.token}")
  private String token;

  @Value("${bot.username}")
  private String username;

  @Value("${bot.id}")
  private long id;

  private final RestService restService;
  private final KeyboardService keyboardService;
  private final StateDataProvider stateDataProvider;
  private int menuMessageId;
  private long chatId;

  public AirAlertBot(RestService restService, StateDataProvider stateDataProvider, KeyboardService keyboardService) {
    this.restService = restService;
    this.stateDataProvider = stateDataProvider;
    this.keyboardService = keyboardService;
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
            chatId = chat.getId();
            sendStartMessage(chat);
            sendMenu();
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

  private boolean botAddedToGroup(Message msg) {
    if (msg.getNewChatMembers() != null && msg.getNewChatMembers().size() > 0) {
      if (msg.getNewChatMembers().stream().anyMatch(u -> u.getId().equals(getId()))) {
        sendStartMessage(msg.getChat());
        sendMenu(msg.getChat().getId(), DEFAULT_MENU_TEXT, keyboardService.buildMainKeyboard());
        return true;
      }
    }
    return false;
  }

  private void sendMenu() {
    sendMenu(chatId, DEFAULT_MENU_TEXT, keyboardService.buildMainKeyboard());
  }

  private void sendMenu(String text, InlineKeyboardMarkup keyboardMarkup) {
    sendMenu(chatId, text, keyboardMarkup);
  }

  private void sendMenu(long chatId, String text, InlineKeyboardMarkup keyboardMarkup) {
    try {
      Message msg = execute(SendMessage.builder()
          .chatId(chatId)
          .text(text)
          .replyMarkup(keyboardMarkup)
          .build()
      );

      menuMessageId = msg.getMessageId();

    } catch (TelegramApiException ex) {
      ex.printStackTrace();
    }
  }

  private DeleteMessage buildDeleteMessage() {
    return DeleteMessage.builder()
        .chatId(chatId)
        .messageId(menuMessageId)
        .build();
  }

  public void handleCallback(CallbackQuery callbackQuery) {
    switch (callbackQuery.getData()) {
      case CALLBACK_MAP -> onMapQuery();
      case CALLBACK_MENU -> onMenuQuery();
      case CALLBACK_HISTORY -> onHistoryQuery();
      case CALLBACK_STATES_PAGE_ONE,
           CALLBACK_STATES_PAGE_TWO,
           CALLBACK_STATES_PAGE_THREE,
           CALLBACK_STATES_PAGE_FOUR -> onStatesQuery(callbackQuery);
    }
  }

  private void onMapQuery() {
    try {
      execute(buildDeleteMessage());

      final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");

      Message msg = execute(
          SendPhoto.builder()
              .photo(new InputFile(API_MAP_URL + "?t=" + System.currentTimeMillis()))
              .chatId(chatId)
              .caption(String.format("Мапа України станом на *%s*", LocalTime.now().format(dtf)))
              .parseMode("Markdown")
              .replyMarkup(keyboardService.buildGoBackKeyboard())
              .build()
      );

      menuMessageId = msg.getMessageId();
    } catch (TelegramApiException ex) {
      ex.printStackTrace();
    }
  }

  private void onHistoryQuery() {
    try {
      restService.getAirAlertHistory();
    } catch (URISyntaxException ex) {
      ex.printStackTrace();
    }
  }

  private void onMenuQuery() {
    try {
      execute(buildDeleteMessage());
      sendMenu(DEFAULT_MENU_TEXT, keyboardService.buildMainKeyboard());
    } catch (TelegramApiException ex) {
      ex.printStackTrace();
    }
  }

  private void onStatesQuery(CallbackQuery callbackQuery) {
    try {
      execute(buildDeleteMessage());

      InlineKeyboardMarkup keyboardMarkup = switch (callbackQuery.getData()) {
        case CALLBACK_STATES_PAGE_ONE -> keyboardService.buildStatesKeyboard(1);
        case CALLBACK_STATES_PAGE_TWO -> keyboardService.buildStatesKeyboard(2);
        case CALLBACK_STATES_PAGE_THREE -> keyboardService.buildStatesKeyboard(3);
        case CALLBACK_STATES_PAGE_FOUR -> keyboardService.buildStatesKeyboard(4);
        default -> throw new IllegalStateException("Unexpected value: " + callbackQuery.getData());
      };

      sendMenu("Оберіть область", keyboardMarkup);
    } catch (TelegramApiException ex) {
      ex.printStackTrace();
    }
  }
}
