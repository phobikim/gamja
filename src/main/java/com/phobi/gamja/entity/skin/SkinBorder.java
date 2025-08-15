package com.phobi.gamja.entity.skin;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "skin_border")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SkinBorder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 스킨명
    @Column(nullable = false, length = 100)
    private String name;

    // 설명
    @Column(columnDefinition = "TEXT")
    private String description;

    // 이미지 경로
    @Column(name = "image_url", nullable = false, length = 255)
    private String imageUrl;

    /** 사용 가능 여부 */
    @Builder.Default
    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}