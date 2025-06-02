package com.phobi.gamja.entity.user;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class UserDexStatId implements Serializable {
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "dex_id")
    private Long dexId;
}
