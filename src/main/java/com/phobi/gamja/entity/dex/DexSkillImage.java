package com.phobi.gamja.entity.dex;

import lombok.*;
import javax.persistence.*;

@Entity
@Table(name = "dex_skill_image")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DexSkillImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id", nullable = false)
    private DexSkill skill;

    @Column(name = "frame_index", nullable = false)
    private int frameIndex;

    @Column(name = "image_path", nullable = false, length = 100)
    private String imagePath;
}