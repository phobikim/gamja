package com.phobi.gamja.entity.achievement;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "achievement", schema = "gamja",
        uniqueConstraints = @UniqueConstraint(name = "uk_series_key", columnNames = "series_key"),
        indexes = {
                @Index(name = "idx_category", columnList = "category"),
                @Index(name = "idx_enabled",  columnList = "enabled")
        })
public class Achievement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AchievementCategory category;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 255)
    private String description;

    @Column(name = "series_key", nullable = false, length = 64)
    private String seriesKey;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "created_at", nullable = false, updatable = false,
            columnDefinition = "datetime default current_timestamp()")
    private LocalDateTime createdAt;
}