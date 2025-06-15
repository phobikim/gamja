package com.phobi.gamja.entity.dex;

import lombok.*;
import javax.persistence.*;
import java.util.Map;

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

        private static final Map<Rarity, Double> rateTable = Map.of(
                COMMON, 60.0,
                UNCOMMON, 25.0,
                RARE, 10.0,
                EPIC, 4.0,
                LEGENDARY, 1.0
        );

        public static Rarity roll() {
            double rand = Math.random() * 100;
            double cumulative = 0.0;

            for (Map.Entry<Rarity, Double> entry : rateTable.entrySet()) {
                cumulative += entry.getValue();
                if (rand < cumulative) {
                    return entry.getKey();
                }
            }

            // fallback (이론상 도달 불가)
            return COMMON;
        }
    }
}