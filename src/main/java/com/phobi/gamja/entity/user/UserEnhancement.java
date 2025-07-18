package com.phobi.gamja.entity.user;

import javax.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
@Entity
@Table(name = "user_enhancement")
@IdClass(UserEnhancementId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEnhancement implements Serializable {

    @Id
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Id
    @Column(name = "item_id", nullable = false)
    private Long itemId;

    @Column(name = "enhancement_level", nullable = false)
    private int enhancementLevel;

    @Column(name = "enhancement_xp", nullable = false)
    private int enhancementXp;

    @Column(name = "bonus_power", nullable = false)
    private int bonusPower;

    @Column(name = "bonus_hp", nullable = false)
    private int bonusHp;

    @Column(name = "bonus_speed", nullable = false)
    private int bonusSpeed;
    @Column(name = "enhancement_at")
    private LocalDateTime enhancementAt;
}