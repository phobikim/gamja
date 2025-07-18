package com.phobi.gamja.entity.user;

import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserEnhancementId implements Serializable {

    private Long userId;
    private Long itemId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserEnhancementId)) return false;
        UserEnhancementId that = (UserEnhancementId) o;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(itemId, that.itemId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, itemId);
    }
}