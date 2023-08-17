package com.kozarenko.bot;

import com.kozarenko.bot.model.State;
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
import java.util.*;

@Component
public class AirAlertBot extends TelegramLongPollingBot {

  private static final String DEFAULT_MENU_TEXT = "Обери функцію, що цікавить тебе.";
  private static final String MAIN_MENU_TITLE = "Головне меню";
  private static final String ARROW_RIGHT = "➡️";
  private static final String ARROW_LEFT = "⬅️";
  private static final String API_MAP_URL = "https://alerts.com.ua/map.png";
  private static final String CALLBACK_MAP = "map";
  private static final String CALLBACK_MENU = "menu";
  private static final String CALLBACK_HISTORY = "history";
  private static final String CALLBACK_STATES_PAGE_ONE = "states1";
  private static final String CALLBACK_STATES_PAGE_TWO = "states2";
  private static final String CALLBACK_STATES_PAGE_THREE = "states2";
  private static final int STATES_FIRST_PAGE_MAX = 9;

  @Value("${bot.token}")
  private String token;

  @Value("${bot.username}")
  private String username;

  @Value("${bot.id}")
  private long id;

  private final RestService restService;

  private final StateService stateService;

  private int menuMessageId;

  private long chatId;

  public AirAlertBot(RestService restService, StateService stateService) {
    this.restService = restService;
    this.stateService = stateService;
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
            sendMenu(chatId);
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
          .replyMarkup(buildMainKeyboard())
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

  private InlineKeyboardMarkup buildMainKeyboard() {
    InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
    List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

    List<InlineKeyboardButton> row1 = new ArrayList<>();
    List<InlineKeyboardButton> row2 = new ArrayList<>();

    row1.add(InlineKeyboardButton.builder().text("\uD83D\uDDFA️ Мапа тривог").callbackData(CALLBACK_MAP).build());
    row1.add(InlineKeyboardButton.builder().text("Історія тривог").callbackData(CALLBACK_HISTORY).build());

    row2.add(InlineKeyboardButton.builder().text("\uD83C\uDF03 Менеджер областей").callbackData(CALLBACK_STATES_PAGE_ONE).build());
    row2.add(InlineKeyboardButton.builder().text("Кнопка 4").callbackData("button4").build());

    keyboard.add(row1);
    keyboard.add(row2);

    inlineKeyboardMarkup.setKeyboard(keyboard);

    return inlineKeyboardMarkup;
  }

  private InlineKeyboardMarkup buildGoBackKeyboard() {
    InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
    List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

    keyboard.add(
        Collections.singletonList(
            InlineKeyboardButton.builder()
                .text("⬅ Повернутись назад")
                .callbackData(CALLBACK_MENU)
                .build()
        )
    );

    inlineKeyboardMarkup.setKeyboard(keyboard);
    return inlineKeyboardMarkup;
  }

  private InlineKeyboardMarkup buildStatesKeyboardPageOne() {
    InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

    List<InlineKeyboardButton> row1 = new ArrayList<>();
    List<InlineKeyboardButton> row2 = new ArrayList<>();
    List<InlineKeyboardButton> row3 = new ArrayList<>();
    List<InlineKeyboardButton> row4 = new ArrayList<>();

    List<List<InlineKeyboardButton>> rows = Arrays.asList(row1, row2, row3, row4);
    List<State> states = stateService.getStates().subList(0, STATES_FIRST_PAGE_MAX);

    for (State state : states) {
      rows.get((state.getId() - 1) / 3).add(
          InlineKeyboardButton.builder().text(state.getName()).callbackData(String.valueOf(state.getId())).build()
      );
    }

    row4.add(InlineKeyboardButton.builder().text(MAIN_MENU_TITLE).callbackData(CALLBACK_MENU).build());
    row4.add(InlineKeyboardButton.builder().text(ARROW_RIGHT).callbackData(CALLBACK_STATES_PAGE_TWO).build());

    List<List<InlineKeyboardButton>> keyboard = new ArrayList<>(rows);
    inlineKeyboardMarkup.setKeyboard(keyboard);

    return inlineKeyboardMarkup;
  }

  public void handleCallback(CallbackQuery callbackQuery) {
    switch (callbackQuery.getData()) {
      case CALLBACK_MAP -> onMapQuery(callbackQuery);
      case CALLBACK_MENU-> onMenuQuery(callbackQuery);
      case CALLBACK_HISTORY -> onHistoryQuery(callbackQuery);
      case CALLBACK_STATES_PAGE_ONE -> onStatesPageOneQuery(callbackQuery);
    }
  }

  private void onMapQuery(CallbackQuery callbackQuery) {
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
              .replyMarkup(buildGoBackKeyboard())
              .build()
      );

      menuMessageId = msg.getMessageId();
    } catch (TelegramApiException ex) {
      ex.printStackTrace();
    }
  }

  private void onHistoryQuery(CallbackQuery callbackQuery) {
    try {
      restService.getAirAlertHistory();
    } catch (URISyntaxException ex) {
      ex.printStackTrace();
    }
  }

  private void onMenuQuery(CallbackQuery callbackQuery) {
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

  private void onStatesPageOneQuery(CallbackQuery callbackQuery) {

  }
}
