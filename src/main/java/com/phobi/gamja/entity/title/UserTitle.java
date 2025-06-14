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
@IdClass(UserTitleId.class)
public class UserTitle {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "title_id", referencedColumnName = "id")
    private Title title;

    @Column(name = "is_equipped", nullable = false)
    private boolean isEquipped = false;

    @Column(name = "acquired_at")
    private LocalDateTime acquiredAt = LocalDateTime.now();
}
