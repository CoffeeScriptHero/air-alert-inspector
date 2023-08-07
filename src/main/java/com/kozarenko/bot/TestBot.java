package com.kozarenko.bot;

import com.kozarenko.bot.util.AppConstants;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.*;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.GetMe;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

public class TestBot extends AbilityBot {

    public TestBot(String token, String username) {
        super(token, username);
    }

    @Override
    public long creatorId() {
        return AppConstants.CREATOR_ID;
    }

    @Override
    public String getBotUsername() {
        return AppConstants.BOT_USERNAME;
    }

    @Override
    public boolean checkGlobalFlags(Update update) {
        return true;
    }

    public void doStartAction(Chat chat) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        List<InlineKeyboardButton> row2 = new ArrayList<>();

        InlineKeyboardButton button1 = new InlineKeyboardButton();
        button1.setText("Кнопка 1");
        button1.setCallbackData("button1");

        InlineKeyboardButton button2 = new InlineKeyboardButton();
        button2.setText("Кнопка 2");
        button2.setCallbackData("button2");

        InlineKeyboardButton button3 = new InlineKeyboardButton();
        button3.setText("Кнопка 3");
        button3.setCallbackData("button3");

        InlineKeyboardButton button4 = new InlineKeyboardButton();
        button4.setText("Кнопка 4");
        button4.setCallbackData("button4");

        row1.add(button1);
        row1.add(button2);

        row2.add(button3);
        row2.add(button4);

        keyboard.add(row1);
        keyboard.add(row2);

        inlineKeyboardMarkup.setKeyboard(keyboard);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chat.getId());

        String greetingsText = chat.getType().equals("group") || chat.getType().equals("supergroup")
                ? "Вітаю всіх учасників групи " + chat.getTitle() + "!"
                : "Вітаю, " + chat.getFirstName() + " " + chat.getLastName() + "!";

        sendMessage.setText(greetingsText
                + " Я бот, що повідомляє про активацію повітряної тривоги в містах та областях.");
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);

        try {
            execute(sendMessage);
        } catch (TelegramApiException ex) {
            ex.printStackTrace();
        }
    }

    public Ability helloAction() {
        return Ability
                .builder()
                .name("hello")
                .info("says hello world!")
                .locality(Locality.ALL)
                .privacy(Privacy.PUBLIC)
                .action(ctx -> silent.send("Привіт!", ctx.chatId()))
                .build();
    }

    public Ability startAction() {
        return Ability
                .builder()
                .name("start")
                .info("Starts the bot.")
                .locality(Locality.ALL)
                .privacy(Privacy.PUBLIC)
                .action(ctx -> doStartAction(ctx.update().getMessage().getChat()))
                .build();
    }

//    public Ability inviteAction() {
//        return Ability
//                .builder()
//                .name(DEFAULT)
//                .flag(Flag.MY_CHAT_MEMBER)
//                .privacy(Privacy.PUBLIC)
//                .locality(Locality.ALL)
//                .action(ctx -> doStartAction(ctx.update().getMyChatMember().getChat()))
//                .build();
//    }

    public Ability callbackQueryAction() {
        return Ability
                .builder()
                .name(DEFAULT)
                .flag(Flag.CALLBACK_QUERY)
                .privacy(Privacy.PUBLIC)
                .locality(Locality.ALL)
                .action(ctx -> {
                    AnswerCallbackQuery answer = new AnswerCallbackQuery();
                    answer.setText(ctx.update().getCallbackQuery().getData());

                    try {
                        execute(answer);
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }

                })
                .build();
    }
}
