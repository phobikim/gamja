package com.phobi.gamja.entity.achievement;


import lombok.Getter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "achievement_entry", schema = "gamja",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_meta_req_target",
                columnNames = {
                        "achievement_id","requirement_type",
                        "norm_character_id","norm_monster_id","norm_item_id",
                        "requirement_value"
                }
        ),
        indexes = {
                @Index(name = "idx_meta_order", columnList = "achievement_id,order_in_series"),
                @Index(name = "idx_req",        columnList = "requirement_type,requirement_value"),
                @Index(name = "idx_char",       columnList = "character_id"),
                @Index(name = "idx_monster",    columnList = "monster_id"),
                @Index(name = "idx_item",       columnList = "item_id"),
                @Index(name = "idx_enabled",    columnList = "enabled")
        })
public class AchievementEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK → achievement.id
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "achievement_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_entry_ach"))
    private Achievement achievement;

    @Column(nullable = false, length = 255)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "requirement_type", nullable = false, length = 20)
    private RequirementType requirementType;

    @Column(name = "requirement_value", nullable = false)
    private Integer requirementValue = 1;

    // 외부 테이블 의존 줄이기 위해 일단 id만 보유
    @Column(name = "character_id")
    private Long characterId;  // gamja.dex.id

    @Column(name = "monster_id")
    private Long monsterId;    // gamja.monster.id

    @Column(name = "item_id")
    private Long itemId;       // gamja.item.id

    @Column(name = "order_in_series", nullable = false)
    private Integer orderInSeries = 1;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "created_at", nullable = false, updatable = false,
            columnDefinition = "datetime default current_timestamp()")
    private LocalDateTime createdAt;

    // 계산(STORED) 컬럼: 읽기 전용 매핑
    @Column(name = "norm_character_id", insertable = false, updatable = false)
    private Long normCharacterId;

    @Column(name = "norm_monster_id", insertable = false, updatable = false)
    private Long normMonsterId;

    @Column(name = "norm_item_id", insertable = false, updatable = false)
    private Long normItemId;

}