package com.example.gamja.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Data
@NoArgsConstructor
@Table(name = "skill_shop")
public class SkillShop {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String skillType; // fishing, woodcutting, mining, cooking

    private String skillName;

    private String skillDescription;

    private int price;
}
