package com.phobi.gamja.entity.contents;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "monster_map")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonsterMap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String desc;

    @Column(name = "background_image_path")
    private String backgroundImagePath;

    @Column(name = "recommended_level")
    private String recommendedLevel;

    private boolean enabled;
}
