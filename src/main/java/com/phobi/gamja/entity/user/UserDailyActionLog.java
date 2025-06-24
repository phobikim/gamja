package com.phobi.gamja.entity.user;

import javax.persistence.*;

import com.phobi.gamja.entity.battle.Monster;
import com.phobi.gamja.entity.item.Item;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_daily_action_log")
@IdClass(UserDailyActionLogId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDailyActionLog {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Id
    @Column(name = "log_date")
    private LocalDate logDate;

    @Id
    @Column(name = "monster_id")
    private Long monsterId;

    @Id
    @Column(name = "item_id")
    private Long itemId;

    @Column(name = "count")
    private int count;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "monster_id", insertable = false, updatable = false)
    private Monster monster;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", insertable = false, updatable = false)
    private Item item;
}
