package com.phobi.gamja.entity;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class UserSkillId implements Serializable {
    private Long userId;
    private SkillType skillType;
}
