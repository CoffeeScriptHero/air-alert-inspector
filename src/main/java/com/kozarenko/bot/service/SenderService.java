package com.kozarenko.bot.service;

import com.kozarenko.bot.model.State;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static com.kozarenko.bot.util.Constants.API_MAP_URL;
import static com.kozarenko.bot.util.Constants.PARSE_MODE_MARKDOWN;

@Service
public class SenderService extends DefaultAbsSender {

  private static final String UKRAINE_ZONE_ID = "Europe/Kiev";
  private static final String GREETINGS_GROUP = "Вітаю всіх учасників групи ";
  private static final String GREETINGS_PERSONAL = "Вітаю, ";
  private static final String DEFAULT_MENU_TEXT = "Оберіть будь-яку функцію:";
  private static final String MAP_STATUS_TEXT = "Мапа України станом на *%s*";
  private static final String GROUP = "group";
  private static final String SUPERGROUP = "supergroup";
  private static final String BOT_DESCRIPTION =
      """
          \n
          Я - бот, що повідомляє про активацію повітряної тривоги в областях.
                
          Мій функціонал передбачає, що ви можете вибірково підписуватись на області, повітряну тривогу яких ви хочете
          відслідковувати. За замовчуванням, при старті цього бота, ви підписані на всі області України.
                
          Основна комунікація зі мною відбувається через меню (яке відкривається за допомогою кнопки меню зліва від поля
          введеня повідомлення або ж через команду /menu).
          Меню містить наступні пункти:
                
          •   \uD83D\uDDFA️ *Мапа тривог* - отримати актуальну мапу повітряних тривог України
                
          •   \uD83C\uDF03 *Області* - відкриває меню для керування підписками на області. Якщо ви відпишетесь від області, вам не будуть надходити повідомлення про початок/кінець повітряної тривоги в даній області
                
          •   ➕ *Підписатися на всі області* - підписка на всі області
                
          •   ➖ *Відписатися від усіх областей* - відписка від усіх областей
          _Прим.: відписуючись від усіх областей, ви не отримуватимете жодних повідомлень. Ця опція існує для зручності, щоб з нуля вибірково вибрати області, які ви хочете відслідковувати, тож, відписавшись від усіх областей, не забудьте потім підписатись на області, за якими ви хочете спостерігати._
                
          •   ❔ *Допомога* - показує повідомлення з описом функціоналу бота та його основних функцій
                
          Бот також підтримує наступні команди:
               
          */start* - запускає бота  
          */menu*  - відкриває головне меню
          */help*  - те саме, що й кнопка ❔ Допомога
          """;

  private final KeyboardService keyboardService;

  protected SenderService(@Value("${bot.token}") String token, KeyboardService keyboardService) {
    super(new DefaultBotOptions(), token);
    this.keyboardService = keyboardService;
  }

  public void sendStartMessage(Chat chat) {
    String greetingsText = chat.getType().equals(GROUP) || chat.getType().equals(SUPERGROUP)
        ? GREETINGS_GROUP + chat.getTitle() + "!"
        : GREETINGS_PERSONAL + chat.getFirstName() + " " + chat.getLastName() + "!";

    try {
      execute(
          SendMessage.builder()
              .chatId(chat.getId())
              .text(greetingsText + BOT_DESCRIPTION)
              .parseMode(PARSE_MODE_MARKDOWN)
              .build()
      );
    } catch (TelegramApiException ex) {
      ex.printStackTrace();
    }
  }

  public void sendMenu(long chatId) {
    sendMenu(chatId, DEFAULT_MENU_TEXT, keyboardService.getKeyboardMain());
  }

  public void sendMenu(long chatId, String text, InlineKeyboardMarkup keyboardMarkup) {
    try {
      execute(SendMessage.builder()
          .chatId(chatId)
          .text(text)
          .replyMarkup(keyboardMarkup)
          .build()
      );
    } catch (TelegramApiException ex) {
      ex.printStackTrace();
    }
  }

  public void sendAlertMessage(long chatId, State state) {
    try {
      execute(SendMessage.builder()
          .chatId(chatId)
          .text(state.getAlertMessage())
          .build()
      );
    } catch (TelegramApiException ex) {
      ex.printStackTrace();
    }
  }

  public void sendCallbackQueryAnswer(String callbackQueryId, String text) {
    try {
      execute(AnswerCallbackQuery.builder()
          .callbackQueryId(callbackQueryId)
          .text(text)
          .build());
    } catch (TelegramApiException ex) {
      ex.printStackTrace();
    }
  }

  public void sendEditedReplyMarkup(long chatId, int messageId, InlineKeyboardMarkup keyboardMarkup) {
    try {
      execute(EditMessageReplyMarkup.builder()
          .chatId(chatId)
          .messageId(messageId)
          .replyMarkup(keyboardMarkup)
          .build()
      );
    } catch (TelegramApiException ex) {
      ex.printStackTrace();
    }
  }

  public void deleteMessage(long chatId, int messageId) {
    try {
      execute(buildDeleteMessage(chatId, messageId));
    } catch (TelegramApiException ex) {
      ex.printStackTrace();
    }
  }

  public void sendAlertMap(long chatId) {
    try {
      DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");

      execute(
          SendPhoto.builder()
              .photo(new InputFile(API_MAP_URL + "?t=" + System.currentTimeMillis()))
              .chatId(chatId)
              .caption(String.format(MAP_STATUS_TEXT,
                  ZonedDateTime.now(ZoneId.of(UKRAINE_ZONE_ID)).toLocalTime().format(dtf)))
              .parseMode(PARSE_MODE_MARKDOWN)
              .replyMarkup(keyboardService.getKeyboardGoBack())
              .build()
      );
    } catch (TelegramApiException ex) {
      ex.printStackTrace();
    }
  }

  public void sendHelpMessage(long chatId) {
    try {
      execute(SendMessage.builder()
          .chatId(chatId)
          .text(BOT_DESCRIPTION)
          .parseMode(PARSE_MODE_MARKDOWN)
          .build());
    } catch (TelegramApiException ex) {
      ex.printStackTrace();
    }
  }

  public void sendHelpMenu(long chatId) {
    try {
      execute(SendMessage.builder()
          .chatId(chatId)
          .text(BOT_DESCRIPTION)
          .parseMode(PARSE_MODE_MARKDOWN)
          .replyMarkup(keyboardService.getKeyboardGoBack())
          .build());
    } catch (TelegramApiException ex) {
      ex.printStackTrace();
    }
  }

  private DeleteMessage buildDeleteMessage(long chatId, int messageId) {
    return DeleteMessage.builder()
        .chatId(chatId)
        .messageId(messageId)
        .build();
  }
}
