package com.kozarenko.bot.telegram;

import com.kozarenko.bot.service.KeyboardService;
import com.kozarenko.bot.service.RestService;
import com.kozarenko.bot.service.SenderService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
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

  private static final String API_MAP_URL = "https://alerts.com.ua/map.png";
  private static final String CALLBACK_MAP = "map";
  private static final String CALLBACK_MENU = "menu";
  private static final String CALLBACK_HISTORY = "history";

  @Value("${bot.username}")
  private String username;

  @Value("${bot.id}")
  private long id;

  private final SenderService senderService;
  private final RestService restService;
  private final KeyboardService keyboardService;

  public AirAlertBot(@Value("${bot.token}") String token,
                     RestService restService,
                     KeyboardService keyboardService,
                     SenderService senderService) {
    super(token);
    this.restService = restService;
    this.keyboardService = keyboardService;
    this.senderService = senderService;
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
            senderService.sendStartMessage(chat);
            senderService.sendMenu(chat.getId());
            break;
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

  public Long getId() {
    return id;
  }

  private boolean botAddedToGroup(Message msg) {
    if (msg.getNewChatMembers() != null && msg.getNewChatMembers().size() > 0) {
      if (msg.getNewChatMembers().stream().anyMatch(u -> u.getId().equals(getId()))) {
        senderService.sendStartMessage(msg.getChat());
        senderService.sendMenu(msg.getChat().getId());
        return true;
      }
    }
    return false;
  }

  private DeleteMessage buildDeleteMessage(long chatId, int messageId) {
    return DeleteMessage.builder()
        .chatId(chatId)
        .messageId(messageId)
        .build();
  }

  public void handleCallback(CallbackQuery callbackQuery) {
    long chatId = callbackQuery.getMessage().getChat().getId();
    int messageId = callbackQuery.getMessage().getMessageId();

    try {
      execute(buildDeleteMessage(chatId, messageId));
    } catch (TelegramApiException ex) {
      System.out.println("~~~~~~~~~~~~~~~~~~ MESSAGE WAS NOT DELETED");
      ex.printStackTrace();
    }

    switch (callbackQuery.getData()) {
      case CALLBACK_MAP -> onMapQuery(chatId);
      case CALLBACK_MENU -> onMenuQuery(chatId);
      case CALLBACK_HISTORY -> onHistoryQuery();
      case CALLBACK_STATES_PAGE_ONE,
          CALLBACK_STATES_PAGE_TWO,
          CALLBACK_STATES_PAGE_THREE,
          CALLBACK_STATES_PAGE_FOUR -> onStatesQuery(chatId, callbackQuery);
    }
  }

  private void onMapQuery(long chatId) {
    try {
      DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");

      execute(
          SendPhoto.builder()
              .photo(new InputFile(API_MAP_URL + "?t=" + System.currentTimeMillis()))
              .chatId(chatId)
              .caption(String.format("Мапа України станом на *%s*", LocalTime.now().format(dtf)))
              .parseMode("Markdown")
              .replyMarkup(keyboardService.buildGoBackKeyboard())
              .build()
      );
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

  private void onMenuQuery(long chatId) {
    senderService.sendMenu(chatId);
  }

  private void onStatesQuery(long chatId, CallbackQuery callbackQuery) {
    InlineKeyboardMarkup keyboardMarkup = switch (callbackQuery.getData()) {
      case CALLBACK_STATES_PAGE_ONE -> keyboardService.buildStatesKeyboard(1);
      case CALLBACK_STATES_PAGE_TWO -> keyboardService.buildStatesKeyboard(2);
      case CALLBACK_STATES_PAGE_THREE -> keyboardService.buildStatesKeyboard(3);
      case CALLBACK_STATES_PAGE_FOUR -> keyboardService.buildStatesKeyboard(4);
      default -> throw new IllegalStateException("Unexpected value: " + callbackQuery.getData());
    };

    senderService.sendMenu(chatId, "Оберіть область", keyboardMarkup);
  }
}
