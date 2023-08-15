package com.kozarenko.bot.sender;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.bots.DefaultBotOptions;

@Component
public class AirAlertBotSender extends DefaultAbsSender {

    @Value("${bot.token}")
    private String token;

    protected AirAlertBotSender() {
        super(new DefaultBotOptions());
    }

    @Override
    public String getBotToken() {
        return token;
    }
}
