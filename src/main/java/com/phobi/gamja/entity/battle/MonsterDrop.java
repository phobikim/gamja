package com.phobi.gamja.entity.battle;

import com.phobi.gamja.entity.item.Item;
import javax.persistence.*;
import lombok.*;

@Entity
@Table(name = "monster_drop")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonsterDrop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 연관된 몬스터
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "monster_id", nullable = false)
    private Monster monster;

    // 드랍 아이템
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    // 드랍 확률 (0.0 ~ 100.0)
    @Column(name = "drop_rate", nullable = false)
    private Float dropRate;

    // 최소 드랍 수량
    @Column(name = "min_count", nullable = false)
    private Integer minCount;

    // 최대 드랍 수량
    @Column(name = "max_count", nullable = false)
    private Integer maxCount;

}