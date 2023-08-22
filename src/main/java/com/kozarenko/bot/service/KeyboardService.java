package com.kozarenko.bot.service;

import com.kozarenko.bot.provider.StateDataProvider;
import com.kozarenko.bot.model.State;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.kozarenko.bot.util.Constants.CALLBACK_STATE_PREFIX;
import static com.kozarenko.bot.util.Constants.CALLBACK_STATES_PAGE_ONE;
import static com.kozarenko.bot.util.Constants.CALLBACK_STATES_PAGE_TWO;
import static com.kozarenko.bot.util.Constants.CALLBACK_STATES_PAGE_THREE;
import static com.kozarenko.bot.util.Constants.CALLBACK_STATES_PAGE_FOUR;
import static com.kozarenko.bot.util.Constants.CALLBACK_MENU;
import static com.kozarenko.bot.util.Constants.CALLBACK_MAP;
import static com.kozarenko.bot.util.Constants.CALLBACK_SUBSCRIBE_ALL;
import static com.kozarenko.bot.util.Constants.CALLBACK_UNSUBSCRIBE_ALL;
import static com.kozarenko.bot.util.Constants.CALLBACK_HELP;


@Service
public class KeyboardService {

  public static final int STATES_PER_PAGE = 8;
  public static final int STATES_PER_ROW = 2;

  private static final InlineKeyboardMarkup KEYBOARD_MAIN = buildMainKeyboard();
  private static final InlineKeyboardMarkup KEYBOARD_GO_BACK = buildGoBackKeyboard();

  private static final String CAPTION_ALERT_MAP = "\uD83D\uDDFA️ Мапа тривог";
  private static final String CAPTION_STATES = "\uD83C\uDF03 Області";
  private static final String CAPTION_SUBSCRIBE_ALL = "➕ Підписатися на всі області";
  private static final String CAPTION_UNSUBSCRIBE_ALL = "➖ Відписатися від усіх областей";
  private static final String CAPTION_HELP = "❔ Допомога";
  private static final String CAPTION_GO_BACK = "⬅ Повернутись назад";
  private static final String CAPTION_MAIN_MENU = "До меню";
  private static final String ARROW_RIGHT = "➡️";
  private static final String ARROW_LEFT = "⬅️";
  private static final String EYES = "\uD83D\uDC40";

  private final StateDataProvider stateDataProvider;

  public KeyboardService(StateDataProvider stateDataProvider) {
    this.stateDataProvider = stateDataProvider;
  }

  public InlineKeyboardMarkup getKeyboardMain() {
    return KEYBOARD_MAIN;
  }

  public InlineKeyboardMarkup getKeyboardGoBack() {
    return KEYBOARD_GO_BACK;
  }

  public InlineKeyboardMarkup buildStatesKeyboard(int page, List<Integer> subscribedStates) {
    List<List<InlineKeyboardButton>> keyboard = generateKeyboard();
    int fromIndex = STATES_PER_PAGE * (page - 1);
    int toIndex = page == 4 ? fromIndex + 1 : STATES_PER_PAGE * page;
    List<State> states = stateDataProvider.getStates().subList(fromIndex, toIndex);

    for (State state : states) {
      String text = isSubscription(state, subscribedStates) ? EYES + " " + state.getShortName() : state.getShortName();
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

  private static InlineKeyboardMarkup buildMainKeyboard() {
    return InlineKeyboardMarkup.builder().keyboard(List.of(
        List.of(buildButton(CAPTION_ALERT_MAP, CALLBACK_MAP), buildButton(CAPTION_STATES, CALLBACK_STATES_PAGE_ONE)),
        List.of(buildButton(CAPTION_SUBSCRIBE_ALL, CALLBACK_SUBSCRIBE_ALL)),
        List.of(buildButton(CAPTION_UNSUBSCRIBE_ALL, CALLBACK_UNSUBSCRIBE_ALL)),
        List.of(buildButton(CAPTION_HELP, CALLBACK_HELP))
    )).build();
  }

  private static InlineKeyboardMarkup buildGoBackKeyboard() {
    return InlineKeyboardMarkup.builder()
        .keyboard(List.of(Collections.singletonList(buildButton(CAPTION_GO_BACK, CALLBACK_MENU))))
        .build();
  }

  private static InlineKeyboardButton buildButton(String text, String callbackData) {
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
