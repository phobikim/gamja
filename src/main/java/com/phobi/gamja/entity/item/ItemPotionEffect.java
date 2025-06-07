package com.phobi.gamja.entity.item;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "item_potion_effect")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemPotionEffect {

    @Id
    @Column(name = "item_id")
    private Long itemId;

    @Column(name = "item_name")
    private String itemName;

    @Column(name = "heal_hp")
    private Integer healHp;

    @Column(name = "bonus_power")
    private Integer bonusPower;

    @Column(name = "duration_turns")
    private Integer durationTurns;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", insertable = false, updatable = false)
    private Item item; // item 테이블과 연결
}
