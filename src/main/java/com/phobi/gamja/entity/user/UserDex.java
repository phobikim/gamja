package com.phobi.gamja.entity.user;

import com.phobi.gamja.entity.contents.Dex;
import lombok.*;

import javax.persistence.*;
import java.util.Date;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class UserDex {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dex_id", nullable = false)
    private Dex dex;

    @Column(name = "acquired_at")
    private Date acquiredAt;


    public static UserDex of(Long userId, Dex dex) {
        User user = new User();
        user.setId(userId);
        return UserDex.builder()
                .user(user)
                .dex(dex)
                .acquiredAt(new Date())
                .build();
    }

}