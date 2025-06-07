package com.phobi.gamja.entity.dex;

import lombok.*;
import javax.persistence.*;

@Entity
@Table(name = "dex_rarity_stat")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DexRarityStat {

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "rarity", nullable = false)
    private Rarity rarity;

    @Column(name = "base_hp", nullable = false)
    private int baseHp;

    @Column(name = "base_power", nullable = false)
    private int basePower;

    @Column(name = "base_speed", nullable = false)
    private int baseSpeed;

    @Column(name = "bonus_description")
    private String bonusDescription;

    public enum Rarity {
        COMMON, UNCOMMON, RARE, EPIC, LEGENDARY;

        public static Rarity roll() {
            double rand = Math.random() * 100;
            if (rand < 60) return COMMON;
            if (rand < 85) return UNCOMMON;
            if (rand < 95) return RARE;
            if (rand < 99) return EPIC;
            return LEGENDARY;
        }
    }
}