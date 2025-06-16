package com.phobi.gamja.entity.quest;
import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
@Entity
@Table(name = "quest")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Quest {

    public enum QuestType {
        MAIN,      // 메인 퀘스트
        HUNT,     // 토벌 퀘스트
        REQUEST   // 아이템 납품 퀘스트
    }
    public enum QuestDifficulty {
        EASY, NORMAL, HARD
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "grade", nullable = false)
    private QuestDifficulty grade;

    @Column(name = "is_repeatable", nullable = false)
    private boolean repeatable;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @Column(name = "main_order")
    private Integer mainOrder;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "quest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuestCondition> conditions;

    @OneToMany(mappedBy = "quest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuestReward> rewards;
}