package com.phobi.gamja.entity.contents;

import lombok.Getter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "action")
public class Action {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityType category; // FISHING, MINING 등

    @Column(nullable = false)
    private int rank;
    private int requiredLevel;
    @Column(name = "display_name", nullable = false)
    private String displayName;

    private String description;

    @Column(name = "icon_path")
    private String iconPath;

    @Column(name = "is_enabled", nullable = false)
    private boolean isEnabled = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
