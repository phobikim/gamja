package com.phobi.gamja.entity.title;
import com.phobi.gamja.entity.contents.SkillType;
import lombok.*;

import javax.persistence.*;
@Entity
@Table(name = "title_condition")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TitleCondition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "title_id", nullable = false)
    private Title title;

    @Column(name = "target_id")
    private Long targetId;

    @Column(name = "required_count")
    private int requiredCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "life_type")
    private SkillType lifeType;
}
