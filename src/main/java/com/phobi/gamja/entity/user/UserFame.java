package com.phobi.gamja.entity.user;

import com.phobi.gamja.entity.fame.FameTier;
import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "user_fame",
        indexes = {
                @Index(name = "idx_fame", columnList = "fame_id,fame_level"),
                @Index(name = "idx_updated", columnList = "updated_at")
        })
public class UserFame {

    @Id
    @Column(name = "user_id", nullable = false)
    private Long userId;

    // FK -> fame_tier.fame_id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fame_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_user_fame_fame_tier"))
    private FameTier fameTier;

    @Column(name = "fame_level", nullable = false)
    private Integer fameLevel; // 표시/운영 편의 레벨

    @Column(name = "xp", nullable = false)
    private Integer xp; // 현재 명성 XP

    @Column(name = "max_xp", nullable = false)
    private Integer maxXp; // 다음 레벨까지 필요 XP

    @Column(name = "fame_point", nullable = false)
    private Integer famePoint; // 상점용 포인트

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt; // DB default CURRENT_TIMESTAMP

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt; // DB ON UPDATE CURRENT_TIMESTAMP
}