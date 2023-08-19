package com.kozarenko.bot.service;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import static com.kozarenko.bot.util.Constants.CALLBACK_MAP;
import static com.kozarenko.bot.util.Constants.CALLBACK_MENU;
import static com.kozarenko.bot.util.Constants.CALLBACK_STATES_PAGE_ONE;
import static com.kozarenko.bot.util.Constants.CALLBACK_STATES_PAGE_TWO;
import static com.kozarenko.bot.util.Constants.CALLBACK_STATES_PAGE_THREE;
import static com.kozarenko.bot.util.Constants.CALLBACK_STATES_PAGE_FOUR;

@Service
public class CallbackService {

  private final SenderService senderService;
  private final KeyboardService keyboardService;
  private final SubscriptionService subscriptionService;

  public CallbackService(SenderService senderService,
                         KeyboardService keyboardService,
                         SubscriptionService subscriptionService) {
    this.senderService = senderService;
    this.keyboardService = keyboardService;
    this.subscriptionService = subscriptionService;
  }

  public void handleCallback(CallbackQuery callbackQuery) {
    long chatId = callbackQuery.getMessage().getChat().getId();
    int messageId = callbackQuery.getMessage().getMessageId();

    senderService.deleteMessage(chatId, messageId);

    switch (callbackQuery.getData()) {
      case CALLBACK_MAP -> senderService.sendAlertMap(chatId);
      case CALLBACK_MENU -> senderService.sendMenu(chatId);
      case CALLBACK_STATES_PAGE_ONE,
          CALLBACK_STATES_PAGE_TWO,
          CALLBACK_STATES_PAGE_THREE,
          CALLBACK_STATES_PAGE_FOUR -> onStatePage(callbackQuery.getData(), chatId);
      default -> onStateChosen(chatId, callbackQuery.getData());
    }
  }

  private void onStatePage(String queryData, long chatId) {
    senderService.sendMenu(
        chatId,
        "Оберіть область",
        keyboardService.buildStatesKeyboard(
            Integer.parseInt(queryData.substring(queryData.length() - 1)),
            subscriptionService.getSubscriptions(chatId)
        )
    );
  }

  private void onStateChosen(long chatId, String queryData) {
    int stateId = Integer.parseInt(queryData);
    if (subscriptionService.isSubscriptionExists(chatId, stateId)) {
      subscriptionService.deleteSubscription(chatId, stateId);
    } else {
      subscriptionService.saveSubscription(chatId, stateId);
    }
  }
}
