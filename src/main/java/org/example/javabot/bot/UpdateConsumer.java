package org.example.javabot.bot;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.example.javabot.client.CommandHandler;
import org.example.javabot.client.MenuService;
import org.example.javabot.entity.UserEntity;
import org.example.javabot.service.UserServiceEntity;
import org.example.javabot.user.Role;
import org.example.javabot.user.UserService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;


@Component
@RequiredArgsConstructor
public class UpdateConsumer implements LongPollingSingleThreadUpdateConsumer {

    private final CommandHandler commandHandler;
    private final MenuService menuService;
    private final UserService userService;

    private final Map<String, BiConsumer<UserEntity, String>> textCommands = new HashMap<>();
    private final UserServiceEntity userServiceEntity;

    @PostConstruct
    private void initCommands() {
        textCommands.put("/start", (user, msg) -> {
            if (user.getRole() == Role.ADMIN) {
                menuService.sendAdminMenu(user.getChatId());
            } else {
                menuService.sendMenu(user.getChatId());
                menuService.sendKeyboard(user.getChatId());
            }
        });

        textCommands.put("Получать расписание на сегодня", (user, msg) -> {
            commandHandler.sendMessage(user.getChatId(), "Принято");
            commandHandler.setTypeSchedule(false);
        });

        textCommands.put("Получать расписание на неделю", (user, msg) -> {
            commandHandler.sendMessage(user.getChatId(), "Принято");
            commandHandler.setTypeSchedule(true);
        });

        // 🔹 Пример шестого пункта
        textCommands.put("Показать контакт администратора", (user, msg) -> {
            commandHandler.sendMessage(user.getChatId(), "Контакт администратора: @admin_username");
        });
    }

    @SneakyThrows
    @Override
    public void consume(Update update) {
        if (update.hasMessage()) {
            String messageText = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();

            // ✅ получаем пользователя с userName
            UserEntity user = userServiceEntity.getOrCreateUser(chatId, update.getMessage().getFrom().getUserName());

            if (update.getMessage().hasDocument()) {
                handleDocument(update, user);
                return;
            }

            if (messageText != null && textCommands.containsKey(messageText)) {
                textCommands.get(messageText).accept(user, messageText);
            } else {
                commandHandler.sendMessage(chatId, "Я вас не понимаю");
            }

        } else if (update.hasCallbackQuery()) {
            commandHandler.handleCallbackQuery(update.getCallbackQuery());
        }
    }

    private void handleDocument(Update update, UserEntity user) {
        if (user.getRole() != Role.ADMIN) {
            commandHandler.sendMessage(user.getChatId(), "❌ У вас нет прав загружать файлы!");
            return;
        }

        Document document = update.getMessage().getDocument();
        String fileName = document.getFileName();

        if (fileName.equals("1course.xlsx") || fileName.equals("2course.xlsx") || fileName.equals("3course.xlsx")) {
            commandHandler.processExcelFile(user.getChatId(), document);
        } else {
            commandHandler.sendMessage(user.getChatId(),
                    "❗ Разрешена загрузка только файлов: 1course.xlsx, 2course.xlsx, 3course.xlsx");
        }
    }
}
