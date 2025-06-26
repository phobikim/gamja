package com.phobi.gamja.entity.user;

import javax.persistence.*;

import com.phobi.gamja.entity.battle.Monster;
import com.phobi.gamja.entity.item.Item;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_daily_action_log",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "log_date", "monster_id", "item_id"})
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDailyActionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // ✅ 단일 PK

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "log_date", nullable = false)
    private LocalDate logDate;

    @Column(name = "monster_id")
    private Long monsterId;

    @Column(name = "item_id")
    private Long itemId;

    @Column(name = "count", nullable = false)
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