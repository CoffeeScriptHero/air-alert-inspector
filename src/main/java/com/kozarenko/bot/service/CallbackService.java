package com.kozarenko.bot.service;

import com.kozarenko.bot.component.StateDataProvider;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import static com.kozarenko.bot.util.Constants.*;

@Service
public class CallbackService {

  private static final String STATE_SUBSCRIBED = "Ви підписалися на ";
  private static final String STATE_UNSUBSCRIBED = "Ви відписалися від ";
  private static final String STATE_PAGE_TEXT =
      """
          ⬇️ Оберіть будь-яку область нижче.
                
          \uD83D\uDC40 означає, що ви підписані на дану область.
          """;

  private final SenderService senderService;
  private final KeyboardService keyboardService;
  private final SubscriptionService subscriptionService;
  private final StateDataProvider stateDataProvider;


  public CallbackService(SenderService senderService,
                         KeyboardService keyboardService,
                         SubscriptionService subscriptionService,
                         StateDataProvider stateDataProvider) {
    this.senderService = senderService;
    this.keyboardService = keyboardService;
    this.subscriptionService = subscriptionService;
    this.stateDataProvider = stateDataProvider;
  }

  public void handleCallback(CallbackQuery callbackQuery) {
    long chatId = callbackQuery.getMessage().getChat().getId();
    int messageId = callbackQuery.getMessage().getMessageId();
    String queryData = callbackQuery.getData();

    if (!queryData.equals(CALLBACK_SUBSCRIBE_ALL)
        && !queryData.equals(CALLBACK_UNSUBSCRIBE_ALL)
        && !queryData.startsWith(CALLBACK_STATE_PREFIX)) {
      senderService.deleteMessage(chatId, messageId);
    }

    switch (queryData) {
      case CALLBACK_MAP -> senderService.sendAlertMap(chatId);
      case CALLBACK_MENU -> senderService.sendMenu(chatId);
      case CALLBACK_SUBSCRIBE_ALL -> onSubscribeAll(callbackQuery.getId(), chatId);
      case CALLBACK_UNSUBSCRIBE_ALL -> onUnsubscribeAll(callbackQuery.getId(), chatId);
      case CALLBACK_HELP -> senderService.sendHelpMenu(chatId);
      case CALLBACK_STATES_PAGE_ONE,
          CALLBACK_STATES_PAGE_TWO,
          CALLBACK_STATES_PAGE_THREE,
          CALLBACK_STATES_PAGE_FOUR -> onStatePage(queryData, chatId);
      default -> onStateChosen(chatId, messageId, queryData, callbackQuery.getId());
    }
  }

  private void onSubscribeAll(String queryId, long chatId) {
    subscriptionService.subscribeToAllStates(chatId);
    senderService.sendCallbackQueryAnswer(queryId, "Ви підписались на всі області");
  }

  private void onUnsubscribeAll(String queryId, long chatId) {
    subscriptionService.unsubscribeFromAllStates(chatId);
    senderService.sendCallbackQueryAnswer(queryId, "Ви відписались від усіх областей");
  }

  private void onStatePage(String queryData, long chatId) {
    senderService.sendMenu(
        chatId,
        STATE_PAGE_TEXT,
        keyboardService.buildStatesKeyboard(
            Integer.parseInt(queryData.substring(queryData.length() - 1)),
            subscriptionService.retrieveStateIdsForChat(chatId)
        )
    );
  }

  private void onStateChosen(long chatId, int messageId, String queryData, String queryId) {
    int stateId = Integer.parseInt(queryData.substring(queryData.indexOf("-") + 1));

    if (stateId < StateDataProvider.STATE_FIRST_ID || stateId > StateDataProvider.STATE_LAST_ID) {
      return;
    }

    boolean isSubscribe = subscriptionService.toggleSubscription(chatId, stateId);
    String stateName = stateDataProvider.getStateById(stateId).get().getName();

    senderService.sendCallbackQueryAnswer(queryId, isSubscribe
        ? STATE_SUBSCRIBED + stateName
        : STATE_UNSUBSCRIBED + stateName);

    int page = (int) Math.ceil((double) stateId / KeyboardService.STATES_PER_PAGE);

    senderService.sendEditedReplyMarkup(
        chatId,
        messageId,
        keyboardService.buildStatesKeyboard(page, subscriptionService.retrieveStateIdsForChat(chatId))
    );
  }
}
