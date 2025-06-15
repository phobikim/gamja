package com.phobi.gamja.entity.quest;

import com.phobi.gamja.entity.user.CounterType;
import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "quest_condition")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestCondition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quest_id")
    private Quest quest;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CounterType counterType;

    @Column(nullable = false)
    private Long targetId;

    @Column(nullable = false)
    private int requiredCount;
}
