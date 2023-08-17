package com.kozarenko.bot.service;

import com.kozarenko.bot.component.StateDataProvider;
import com.kozarenko.bot.model.State;
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

  private static final String CAPTION_ALERT_MAP = "\uD83D\uDDFA️ Мапа тривог";
  private static final String CAPTION_ALERT_HISTORY = "Історія тривог";
  private static final String CAPTION_STATES = "\uD83C\uDF03 Області";
  private static final String CAPTION_FOUR = "Кнопка 4";
  private static final String CAPTION_GO_BACK = "⬅ Повернутись назад";
  private static final String CAPTION_MAIN_MENU = "До меню";
  private static final String ARROW_RIGHT = "➡️";
  private static final String ARROW_LEFT = "⬅️";
  private static final int STATES_PER_PAGE = 8;
  private static final int STATES_PER_ROW = 2;

  private final StateDataProvider stateDataProvider;

  public KeyboardService(StateDataProvider stateDataProvider) {
    this.stateDataProvider = stateDataProvider;
  }

  public InlineKeyboardMarkup buildMainKeyboard() {
    return InlineKeyboardMarkup.builder().keyboard(List.of(
        List.of(
            InlineKeyboardButton.builder().text(CAPTION_ALERT_MAP).callbackData(CALLBACK_MAP).build(),
            InlineKeyboardButton.builder().text(CAPTION_ALERT_HISTORY).callbackData(CALLBACK_HISTORY).build()
        ),
        List.of(
            InlineKeyboardButton.builder().text(CAPTION_STATES).callbackData(CALLBACK_STATES_PAGE_ONE).build(),
            InlineKeyboardButton.builder().text(CAPTION_FOUR).callbackData("button4").build()
        )
    )).build();
  }

  public InlineKeyboardMarkup buildGoBackKeyboard() {
    return InlineKeyboardMarkup.builder().keyboard(
            List.of(
                Collections.singletonList(
                    InlineKeyboardButton.builder()
                        .text(CAPTION_GO_BACK)
                        .callbackData(CALLBACK_MENU)
                        .build()
                )
            )
        )
        .build();
  }

  public InlineKeyboardMarkup buildStatesKeyboard(int page) {
    List<List<InlineKeyboardButton>> keyboard = generateKeyboard();
    int fromIndex = STATES_PER_PAGE * (page - 1);
    int toIndex = page == 4 ? fromIndex + 1 : STATES_PER_PAGE * page;
    List<State> states = stateDataProvider.getStates().subList(fromIndex, toIndex);

    for (State state : states) {
      int i = page == 1 ? state.getId() - 1 : (state.getId() - 1) % fromIndex;
      keyboard.get(i / STATES_PER_ROW).add(
          InlineKeyboardButton.builder().text(state.getNameOfState()).callbackData(String.valueOf(state.getId())).build()
      );
    }

    List<InlineKeyboardButton> navigationRow = new ArrayList<>();

    if (page > 1) {
      String callbackData = page == 2
          ? CALLBACK_STATES_PAGE_ONE
          : (page == 3 ? CALLBACK_STATES_PAGE_TWO : CALLBACK_STATES_PAGE_THREE);
      navigationRow.add(InlineKeyboardButton.builder().text(ARROW_LEFT).callbackData(callbackData).build());
    }

    navigationRow.add(InlineKeyboardButton.builder().text(CAPTION_MAIN_MENU).callbackData(CALLBACK_MENU).build());

    if (page < 4) {
      String callbackData = page == 1
          ? CALLBACK_STATES_PAGE_TWO
          : (page == 2 ? CALLBACK_STATES_PAGE_THREE : CALLBACK_STATES_PAGE_FOUR);
      navigationRow.add(InlineKeyboardButton.builder().text(ARROW_RIGHT).callbackData(callbackData).build());
    }

    keyboard.add(navigationRow);

    return InlineKeyboardMarkup.builder().keyboard(keyboard).build();
  }

  private List<List<InlineKeyboardButton>> generateKeyboard() {
    return Stream.generate(ArrayList<InlineKeyboardButton>::new)
        .limit(STATES_PER_PAGE / STATES_PER_ROW)
        .collect(Collectors.toList());
  }
}
