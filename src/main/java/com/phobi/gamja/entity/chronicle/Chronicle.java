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

    /*퀘스트 필터링 용 (퀘스트에서 itemId는 item, food를 구분 못하는 이슈 */
    public enum ChronicleTargetType {
        ITEM, QUEST, MONSTER
    }

    /*연대기 모달에서 수집품 필터링용 */
    public enum ChronicleCategory {
        DROP, FOOD, MONSTER // 수집품 / 음식 / 몬스터
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

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private ChronicleCategory category;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Column(name = "required_count", nullable = false)
    private int requiredCount;

    @Column(name = "order_in_ui")
    private Integer orderInUi;

    @Column(name = "use_flag", nullable = false)
    private boolean useFlag;
}

