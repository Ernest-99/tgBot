package org.example.javabot.user.serviec;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.javabot.user.Role;
import org.example.javabot.user.Status;
import org.example.javabot.user.entity.UserEntity;
import org.example.javabot.user.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;


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
                    newUser.setRegisteredDate(LocalDateTime.now());
                    return userRepository.save(newUser);
                });
    }
    @Transactional
    public void setAdminRole(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            if ( user.getUsername().equals(username)){
                user.setRole(Role.ADMIN);
                userRepository.save(user);
            }
        });
    }
    @Transactional
    public void setScheduleType(Long chatId, int shceduleType) {
        userRepository.findByChatId(chatId).ifPresent(user -> {
            user.setScheduleType(shceduleType);
            userRepository.save(user);
        });
    }

    public Role getUserRole(Long chatId) {
        Optional<UserEntity> optionalUser = userRepository.findByChatId(chatId);
        if (optionalUser.isPresent()) {
            UserEntity user = optionalUser.get();
            return user.getRole();
        } else {
            return null; // или можно бросить исключение
        }
    }

}
