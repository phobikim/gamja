package com.phobi.gamja.entity.item;

import javax.persistence.*;
import lombok.*;

@Entity
@Table(name = "item_exp_bonus")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemExpBonus {

    @Id
    @Column(name = "item_id")
    private Long itemId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "item_id")
    private Item item;

    @Column(name = "item_name")
    private String itemName;

    @Column(name = "bonus_exp", nullable = false)
    private int bonusExp;
}