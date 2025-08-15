package org.example.javabot.bot;

import lombok.SneakyThrows;
import org.example.javabot.client.CommandHandler;
import org.example.javabot.client.MenuService;
import org.example.javabot.user.entity.UserEntity;
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

    public UpdateConsumer(UserService userService, UserService userServiceEntity, CommandHandler commandHandler, MenuService menuService) {
        this.userService = userService;
        this.commandHandler = commandHandler;
        this.menuService = menuService;

    }


    //Загрузка документа с расписанием
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

    @SneakyThrows
    @Override
    public void consume(Update update) {

        if (update.hasMessage()) {
            String messageText = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();


            if (messageText != null && messageText.equals("/start")) {
                menuService.sendMenu(chatId);
                menuService.sendKeyboard(chatId);
                UserEntity user = userService.getOrCreateUser(chatId, update.getMessage().getFrom().getUserName(),
                        update.getMessage().getFrom().getFirstName(),update.getMessage().getFrom().getLastName());


//                Role role = userService.getOrCreateUser(chatId).getRole();
//                commandHandler.sendMessage(chatId, "Вы вошли как: " + role);
//                if (role == Role.ADMIN) {
//                    menuService.sendAdminMenu(chatId);
//                } else {
//                    menuService.sendMenu(chatId);
//                    menuService.sendKeyboard(chatId);
//                }
            } else if (update.getMessage().hasDocument()) {

                //handleDocument(update, user);

            }else if(messageText != null && messageText.equals("Получать расписание на сегодня")){
                commandHandler.sendMessage(chatId, "Принято");
                userService.setScheduleType(chatId, 0);
            }else if(messageText != null && messageText.equals("Получать расписание на неделю")){
                commandHandler.sendMessage(chatId, "Принято");
                userService.setScheduleType(chatId, 1);
            }  else {
                commandHandler.sendMessage(chatId, "Я вас не понимаю");
            }

        } else if (update.hasCallbackQuery()) {
            commandHandler.handleCallbackQuery(update.getCallbackQuery());
        }
    }
}
