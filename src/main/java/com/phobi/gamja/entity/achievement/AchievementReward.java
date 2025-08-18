package com.phobi.gamja.entity.achievement;

import javax.persistence.*;

@Entity
@Table(name = "achievement_reward", schema = "gamja",
        indexes = {
                @Index(name = "idx_entry", columnList = "entry_id"),
                @Index(name = "idx_type",  columnList = "reward_type")
        })
public class AchievementReward {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK → achievement_entry.id
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "entry_id", nullable = false,
            foreignKey = @ForeignKey(name = "achievement_reward_ibfk_1"))
    private AchievementEntry entry;

    @Enumerated(EnumType.STRING)
    @Column(name = "reward_type", nullable = false, length = 20)
    private RewardType rewardType;

    @Column(nullable = false)
    private Integer amount = 0;

    @Column(name = "reward_key", length = 100)
    private String rewardKey;     // 코드형 보상(GOLD, SLOT_xxx 등)

    @Column(name = "reward_ref_id")
    private Long rewardRefId;     // 참조 테이블 id(title/item/skin 등)
}