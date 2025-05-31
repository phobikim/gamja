package com.phobi.gamja.entity.user;

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
    private Long id; // user.id 를 외래키로 사용

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id")
    private User user;

    @Column(name = "user_power", nullable = false)
    private int userPower;

    @Column(name = "user_hp", nullable = false)
    private int userHp;

    @Column(name = "user_speed", nullable = false)
    private int userSpeed;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
