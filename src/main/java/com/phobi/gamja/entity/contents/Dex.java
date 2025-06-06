package com.phobi.gamja.entity.contents;
import com.phobi.gamja.entity.item.Item;
import lombok.*;
import javax.persistence.*;

@Entity
@Table(name = "dex")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Dex {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 50)
    private String description;

    @Column(nullable = false, length = 100)
    private String image;

    @Column(nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private DexRarity rarity;

    @Column(name = "acquire_condition", nullable = false, columnDefinition = "TEXT")
    private String acquireCondition;

    @Column(name = "acquired_count", nullable = false)
    private int acquiredCount;

    @Column(name = "user_flag", nullable = false)
    private boolean userFlag;

    private int dexPower;
    private int dexHp;
    private int dexSpeed;
    private String attribute;

    public enum DexRarity {
        COMMON, UNCOMMON, RARE, EPIC, LEGENDARY;

    }

    public static DexRarity rollRarity() {
        double rand = Math.random() * 100;
        if (rand < 60) return DexRarity.COMMON;
        if (rand < 85) return DexRarity.UNCOMMON;
        if (rand < 95) return DexRarity.RARE;
        if (rand < 99) return DexRarity.EPIC;
        return DexRarity.LEGENDARY;
    }
}