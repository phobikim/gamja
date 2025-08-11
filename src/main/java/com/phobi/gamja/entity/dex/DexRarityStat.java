package com.phobi.gamja.entity.dex;

import lombok.*;
import javax.persistence.*;
import java.util.List;
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

    private record RarityRate(Rarity rarity, double rate) {}
    public enum Rarity {
        COMMON, UNCOMMON, RARE, EPIC, LEGENDARY;

        private static final List<RarityRate> rateTable = List.of(
                new RarityRate(COMMON, 61.45),
                new RarityRate(UNCOMMON, 24.64),
                new RarityRate(RARE, 9.80),
                new RarityRate(EPIC, 4.0),
                new RarityRate(LEGENDARY, 0.1)
        );


        public static Rarity roll() {
            double rand = Math.random() * 100;
            double cumulative = 0.0;

            for (RarityRate rate : rateTable) {
                cumulative += rate.rate;
                if (rand < cumulative) return rate.rarity;
            }
            return COMMON; // fallback
        }
    }
}