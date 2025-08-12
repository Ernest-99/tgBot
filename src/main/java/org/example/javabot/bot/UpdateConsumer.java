package org.example.javabot.bot;

import lombok.SneakyThrows;
import org.example.javabot.client.CommandHandler;
import org.example.javabot.client.MenuService;
import org.example.javabot.user.Role;
import org.example.javabot.user.UserService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.Update;


@Component
public class UpdateConsumer implements LongPollingSingleThreadUpdateConsumer {

    private final UserService userService;
    private MenuService menuService;

    private final CommandHandler commandHandler;

    public UpdateConsumer(UserService userService, MenuService menuService, CommandHandler commandHandler) {
        this.userService = userService;
        this.menuService = menuService;
        this.commandHandler = commandHandler;
    }

    @SneakyThrows
    @Override
    public void consume(Update update) {

        if (update.hasMessage()) {
            String messageText = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();

            if (messageText != null && messageText.equals("/start")) {
                Role role = userService.getOrCreateUser(chatId).getRole();
                //commandHandler.sendMessage(chatId, "Вы вошли как: " + role);

                if (role == Role.ADMIN) {
                    menuService.sendAdminMenu(chatId);
                } else {
                    menuService.sendMenu(chatId);
                    menuService.sendKeyboard(chatId);
                }
            } else if (update.getMessage().hasDocument()) {
                // Проверка роли перед обработкой Excel
                Role role = userService.getOrCreateUser(chatId).getRole();

                if (role == Role.ADMIN) {
                    Document document = update.getMessage().getDocument();
                    String fileName = document.getFileName();

                    if (fileName.equals("1course.xlsx") || fileName.equals("2course.xlsx") || fileName.equals("3course.xlsx")) {
                        commandHandler.processExcelFile(chatId, document);
                    } else {
                        commandHandler.sendMessage(chatId, "❗ Разрешена загрузка только файлов: 1course.xlsx, 2course.xlsx, 3course.xlsx");
                    }
                } else {
                    commandHandler.sendMessage(chatId, "❌ Только администратор может загружать файлы.");
                }

            }else if(messageText != null && messageText.equals("Получать расписание на сегодня")){
                commandHandler.sendMessage(chatId, "Принято");
                commandHandler.setTypeSchedule(false);
            }else if(messageText != null && messageText.equals("Получать расписание на неделю")){
                commandHandler.sendMessage(chatId, "Принято");
                commandHandler.setTypeSchedule(true);
            }  else {
                commandHandler.sendMessage(chatId, "Я вас не понимаю");
            }

        } else if (update.hasCallbackQuery()) {
            commandHandler.handleCallbackQuery(update.getCallbackQuery());
        }
    }
}
