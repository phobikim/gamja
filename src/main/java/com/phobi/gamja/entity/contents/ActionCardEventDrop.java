package com.phobi.gamja.entity.contents;

import com.phobi.gamja.entity.item.Item;
import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "action_card_event_drop")
@Getter
@Setter
public class ActionCardEventDrop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "drop_group_id", nullable = false)
    private Long dropGroupId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    @Column(name = "drop_rate", nullable = false)
    private float dropRate;

    @Column(name = "min_quantity")
    private int minQuantity;

    @Column(name = "max_quantity")
    private int maxQuantity;

    @Column(name = "exp_reward")
    private float expReward;
}
