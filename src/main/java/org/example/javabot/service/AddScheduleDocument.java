package org.example.javabot.service;

import lombok.SneakyThrows;
import org.example.javabot.config.BotConfig;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

@Component
public class AddScheduleDocument {

    private final TelegramClient telegramClient;
    private final BotConfig botConfig;

    public AddScheduleDocument( BotConfig botConfig) {
        this.telegramClient = new OkHttpTelegramClient(botConfig.getBotToken());
        this.botConfig = botConfig;
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
