package com.phobi.gamja.entity.user;

import com.phobi.gamja.entity.achievement.AchievementEntry;
import com.phobi.gamja.entity.achievement.AchievementStatus;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Table(name = "user_achievement", schema = "gamja",
        uniqueConstraints = @UniqueConstraint(name = "uk_user_entry", columnNames = {"user_id","entry_id"}),
        indexes = {
                @Index(name = "fk_ua_entry",     columnList = "entry_id"),
                @Index(name = "idx_user_status", columnList = "user_id,status"),
                @Index(name = "idx_updated",     columnList = "last_updated_at")
        })
public class UserAchievement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 유저 테이블 FK는 스키마에 없으므로 id만 보유
    @Column(name = "user_id", nullable = false)
    private Long userId;

    // FK → achievement_entry.id
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "entry_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_ua_entry"))
    private AchievementEntry entry;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AchievementStatus status = AchievementStatus.IN_PROGRESS;

    @Column(name = "progress_count", nullable = false)
    private Integer progressCount = 0;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "rewarded_at")
    private LocalDateTime rewardedAt;

    @Column(name = "last_updated_at", nullable = false,
            columnDefinition = "datetime default current_timestamp() on update current_timestamp()")
    private LocalDateTime lastUpdatedAt;
}