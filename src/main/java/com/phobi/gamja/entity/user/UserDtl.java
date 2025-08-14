package com.phobi.gamja.entity.user;

import com.phobi.gamja.entity.skin.SkinBorder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import com.phobi.gamja.entity.skin.BackgroundImage;
@Data
@Entity
@NoArgsConstructor
@Table(name = "user_dtl")
public class UserDtl {
    @Id
    private Long id; // user.id 를 외래키로 사용

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id")
    private User user;

    @Column(length = 255)
    private String characterImage;

    /* 배경화면 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "background_id")
    private BackgroundImage backgroundImage;

    /* 현재 적용 테두리 스킨 */
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "border_skin_id")
    private SkinBorder borderSkin;

    /* 대표 감자 */
    @Column(name = "character_dex_id")
    private Long characterDexId; // 대표 감자 설정용

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private Long gold = 0L;
}
