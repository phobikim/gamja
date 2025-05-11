package com.example.gamja.entity;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table(name = "user_skill")
public class UserSkill {
    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id")
    private User user;

    private int fishLevel = 1;
    private int woodLevel = 1;
    private int stoneLevel = 1;
    private int cookLevel = 1;
}
