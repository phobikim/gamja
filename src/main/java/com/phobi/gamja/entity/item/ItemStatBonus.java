package com.phobi.gamja.entity.item;

import lombok.Getter;

import javax.persistence.*;

@Entity
@Table(name = "item_stat_bonus")
@Getter
public class ItemStatBonus {

    @Id
    @Column(name = "item_id")
    private Long itemId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", insertable = false, updatable = false)
    private Item item;

    @Column(name = "bonus_hp")
    private int bonusHp;

    @Column(name = "bonus_power")
    private int bonusPower;

    @Column(name = "bonus_speed")
    private int bonusSpeed;

    // getter, setter 생략
}
