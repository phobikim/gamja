package com.phobi.gamja.entity.dex;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rarity", referencedColumnName = "rarity")
    private DexRarityStat rarity;

    @Column(name = "acquire_condition", nullable = false, columnDefinition = "TEXT")
    private String acquireCondition;

    @Column(name = "user_flag", nullable = false)
    private boolean userFlag;

    private String attribute;
}