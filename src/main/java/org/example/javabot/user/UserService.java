package org.example.javabot.user;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserService {

    // Можно заменить на базу данных позже
    private final Map<Long, BotUser> users = new HashMap<>();

    // Задать роли вручную
    public UserService() {
        users.put(1086068083L, new BotUser(1086068083L, Role.ADMIN)); // пример chatId админа
    }

    public BotUser getOrCreateUser(Long chatId) {
        return users.computeIfAbsent(chatId, id -> new BotUser(id, Role.USER));
    }

    public boolean isAdmin(Long chatId) {
        return getOrCreateUser(chatId).getRole() == Role.ADMIN;
    }

    public void promoteToAdmin(Long chatId) {
        getOrCreateUser(chatId).setRole(Role.ADMIN);
    }

    public void demoteToUser(Long chatId) {
        getOrCreateUser(chatId).setRole(Role.USER);
    }
}
