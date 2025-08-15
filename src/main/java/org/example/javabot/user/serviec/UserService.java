package org.example.javabot.user.serviec;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.javabot.user.Role;
import org.example.javabot.user.Status;
import org.example.javabot.user.entity.UserEntity;
import org.example.javabot.user.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public UserEntity getOrCreateUser(Long chatId, String username, String firstName, String lastName) {
        return userRepository.findByChatId(chatId)
                .map(user -> {
                    // Если username изменился — обновляем
                    if (username != null && !username.equals(user.getUsername())) {
                        user.setUsername(username);
                        userRepository.save(user);
                    }
                    return user;
                })
                .orElseGet(() -> {
                    UserEntity newUser = new UserEntity();
                    newUser.setChatId(chatId);
                    newUser.setUsername(username);
                    newUser.setFullName(firstName + " " + lastName);
                    newUser.setRole(Role.USER);
                    newUser.setStatus(Status.ACTIVE);
                    newUser.setRegisteredAt(LocalDateTime.now());
                    return userRepository.save(newUser);
                });
    }

    public void setRole(Long chatId, Role role) {
        userRepository.findById(chatId).ifPresent(user -> {
            user.setRole(role);
            userRepository.save(user);
        });
    }
    @Transactional
    public void setScheduleType(Long chatId, int shceduleType) {
        userRepository.findByChatId(chatId).ifPresent(user -> {
            user.setScheduleType(shceduleType);
            userRepository.save(user);
        });
    }

    @Transactional
    public void setStudentGroup(Long chatId, int shceduleType) {
        userRepository.findByChatId(chatId).ifPresent(user -> {
            user.setScheduleType(shceduleType);
            userRepository.save(user);
        });
    }

}
