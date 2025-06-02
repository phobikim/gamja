package com.phobi.gamja.entity.contents;

import lombok.*;
import javax.persistence.*;

@Entity
@Table(name = "monster")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Monster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String desc;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "map_id")
    private MonsterMap map;

    private boolean enabled;
    private String rank;

    @Column(name = "image_path")
    private String imagePath;

    @Column(name = "monster_power")
    private int monsterPower;

    @Column(name = "monster_hp")
    private int monsterHp;

    @Column(name = "monster_xp")
    private int monsterXp;

    @Column(name = "drop_item1_id")
    private Long dropItem1Id;

    @Column(name = "drop_item2_id")
    private Long dropItem2Id;

    @Column(name = "drop_item3_id")
    private Long dropItem3Id;

    @Column(name = "drop_item4_id")
    private Long dropItem4Id;

    @Column(name = "drop_item5_id")
    private Long dropItem5Id;


}