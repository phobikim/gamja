package com.example.gamja.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_resource_log")
public class UserResourceLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type", nullable = false)
    private DailyQuest.ActionType resourceType;

    @Column(nullable = false)
    private int amount = 1;

    @Column(name = "logged_at", nullable = false)
    private LocalDateTime loggedAt = LocalDateTime.now();

    // getter, setter 생략
}
