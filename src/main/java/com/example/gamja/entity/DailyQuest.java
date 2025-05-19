package com.example.gamja.entity;

import lombok.*;
import javax.persistence.*;
import java.time.DayOfWeek;

@Entity
@Data
@Table(name = "daily_quest")
public class DailyQuest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DayOfWeek day;
    @Column(columnDefinition = "TEXT")
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActionType action;

    @Column(name = "goal_count", nullable = false)
    private int goalCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "reward_type", nullable = false)
    private RewardType rewardType;

    @Column(name = "reward_value", nullable = false)
    private int rewardValue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Difficulty difficulty = Difficulty.보통;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    // getter, setter 생략
    public enum ActionType {
        fish, wood, rock, dish,
        rare_fish, rare_wood, rare_rock, rare_dish,
        login
    }

    public enum RewardType {
        xp, gold, ticket
    }

    public enum Difficulty {
        쉬움, 보통, 어려움
    }
}


