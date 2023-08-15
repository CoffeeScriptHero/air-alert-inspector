package com.kozarenko.bot.service;

import com.kozarenko.bot.sender.AirAlertBotSender;
import org.springframework.stereotype.Service;

@Service
public class TelegramService {

    private final AirAlertBotSender botSender;

    public TelegramService(AirAlertBotSender botSender) {
        this.botSender = botSender;
    }
}
