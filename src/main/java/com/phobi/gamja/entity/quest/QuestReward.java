package com.phobi.gamja.entity.quest;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "quest_reward")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestReward {

    public enum RewardType {
        ITEM, EXP, RANDOM_ITEM, GOLD
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quest_id")
    private Quest quest;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RewardType rewardType;

    @Column
    private Long itemId; // rewardType == ITEM 일 때만 사용

    @Column(nullable = false)
    private int amount;
}
