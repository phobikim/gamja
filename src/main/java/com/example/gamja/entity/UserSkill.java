package com.example.gamja.entity;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@IdClass(UserSkillId.class)
@Table(name = "user_skill")
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
}
