package org.example.javabot.service;

import lombok.RequiredArgsConstructor;
import org.example.javabot.entity.UserEntity;
import org.example.javabot.repositories.UserRepository;
import org.example.javabot.user.Role;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class UserServiceEntity {
    private final UserRepository userRepository;

    public UserEntity getOrCreateUser(Long chatId, String username) {
        return userRepository.findById(chatId)
                .orElseGet(() -> {
                    UserEntity user = new UserEntity();
                    user.setChatId(chatId);
                    user.setUsername(username);
                    user.setRole(Role.USER);
                    return userRepository.save(user);
                });
    }

    public void setRole(Long chatId, Role role) {
        userRepository.findById(chatId).ifPresent(user -> {
            user.setRole(role);
            userRepository.save(user);
        });
    }
}
