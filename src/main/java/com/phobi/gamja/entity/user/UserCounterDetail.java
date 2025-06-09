package com.phobi.gamja.entity.user;


import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_counter_detail")
@IdClass(UserCounterDetail.PK.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCounterDetail {
    @Id
    @Column(name = "user_id")
    private Long userId;

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "counter_type")
    private CounterType counterType;
    /**
     * counterType에 따라 targetId 의미:
     * MONSTER_KILL    → monster.id (처치한 몬스터 ID)
     * ITEM_CRAFT      → item.id (제작된 아이템 ID)
     * CHARACTER_DRAW  → dex 캐릭터의 rarity 코드 (COMMON=1, ..., LEGENDARY=5)
     * LIFE_ACTION     → 생활 활동 코드 (FISHING=1, WOODCUTTING=2, MINING=3, GATHERING=4)
     */
    @Id
    @Column(name = "target_id")
    private Long targetId;

    @Column(name = "counter_value", nullable = false)
    private Integer counterValue = 0;

    @Column(name = "created_at", updatable = false, insertable = false, columnDefinition = "timestamp default current_timestamp")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, columnDefinition = "timestamp default current_timestamp on update current_timestamp")
    private LocalDateTime updatedAt;

    // 복합 키 정의
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PK implements Serializable {
        private Long userId;
        private CounterType counterType;
        private Long targetId;
    }
}
