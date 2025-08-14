package org.example.javabot.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.example.javabot.user.Role;

@Data
@Entity
@Table(name = "users")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long chatId;

    private String username;
    @Enumerated(EnumType.STRING)
    private Role role;

    private boolean weeklySchedule;
}
