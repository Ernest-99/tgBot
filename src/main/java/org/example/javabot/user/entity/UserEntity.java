package org.example.javabot.user.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.example.javabot.user.Role;
import org.example.javabot.user.Status;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "users")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private Long chatId;

    private String username;

    private String fullName;

    @Enumerated(EnumType.STRING)
    private Role role;
    @Enumerated(EnumType.STRING)
    private Status status;

    private int scheduleType;

    private LocalDateTime registeredDate;

}
