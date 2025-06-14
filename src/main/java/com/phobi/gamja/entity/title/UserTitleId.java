package com.phobi.gamja.entity.title;

import lombok.*;

import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;


@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class UserTitleId implements Serializable {

    private Long userId;
    private Long titleId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserTitleId)) return false;
        UserTitleId that = (UserTitleId) o;
        return Objects.equals(userId, that.userId) && Objects.equals(titleId, that.titleId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, titleId);
    }
}