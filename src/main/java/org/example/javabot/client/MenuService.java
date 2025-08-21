package org.example.javabot.client;

import lombok.SneakyThrows;
import org.example.javabot.config.BotConfig;
import org.example.javabot.service.ExcelParserService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import java.util.ArrayList;
import java.util.List;

@Component
public class MenuService {
    private final BotConfig botConfig;
    private final TelegramClient telegramClient;
    private final ExcelParserService excelParserService;


    public MenuService(BotConfig botConfig, ExcelParserService excelParserService) {
        this.botConfig = botConfig;
        this.telegramClient = new OkHttpTelegramClient(botConfig.getBotToken());
        this.excelParserService = excelParserService;
    }

    //Кнопки выбора типа расписаний
    @SneakyThrows
    public void sendKeyboard(Long chatId) {
        SendMessage message = SendMessage.builder()
                .text("Вы можете выбрать тип расписания, по умолчанию стоит на сегодня)")
                .chatId(chatId)
                .build();

        List<KeyboardRow> keyboardRows = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add("Получать расписание на сегодня");
        row1.add("Получать расписание на неделю ");
        keyboardRows.add(row1);

        ReplyKeyboardMarkup replyKeyboardMarkup = ReplyKeyboardMarkup.builder()
                .keyboard(keyboardRows)
                .resizeKeyboard(true)  // делает кнопки компактнее
                .oneTimeKeyboard(false)
                .build();

        message.setReplyMarkup(replyKeyboardMarkup);

        telegramClient.execute(message);
    }

    //Кнопки курсов, показывает при запуске бота
    @SneakyThrows
    public void sendMenu(Long chatId) {
        // сделай меню для обычного пользователя
        SendMessage message = SendMessage.builder().text("Добро пожаловать! Выберите курс: ").chatId(chatId).build();

        var button1 = InlineKeyboardButton.builder().text("1 курс").callbackData("course_first").build();
        var button2 = InlineKeyboardButton.builder().text("2 курс").callbackData("course_second").build();
        var button3 = InlineKeyboardButton.builder().text("3 курс").callbackData("course_third").build();

        List<InlineKeyboardRow> keyboardRows = List.of(
                new InlineKeyboardRow(button1),
                new InlineKeyboardRow(button2),
                new InlineKeyboardRow(button3)
        );

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(keyboardRows);

        message.setReplyMarkup(markup);

        telegramClient.execute(message);
    }

    //Кнопки групп, показывает при выборе курса
    @SneakyThrows
    public InlineKeyboardMarkup createGroupButtons(List<String> groupNames) {
        List<InlineKeyboardRow> keyboardRows = new ArrayList<>();

        for (String group : groupNames) {
            InlineKeyboardButton button = InlineKeyboardButton.builder()
                    .text(group)
                    .callbackData("GROUP_" + group)
                    .build();

            InlineKeyboardRow row = new InlineKeyboardRow();
            row.add(button);

            keyboardRows.add(row);
        }

        return new InlineKeyboardMarkup(keyboardRows);
    }

    //Кнопки для админа
    @SneakyThrows
    public void sendAdminMenu(Long chatId) {
        SendMessage message = SendMessage.builder().text("Вы являетесь администратором и можете загружать расписание, а также выдавать админ права другим пользователям").chatId(chatId).build();
        var button1 = InlineKeyboardButton.builder().text("Назначить Администратора").callbackData("admin_set_admin").build();
        var button2 = InlineKeyboardButton.builder().text("Загрузить расписание").callbackData("admin_download_schedule").build();
        List<InlineKeyboardRow> keyboardRows = List.of(
                new InlineKeyboardRow(button1),
                new InlineKeyboardRow(button2)
        );

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(keyboardRows);

        message.setReplyMarkup(markup);

        telegramClient.execute(message);
    }
}
