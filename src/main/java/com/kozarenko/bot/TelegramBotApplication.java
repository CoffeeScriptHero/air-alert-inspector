package com.kozarenko.bot;

import com.kozarenko.bot.util.AppConstants;
import com.kozarenko.bot.util.ConfigManager;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class TelegramBotApplication {
  public static void main(String[] args) {

    try {
      ConfigManager configManager = ConfigManager.instance();
      TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
      TestBot bot = new TestBot(
              configManager.getProperty(AppConstants.BOT_TOKEN_FIELD),
              AppConstants.BOT_USERNAME,
              Long.parseLong(configManager.getProperty(AppConstants.BOT_ID_FIELD)));
      botsApi.registerBot(bot);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
}
