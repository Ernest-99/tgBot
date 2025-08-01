package org.example.javabot.bot;

import org.example.javabot.config.BotConfig;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;

@Component
public class MyTelegramBot implements SpringLongPollingBot{

    private final BotConfig botConfig;
    private final UpdateConsumer updateConsumer;

    public MyTelegramBot(BotConfig botConfig, UpdateConsumer updateConsumer) {
        this.botConfig = botConfig;
        this.updateConsumer = updateConsumer;
    }

    @Override
    public String getBotToken() {
        return botConfig.getBotToken();
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return updateConsumer;
    }
}
