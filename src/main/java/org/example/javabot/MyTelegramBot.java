package org.example.javabot;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.springframework.beans.factory.annotation.Value;

@Component
public class MyTelegramBot implements SpringLongPollingBot{

    private final String botToken;

    private final UpdateConsumer updateConsumer;

    public MyTelegramBot(@Value("${telegram.bot.token}") String botToken, UpdateConsumer updateConsumer) {
        this.botToken = botToken;
        this.updateConsumer = updateConsumer;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return updateConsumer;
    }
}
