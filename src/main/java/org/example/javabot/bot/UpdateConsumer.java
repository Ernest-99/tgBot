package org.example.javabot.bot;

import lombok.SneakyThrows;
import org.example.javabot.user.Role;
import org.example.javabot.user.UserService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

@Component
public class UpdateConsumer implements LongPollingSingleThreadUpdateConsumer {

    private final TelegramClient telegramClient;
    private final UserService userService;

    public UpdateConsumer(@Value("${telegram.bot.token}") String botToken, UserService userService) {
        this.telegramClient = new OkHttpTelegramClient(botToken);
        this.userService = userService;
    }

    @SneakyThrows
    @Override
    public void consume(Update update) {
        Long chatId = update.getMessage().getChatId();
        String text = update.getMessage().getText();

        if ("/start".equals(text)) {
            // Приветствие по роли
            Role role = userService.getOrCreateUser(chatId).getRole();
            String welcomeMessage = "Вы вошли как: " + role;
            sendMessage(chatId, welcomeMessage);
            // отправка сообщения
        } else if ("/upload".equals(text)) {
            if (userService.isAdmin(chatId)) {
                // логика загрузки Excel
            } else {
                // отказ в доступе
            }
        } else {
            // обычная логика
        }


//        if(update.hasMessage()){
//            String messageText = update.getMessage().getText();
//            Long chatId = update.getMessage().getChatId();
//
//            if(messageText.equals("/start")){
//                sendMainMenu(chatId);
//            }else {
//                sendMessage(chatId, "Я вас не понимаю");
//                System.out.println(chatId);
//            }
//        }else if(update.hasCallbackQuery()){
//            handleCallbackQuery(update.getCallbackQuery());
//        }

    }

    private void handleCallbackQuery(CallbackQuery callbackQuery) {
        var data = callbackQuery.getData();
        var chatId = callbackQuery.getFrom().getId();
        var user = callbackQuery.getFrom();
        switch (data){
            case "my_name"-> sendMyName(chatId, user);
            case "random"-> sendRandom(chatId);
            case "long_process"-> sendImage(chatId);
            default -> sendMessage(chatId, "Неизвестная команда");

        }
    }

    @SneakyThrows
    private void sendMessage(Long chatId, String messageText) {
        SendMessage message = SendMessage.builder().text(messageText).chatId(chatId).build();
        telegramClient.execute(message);
    }

    private void sendImage(Long chatId) {
        sendMessage(chatId, "test");
    }

    private void sendRandom(Long chatId) {
        sendMessage(chatId, "test1");
    }

    private void sendMyName(Long chatId, User user) {
        sendMessage(chatId, "test2");
    }

    @SneakyThrows
    private void sendMainMenu(Long chatId) {
        SendMessage message = SendMessage.builder().text("Добро пожаловать! Выберите действие: ").chatId(chatId).build();

        var button1 = InlineKeyboardButton.builder().text("Как меня зовут?").callbackData("my_name").build();
        var button2 = InlineKeyboardButton.builder().text("Случайное число").callbackData("random").build();
        var button3 = InlineKeyboardButton.builder().text("Долгий процесс").callbackData("long_process").build();

        List<InlineKeyboardRow> keyboardRows = List.of(
                new InlineKeyboardRow(button1),
                new InlineKeyboardRow(button2),
                new InlineKeyboardRow(button3)
        );

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(keyboardRows);

        message.setReplyMarkup(markup);

        telegramClient.execute(message);
    }
}
