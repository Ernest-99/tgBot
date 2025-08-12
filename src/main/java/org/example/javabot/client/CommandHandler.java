package org.example.javabot.client;

import lombok.SneakyThrows;
import org.example.javabot.config.BotConfig;
import org.example.javabot.service.ExcelParserService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.*;

@Component
public class CommandHandler {

    private final BotConfig botConfig;
    private final TelegramClient telegramClient;
    private ExcelParserService excelParserService;
    private final MenuService menuService;

    public CommandHandler(BotConfig botConfig, ExcelParserService excelParserService, MenuService menuService) {
        this.botConfig = botConfig;
        this.telegramClient = new OkHttpTelegramClient(botConfig.getBotToken());
        this.excelParserService = excelParserService;
        this.menuService = menuService;
    }

    public void setTypeSchedule(boolean typeSchedule) {
        this.typeSchedule = typeSchedule;
    }

    private boolean typeSchedule = false; // По умслчанию получает расписание на сегодня = false

    private final Map<Long, Set<String>> userCoursesMap = new HashMap<>();

    public void handleCallbackQuery(CallbackQuery callbackQuery) {
        var data = callbackQuery.getData();
        var chatId = callbackQuery.getFrom().getId();



        // 2. Если выбран курс:
        String fileName1 = switch (data) {
            case "first_course" -> "1course.xlsx";
            case "second_course" -> "2course.xlsx";
            case "third_course" -> "3course.xlsx";
            default -> null;
        };

        if (data.startsWith("GROUP_")) {
            String groupName = data.substring("GROUP_".length());

            Set<String> selectedCourses = userCoursesMap.get(chatId);
            if (selectedCourses == null || selectedCourses.isEmpty()) {
                sendMessage(chatId, "Кэш был очищен, пожалуйста сначала выберите курс.");
                menuService.sendMenu(chatId);
                return;
            }

            StringBuilder fullSchedule = new StringBuilder();
            for (String fileName : selectedCourses) {
                File file = new File("courses/" + fileName);
                if (!file.exists()) {
                    fullSchedule.append("❌ Файл не найден: ").append(fileName).append("\n\n");
                    continue;
                }
                String schedule;
                if (typeSchedule){
                    schedule = excelParserService.getScheduleByGroupForWeek(file, groupName);
                }else {
                    schedule = excelParserService.getScheduleByGroupToday(file, groupName);
                }


                String course = switch (fileName) {
                    case "1course.xlsx" -> "1 курс";
                    case "2course.xlsx" -> "2 курс";
                    case "3course.xlsx" -> "3 курс";
                    default -> null;
                };

                if(!schedule.isEmpty() && !schedule.equals("❌ Не найден лист с названием группы: " + groupName)) {
                    fullSchedule.append("📘 ").append(course).append("\n").append(schedule).append("\n\n");
                }

            }

            if(fullSchedule.isEmpty()){
                sendMessage(chatId, "Выбранная группа не соответствует курсу, пожалуйста сначала выберите курс.");
                menuService.sendMenu(chatId);
            }else {
                sendMessage(chatId, fullSchedule.toString());
            }
            return;
        }



        if (fileName1 != null) {
            // Добавляем курс в список выбранных
            userCoursesMap.computeIfAbsent(chatId, k -> new HashSet<>()).add(fileName1);
            //sendMessage(chatId, "✅ Курс добавлен: " + fileName1 + "\nТеперь выберите группу.");
            sendGroupSelectionMenu(chatId, new File("courses/" + fileName1)); // Показываем кнопки с группами
        } else {
            sendMessage(chatId, "Неизвестная команда.");
        }
    }

    public boolean handleCallbackQueryForAdmin(CallbackQuery callbackQuery , String fileName) {
        var data = callbackQuery.getData();
        var chatId = callbackQuery.getFrom().getId();

        switch (data) {
            case "schedule_first_course" -> sendMessage(chatId, "Ожидаю документ");
            case "schedule_second_course" -> sendMessage(chatId, "Ожидаю документ");
            case "schedule_third_course" -> sendMessage(chatId, "Ожидаю документ");
            default -> sendMessage(chatId, "Неизвестная команда");
        };

        sendMessage(chatId, "Ожидаю документ");

        if (fileName.equals("1course.xlsx") || fileName.equals("2course.xlsx") || fileName.equals("3course.xlsx")) {
            // Обработка Excel, например админ загрузил файл
            return true;
        }
        return false;
    }

    @SneakyThrows
    public void sendMessage(Long chatId, String messageText) {
        SendMessage message = SendMessage.builder().text(messageText).chatId(chatId).build();
        telegramClient.execute(message);
    }

    // Получение списка групп
    @SneakyThrows
    public void sendGroupSelectionMenu(Long chatId, File excelFile) {
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

 
    //Загрузка Exel файла в бота
    @SneakyThrows
    public void processExcelFile(Long chatId, Document document) {
        try {
            String fileId = document.getFileId();
            String originalFileName = document.getFileName(); // напр. "1course.xlsx"

            // Проверка, что файл — Excel
            if (!originalFileName.endsWith(".xlsx")) {
                telegramClient.execute(new SendMessage(chatId.toString(), "Пожалуйста, загрузите файл с расширением .xlsx"));
                return;
            }

            // Получаем информацию о файле
            GetFile getFile = new GetFile(fileId);
            org.telegram.telegrambots.meta.api.objects.File tgFile = telegramClient.execute(getFile);

            // Формируем ссылку на файл
            String fileUrl = "https://api.telegram.org/file/bot" + botConfig.getBotToken() + "/" + tgFile.getFilePath();

            // Создаём директорию при необходимости
            File saveDir = new File("courses");
            if (!saveDir.exists()) saveDir.mkdirs();

            // Путь к сохранённому файлу
            File savedFile = new File(saveDir, originalFileName);

            // Скачиваем и сохраняем
            try (InputStream in = new URL(fileUrl).openStream();
                 OutputStream out = new FileOutputStream(savedFile)) {
                in.transferTo(out);
            }

            System.out.println("Сохранён файл: " + savedFile.getAbsolutePath());

            // Уведомляем пользователя
            telegramClient.execute(new SendMessage(chatId.toString(), "Файл " + originalFileName + " загружен и сохранён успешно."));

        } catch (Exception e) {
            e.printStackTrace();
            telegramClient.execute(new SendMessage(chatId.toString(), "Ошибка при обработке файла: " + e.getMessage()));
        }
    }

}
