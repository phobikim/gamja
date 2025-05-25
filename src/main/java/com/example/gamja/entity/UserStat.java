package com.example.gamja.entity;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_stat")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStat {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dex_id", nullable = false)
    private Dex dex;

    @Column(name = "user_power", nullable = false)
    private int userPower;

    @Column(name = "user_shield", nullable = false)
    private int userShield;

    @Column(name = "user_hp", nullable = false)
    private int userHp;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
