package com.phobi.gamja.entity.title;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_title")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserTitle {

    @EmbeddedId
    private UserTitleId id;

    @MapsId("titleId") // 복합키 내부 titleId 필드를 연동
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "title_id")
    private Title title;

    @Column(name = "is_owned", nullable = false)
    private boolean isOwned = true;

    @Column(name = "is_equipped", nullable = false)
    private boolean isEquipped = false;

    @Column(name = "acquired_at", insertable = false, updatable = false)
    private LocalDateTime acquiredAt;
}