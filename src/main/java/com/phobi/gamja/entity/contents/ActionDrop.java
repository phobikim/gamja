package com.phobi.gamja.entity.contents;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "action_drop")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActionDrop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", nullable = false)
    private ActivityType activityType;

    @Column(name = "spot_rank", nullable = false)
    private int spotRank;

    @Column(name = "item_id", nullable = false)
    private Long itemId;

    @Column(name = "drop_rate", nullable = false)
    private float dropRate;
    @Column(name = "exp_reward", nullable = false)
    private float expReward;

    @Column(name = "min_quantity")
    private int minQuantity = 1;

    @Column(name = "max_quantity")
    private int maxQuantity = 1;

    @Column(name = "required_skill_level")
    private int requiredSkillLevel = 1;

    @Column(name = "rarity_boost")
    private float rarityBoost = 1f;

    @Enumerated(EnumType.STRING)
    @Column(name = "time_condition", nullable = false)
    private TimeCondition timeCondition = TimeCondition.ANY;

    @Enumerated(EnumType.STRING)
    @Column(name = "season", nullable = false)
    private Season season = Season.ANY;

    @Column(length = 100)
    private String note;

    public enum TimeCondition {
        ANY, DAY, NIGHT
    }

    public enum Season {
        ANY, SPRING, SUMMER, FALL, WINTER
    }
}
