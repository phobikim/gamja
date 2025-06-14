package com.phobi.gamja.entity.title;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "title_effect")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TitleEffect {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "title_id", nullable = false)
    private Title title;

    @Enumerated(EnumType.STRING)
    @Column(name = "effect_type", nullable = false)
    private EffectType effectType;

    @Column(name = "effect_value", nullable = false)
    private int effectValue;

    public enum EffectType {
        BONUS_ATTACK, BONUS_HP
    }
}
