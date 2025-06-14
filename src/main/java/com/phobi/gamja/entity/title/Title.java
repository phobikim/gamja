package com.phobi.gamja.entity.title;

import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "title")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Title {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "counter_type", nullable = false)
    private CounterType counterType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Column(name = "required_count", nullable = false)
    private int requiredCount;

    @Enumerated(EnumType.STRING)
    private Rarity rarity;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "title", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TitleEffect> effects;

    public enum CounterType {
        MONSTER_KILL, ITEM_CRAFT, CHARACTER_DRAW, LIFE_ACTION
    }

    public enum Rarity {
        COMMON, UNCOMMON, RARE, LEGENDARY
    }
}
