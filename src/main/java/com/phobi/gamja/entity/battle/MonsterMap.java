package com.phobi.gamja.entity.battle;

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

    @Column(name = "map_group_id", nullable = false)
    private Long mapGroupId;

    @Enumerated(EnumType.STRING)
    @Column(name = "map_difficulty", nullable = false)
    private MapDifficulty mapDifficulty;

    public enum MapDifficulty {
        NORMAL, HARD, BOSS
    }
}
