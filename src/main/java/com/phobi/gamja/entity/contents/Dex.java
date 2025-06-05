package com.phobi.gamja.entity.contents;
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
    private String rarity;

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
}