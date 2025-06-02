package com.phobi.gamja.entity.user;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

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

    @Column(length = 50)
    private String usernickname;

    @Column(length = 255)
    private String characterImage;

    @Column(name = "character_dex_id")
    private Long characterDexId; // 대표 감자 설정용


    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

}
