package com.phobi.gamja.entity.dex;

import com.phobi.gamja.entity.battle.BattleSkill;
import lombok.*;
import javax.persistence.*;

@Entity
@Table(name = "dex_skill")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DexSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dex_attribute", nullable = false)
    private String dexAttribute;

    @Enumerated(EnumType.STRING)
    @Column(name = "skill_type", nullable = false)
    private BattleSkill.Type skillType;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "power_ratio", nullable = false)
    private float powerRatio = 1.0f;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BattleSkill.Target target = BattleSkill.Target.ENEMY;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BattleSkill.Effect effect = BattleSkill.Effect.DAMAGE;

    @Column(name = "effect_value")
    private Integer effectValue;

    @Column(name = "mp_cost", nullable = false)
    private int mpCost = 1;

    @Column(nullable = false)
    private int cooldown = 0;
}