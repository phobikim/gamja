package com.phobi.gamja.entity.user;

import com.phobi.gamja.entity.contents.SkillType;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Data
@IdClass(UserSkillId.class)
@Table(name = "user_skill")
@NoArgsConstructor
public class UserSkill {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "skill_type")
    private SkillType skillType;

    @Column(nullable = false)
    private int level = 1;

    @Column(nullable = false)
    private int exp = 0;

    @Column(name = "max_combo")
    private Integer maxCombo = 0;

    public UserSkill(Long userId, SkillType skillType, int level, int exp) {
        this.userId = userId;
        this.skillType = skillType;
        this.level = level;
        this.exp = exp;
        this.maxCombo = 0; // 필요시 초기화
    }

}
