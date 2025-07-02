package com.phobi.gamja.entity.chronicle;

import com.phobi.gamja.entity.battle.MonsterMap;
import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "chronicle")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Chronicle {

    public enum ChronicleTargetType {
        ITEM, QUEST, FOOD
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "map_id", nullable = false)
    private MonsterMap map;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false)
    private ChronicleTargetType targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Column(name = "required_count", nullable = false)
    private int requiredCount;

    @Column(name = "order_in_ui")
    private Integer orderInUi;

    @Column(name = "use_flag", nullable = false)
    private boolean useFlag;
}

