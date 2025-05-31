package com.phobi.gamja.entity.item;

import lombok.Getter;

import javax.persistence.*;

@Entity
@Table(name = "item_skill_bonus")
@Getter
public class ItemSkillBonus {

    @Id
    @Column(name = "item_id")
    private Long itemId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", insertable = false, updatable = false)
    private Item item;

    private int fishing;
    private int mining;
    private int woodcutting;
    private int gathering;
    private int making;

    // getter, setter 생략
}
