package com.kozarenko.bot.service;

import com.kozarenko.bot.component.StateDataProvider;
import com.kozarenko.bot.model.State;
import com.kozarenko.bot.model.Subscription;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.kozarenko.bot.util.Constants.*;

@Service
public class KeyboardService {

  public static final int STATES_PER_PAGE = 8;
  public static final int STATES_PER_ROW = 2;

  private static final String CAPTION_ALERT_MAP = "\uD83D\uDDFA️ Мапа тривог";
  private static final String CAPTION_ALERT_HISTORY = "Історія тривог";
  private static final String CAPTION_STATES = "\uD83C\uDF03 Області";
  private static final String CAPTION_FOUR = "Кнопка 4";
  private static final String CAPTION_GO_BACK = "⬅ Повернутись назад";
  private static final String CAPTION_MAIN_MENU = "До меню";
  private static final String ARROW_RIGHT = "➡️";
  private static final String ARROW_LEFT = "⬅️";
  private static final String EYES = "\uD83D\uDC40";

  private final StateDataProvider stateDataProvider;

  public KeyboardService(StateDataProvider stateDataProvider) {
    this.stateDataProvider = stateDataProvider;
  }

  public InlineKeyboardMarkup buildMainKeyboard() {
    return InlineKeyboardMarkup.builder().keyboard(List.of(
        List.of(buildButton(CAPTION_ALERT_MAP, CALLBACK_MAP), buildButton(CAPTION_ALERT_HISTORY, CALLBACK_HISTORY)),
        List.of(buildButton(CAPTION_STATES, CALLBACK_STATES_PAGE_ONE), buildButton(CAPTION_FOUR, "button4"))
    )).build();
  }

  public InlineKeyboardMarkup buildGoBackKeyboard() {
    return InlineKeyboardMarkup.builder()
        .keyboard(List.of(Collections.singletonList(buildButton(CAPTION_GO_BACK, CALLBACK_MENU))))
        .build();
  }

  public InlineKeyboardMarkup buildStatesKeyboard(int page, List<Subscription> subscriptions) {
    List<Integer> stateIds = subscriptions.stream().map(Subscription::getStateId).toList();
    List<List<InlineKeyboardButton>> keyboard = generateKeyboard();
    int fromIndex = STATES_PER_PAGE * (page - 1);
    int toIndex = page == 4 ? fromIndex + 1 : STATES_PER_PAGE * page;
    List<State> states = stateDataProvider.getStates().subList(fromIndex, toIndex);

    for (State state : states) {
      String text = isSubscription(state, stateIds) ? EYES + " " + state.getNameOfState() : state.getNameOfState();
      int i = page == 1 ? state.getId() - 1 : (state.getId() - 1) % fromIndex;
      keyboard.get(i / STATES_PER_ROW).add(buildButton(text, CALLBACK_STATE_PREFIX + state.getId()));
    }

    List<InlineKeyboardButton> navigationRow = new ArrayList<>();

    if (page > 1) {
      String callbackData = page == 2
          ? CALLBACK_STATES_PAGE_ONE
          : (page == 3 ? CALLBACK_STATES_PAGE_TWO : CALLBACK_STATES_PAGE_THREE);
      navigationRow.add(buildButton(ARROW_LEFT, callbackData));
    }

    navigationRow.add(buildButton(CAPTION_MAIN_MENU, CALLBACK_MENU));

    if (page < 4) {
      String callbackData = page == 1
          ? CALLBACK_STATES_PAGE_TWO
          : (page == 2 ? CALLBACK_STATES_PAGE_THREE : CALLBACK_STATES_PAGE_FOUR);
      navigationRow.add(buildButton(ARROW_RIGHT, callbackData));
    }

    keyboard.add(navigationRow);

    return InlineKeyboardMarkup.builder().keyboard(keyboard).build();
  }

  private boolean isSubscription(State state, List<Integer> stateIds) {
    return stateIds.contains(state.getId());
  }

  private InlineKeyboardButton buildButton(String text, String callbackData) {
    return InlineKeyboardButton.builder()
        .text(text)
        .callbackData(callbackData)
        .build();
  }

  private List<List<InlineKeyboardButton>> generateKeyboard() {
    return Stream.generate(ArrayList<InlineKeyboardButton>::new)
        .limit(STATES_PER_PAGE / STATES_PER_ROW)
        .collect(Collectors.toList());
  }
}
