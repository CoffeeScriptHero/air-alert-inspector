package com.kozarenko.bot;

import com.kozarenko.bot.util.AppConstants;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.Locality;
import org.telegram.abilitybots.api.objects.Privacy;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.polls.SendPoll;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class TestBot extends AbilityBot {

    private final Long BOT_ID;

    public TestBot(String token, String username, Long id) {
        super(token, username);
        BOT_ID = id;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {

            Message message = update.getMessage();

            System.out.println(message);

            if (message.getNewChatMembers() != null && message.getNewChatMembers().size() > 0) {
                System.out.println("Condition is true");
                if (message.getNewChatMembers().stream().anyMatch(u -> u.getId().equals(getId()))) {
                    startAction(update);
                    return;
                }
            }

            switch (update.getMessage().getText()) {
                case "/start":
                    startAction(update);
                    break;
                default:
                    System.out.println("Undefined command :(\nTry again");
            }

//            SendMessage sendMessage = new SendMessage();
//            sendMessage.setChatId(update.getMessage().getChatId());
//            sendMessage.setText(update.getMessage().getText());
//            try {
//                execute(sendMessage);
//            } catch (TelegramApiException e) {
//                throw new RuntimeException(e);
//            }
        }
    }

    @Override
    public long creatorId() {
        return AppConstants.CREATOR_ID;
    }

    @Override
    public String getBotUsername() {
        return AppConstants.BOT_USERNAME;
    }

    public Long getId() {
        return BOT_ID;
    }

    public void startAction(Update update) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(update.getMessage().getChatId());

        Chat chat = update.getMessage().getChat();
        String chatType = chat.getType();

        String greetingsText = chatType.equals("group")
                        ? "Вітаю усіх учасників групи " + chat.getTitle() + "!"
                        : "Вітаю, " + chat.getFirstName() + " " + chat.getLastName() + "!";
        sendMessage.setText(greetingsText + " Я бот, що повідомляє про активацію повітряної тривоги в містах та областях.");

        try {
            execute(sendMessage);
        } catch (TelegramApiException ex) {
            ex.printStackTrace();
        }
    }

//    public Ability sayHelloWorld() {
//        return Ability
//                .builder()
//                .name("hello")
//                .info("says hello world!")
//                .locality(Locality.ALL)
//                .privacy(Privacy.PUBLIC)
//                .action(ctx -> silent.send("Привіт!", ctx.chatId()))
//                .build();
//    }
//
//    public Ability startAction() {
//        return Ability
//                .builder()
//                .name("start")
//                .info("starts bot")
//                .locality(Locality.ALL)
//                .privacy(Privacy.PUBLIC)
//                .action(ctx -> {
//                    SendPoll poll = new SendPoll(
//                            ctx.chatId().toString(),
//                            "What do you have today for the breakfast?",
//                            Arrays.asList("Avocado", "Eggs", "Coffee with pancakes", "KFC"));
//                    try {
//                        poll.setExplanation("Some wise explanation");
//                        Message pollMessage = execute(poll);
//                        System.out.println("Poll: " + pollMessage.getPoll());
//                    } catch (TelegramApiException e) {
//                        throw new RuntimeException(e);
//                    }
//                })
//                .build();
//    }
}
