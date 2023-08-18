package com.kozarenko.bot.service;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import static com.kozarenko.bot.util.Constants.CALLBACK_MAP;
import static com.kozarenko.bot.util.Constants.CALLBACK_MENU;
import static com.kozarenko.bot.util.Constants.CALLBACK_HISTORY;
import static com.kozarenko.bot.util.Constants.CALLBACK_STATES_PAGE_ONE;
import static com.kozarenko.bot.util.Constants.CALLBACK_STATES_PAGE_TWO;
import static com.kozarenko.bot.util.Constants.CALLBACK_STATES_PAGE_THREE;
import static com.kozarenko.bot.util.Constants.CALLBACK_STATES_PAGE_FOUR;

@Service
public class CallbackService {

  private final SenderService senderService;
  private final KeyboardService keyboardService;

  public CallbackService(SenderService senderService,
                         KeyboardService keyboardService) {
    this.senderService = senderService;
    this.keyboardService = keyboardService;
  }

  public void handleCallback(CallbackQuery callbackQuery) {
    long chatId = callbackQuery.getMessage().getChat().getId();
    int messageId = callbackQuery.getMessage().getMessageId();

    senderService.deleteMessage(chatId, messageId);

    switch (callbackQuery.getData()) {
      case CALLBACK_MAP -> senderService.sendAlertMap(chatId);
      case CALLBACK_MENU -> onMenuQuery(chatId);
      case CALLBACK_HISTORY -> onHistoryQuery();
      case CALLBACK_STATES_PAGE_ONE,
          CALLBACK_STATES_PAGE_TWO,
          CALLBACK_STATES_PAGE_THREE,
          CALLBACK_STATES_PAGE_FOUR -> onStatesQuery(chatId, callbackQuery);
    }
  }

  private void onHistoryQuery() {
//    try {
//      restService.getAirAlertHistory();
//    } catch (URISyntaxException ex) {
//      ex.printStackTrace();
//    }
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
