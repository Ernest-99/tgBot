package org.example.javabot.bot;

import lombok.SneakyThrows;
import org.example.javabot.client.CallbackHandle;
import org.example.javabot.client.CommandHandler;
import org.example.javabot.client.MenuService;
import org.example.javabot.user.serviec.UserService;
import org.example.javabot.user.Role;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.Update;


@Component
public class UpdateConsumer implements LongPollingSingleThreadUpdateConsumer {

    private final UserService userService;
    private final MenuService menuService;
    private final CommandHandler commandHandler;
    private final CallbackHandle callbackHandle;

    public UpdateConsumer(UserService userService, CommandHandler commandHandler, MenuService menuService, CallbackHandle callbackHandle) {
        this.userService = userService;
        this.commandHandler = commandHandler;
        this.menuService = menuService;
        this.callbackHandle = callbackHandle;
    }


    //Загрузка документа с расписанием
    private void handleDocument(Update update, Long chatId) {

        Document document = update.getMessage().getDocument();
        String fileName = document.getFileName();

        if (fileName.equals("1course.xlsx") || fileName.equals("2course.xlsx") || fileName.equals("3course.xlsx")) {
            commandHandler.processExcelFile(chatId, document);
        } else {
            commandHandler.sendMessage(chatId,
                    "❗ Разрешена загрузка только файлов: 1course.xlsx, 2course.xlsx, 3course.xlsx");
        }
    }

    @SneakyThrows
    @Override
    public void consume(Update update) {

        if (update.hasMessage()) {
            String messageText = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();
            Role role = userService.getUserRole(chatId);

            if (messageText != null && messageText.equals("/start")) {
                //При запуске бота -> добавление в БД и приветствие пользователя
                userService.getOrCreateUser(chatId, update.getMessage().getFrom().getUserName(),
                        update.getMessage().getFrom().getFirstName(),update.getMessage().getFrom().getLastName());

                if (role == Role.ADMIN) {
                    menuService.sendAdminMenu(chatId);
                    menuService.sendMenu(chatId);
                    menuService.sendKeyboard(chatId);
                } else {
                    menuService.sendMenu(chatId);
                    menuService.sendKeyboard(chatId);
                }
            //Загрузка документа администратором
            } else if (update.getMessage().hasDocument() && role == Role.ADMIN) {

                handleDocument(update, chatId);

            } else if (messageText != null && messageText.startsWith("@") && role == Role.ADMIN) {
                String username = messageText.substring(1);
                userService.setAdminRole(username);

            }else if(messageText != null && messageText.equals("Получать расписание на сегодня")){
                commandHandler.sendMessage(chatId, "Принято) \nВыберите группу!");
                userService.setScheduleType(chatId, 0);
            }else if(messageText != null && messageText.equals("Получать расписание на неделю")){
                commandHandler.sendMessage(chatId, "Принято) \nВыберите группу!");
                userService.setScheduleType(chatId, 1);
            }  else {
                commandHandler.sendMessage(chatId, "Я вас не понимаю");
            }

        } else if (update.hasCallbackQuery()) {
            callbackHandle.handleCallback(update.getCallbackQuery());
        }
    }
}
