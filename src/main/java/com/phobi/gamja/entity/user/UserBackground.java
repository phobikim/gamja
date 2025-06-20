package com.phobi.gamja.entity.user;

import com.phobi.gamja.entity.contents.BackgroundImage;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_background")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserBackground {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "background_id", nullable = false)
    private BackgroundImage backgroundImage;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}