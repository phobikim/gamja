package com.phobi.gamja.entity.battle;
import javax.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "monster_boss_pattern")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonsterBossPattern {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 몬스터 ID (외래키)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "monster_id", nullable = false)
    private Monster monster;

    // 페이즈 (0 = 기본 패턴)
    @Column(nullable = false)
    private int phase;

    @Column(name = "phase_order", nullable = false)
    private int phaseOrder;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String dialogue;

    @Enumerated(EnumType.STRING)
    @Column(name = "pattern_type", nullable = false)
    private PatternType patternType;

    @Column(name = "pattern_value", nullable = false)
    private int patternValue;

    @Column(name = "is_repeatable", nullable = false)
    private boolean isRepeatable;

    @Column(name = "is_enabled", nullable = false)
    private boolean isEnabled;

    @Column(name = "cooldown")
    private Integer cooldown;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public enum PatternType {
        DAMAGE_TO_PLAYER,
        HEAL_SELF,
        DEBUFF_PLAYER
    }
}