package org.example.javabot.client;

import lombok.SneakyThrows;
import org.example.javabot.config.BotConfig;
import org.example.javabot.service.ExcelParserService;
import org.example.javabot.user.entity.UserEntity;
import org.example.javabot.user.repositories.UserRepository;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.File;
import java.util.List;

@Component
public class CallbackHandle {
    private  final TelegramClient telegramClient;
    private final BotConfig botConfig;
    private final MenuService menuService;
    private final UserRepository userRepository;
    private ExcelParserService excelParserService;

    public CallbackHandle(BotConfig botConfig, MenuService menuService, ExcelParserService excelParserService, UserRepository userRepository) {
        this.botConfig = botConfig;
        this.telegramClient = new OkHttpTelegramClient(botConfig.getBotToken());
        this.menuService = menuService;
        this.excelParserService = excelParserService;
        this.userRepository = userRepository;
    }

    public void handleCallback(CallbackQuery callbackQuery) {
        var data = callbackQuery.getData();
        var chatId = callbackQuery.getFrom().getId();
        var messageId = callbackQuery.getMessage().getMessageId();
        var callbackQueryId = callbackQuery.getId();

        // Обработка выбора курса
        if (data.startsWith("course_")) {
            handleCourseSelection(callbackQueryId, data, chatId);
        }
        // Обработка выбора группы
        else if (data.startsWith("GROUP_")) {
            handleGroupSelection(callbackQueryId, data, chatId);
        }
        // Обработка других действий
        else if (data.startsWith("action_")) {
            //handleAction(callbackQuery, data);
        }
        else {
            sendMessage(chatId, "Неизвестная команда.");
        }
    }


    private void handleCourseSelection(String callbackQueryId, String data, Long chatId){
        try {
            // 1. Сначала отвечаем на callback (убираем индикатор загрузки)
            answerCallbackQuery(callbackQueryId);

            // 2. Затем обрабатываем логику
            String fileName = switch (data) {
                case "course_first" -> "1course.xlsx";
                case "course_second" -> "2course.xlsx";
                case "course_third" -> "3course.xlsx";
                default -> null;
            };

            if (fileName != null) {
                sendGroupSelectionMenu(chatId, new File("courses/" + fileName));
            } else {
                sendMessage(chatId, "Неизвестная команда.");
            }
        }catch (Exception e){
            // В случае ошибки тоже отвечаем на callback
            answerCallbackQuery(callbackQueryId);
            e.printStackTrace();
        }
    }
    private void handleGroupSelection(String callbackQueryId, String data, Long chatId) {
        try {
            answerCallbackQuery(callbackQueryId);

            String groupName = data.substring("GROUP_".length());

            // Получаем тип расписания
            int scheduleType = userRepository.findByChatId(chatId)
                    .map(UserEntity::getScheduleType)
                    .orElse(0);
            // Ищем группу во всех трех курсах
            boolean found = false;

            for (Course course : Course.values()) {
                try {
                    String schedule;
                    File courseFile = course.getFile();

                    if (scheduleType == 1) {
                        schedule = excelParserService.getScheduleByGroupForWeek(courseFile, groupName);
                    } else {
                        schedule = excelParserService.getScheduleByGroupToday(courseFile, groupName);
                    }

                    // Проверяем, что расписание найдено и не содержит ошибок
                    if (schedule != null && !schedule.trim().isEmpty() &&
                            !schedule.contains("❌ Не найден") &&
                            !schedule.contains("не найдена")) {

                        String message = "📘 " + course.getDisplayName() +
                                "\n👥 Группа " + groupName +
                                "\n" + schedule;
                        sendMessage(chatId, message);
                        found = true;
                        break; // Прерываем цикл после нахождения
                    }
                } catch (Exception e) {
                    // Продолжаем поиск в следующем курсе при ошибке
                    continue;
                }
            }
            // Если ни в одном курсе не нашли
            if (!found) {
                sendMessage(chatId, "❌ Группа \"" + groupName + "\" не найдена ни в одном курсе\n\n" +
                        "Проверьте правильность написания группы");
            }
        } catch (Exception e) {
            answerCallbackQuery(callbackQueryId);
            e.printStackTrace();
            sendMessage(chatId, "⚠️ Произошла ошибка. Попробуйте позже.");
        }
    }

    // Вспомогательный метод для ответа на callback, чтобы убарть индикатор загрузки
    private void answerCallbackQuery(String callbackQueryId) {
        AnswerCallbackQuery answer = new AnswerCallbackQuery(callbackQueryId);
        try {
            telegramClient.execute(answer);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @SneakyThrows
    private void sendGroupSelectionMenu(Long chatId, File excelFile) {
        List<String> groups = excelParserService.getGroupNames(excelFile);
        InlineKeyboardMarkup markup = menuService.createGroupButtons(groups);

        SendMessage message = SendMessage.builder().text("Выберите свою группу:").chatId(chatId).build();
        message.setReplyMarkup(markup);

        try {
            telegramClient.execute(message); // или просто execute(...) если ты в классе TelegramLongPollingBot
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @SneakyThrows
    public void sendMessage(Long chatId, String messageText) {
        SendMessage message = SendMessage.builder().text(messageText).chatId(chatId).build();
        telegramClient.execute(message);
    }
}
