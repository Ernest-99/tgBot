package org.example.javabot.user;

public class BotUser {
    private Long chatId;
    private Role role;

    public BotUser(Long chatId, Role role) {
        this.chatId = chatId;
        this.role = role;
    }

    public Long getChatId() {
        return chatId;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
