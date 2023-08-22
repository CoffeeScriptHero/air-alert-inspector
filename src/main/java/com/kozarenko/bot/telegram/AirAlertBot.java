package com.kozarenko.bot.telegram;

import com.kozarenko.bot.service.UpdateService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class AirAlertBot extends TelegramLongPollingBot {

  @Value("${bot.username}")
  private String username;

  @Value("${bot.id}")
  private long id;

  private final UpdateService updateService;

  public AirAlertBot(@Value("${bot.token}") String token,
                     UpdateService updateService) {
    super(token);
    this.updateService = updateService;
  }

  @Override
  public void onUpdateReceived(Update update) {
    updateService.handleUpdate(update, id);
  }

  @Override
  public String getBotUsername() {
    return username;
  }
}
