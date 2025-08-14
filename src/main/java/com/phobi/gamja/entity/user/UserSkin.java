package com.phobi.gamja.entity.user;

import com.phobi.gamja.entity.skin.BackgroundImage;
import com.phobi.gamja.entity.skin.SkinBorder;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_skin",
        indexes = {
                @Index(name = "idx_user_skin_user_id", columnList = "user_id"),
                @Index(name = "idx_user_skin_bg", columnList = "skin_background_id"),
                @Index(name = "idx_user_skin_border", columnList = "skin_border_id")
        })
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSkin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 유저 식별자
    @Column(name = "user_id", nullable = false)
    private Long userId;

    // 배경 스킨 (필수)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skin_background_id", nullable = false)
    private BackgroundImage skinBackground;

    // 보더 스킨 (선택)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skin_border_id")
    private SkinBorder skinBorder;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}