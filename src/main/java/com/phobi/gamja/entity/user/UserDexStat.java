package com.phobi.gamja.entity.user;

import com.phobi.gamja.entity.contents.Dex;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_dex_stat")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDexStat {

    @EmbeddedId
    private UserDexStatId id;

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @MapsId("dexId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dex_id")
    private Dex dex;

    @Column(name = "level", nullable = false)
    private int level;

    @Column(name = "xp", nullable = false)
    private int xp;

    @Column(name = "max_exp", nullable = false)
    private int maxExp;

    @Column(name = "power", nullable = false)
    private int power;

    @Column(name = "hp", nullable = false)
    private int hp;

    @Column(name = "speed", nullable = false)
    private int speed;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}