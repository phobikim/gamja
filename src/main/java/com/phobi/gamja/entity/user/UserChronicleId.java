package com.phobi.gamja.entity.user;

import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserChronicleId implements Serializable {

    private Long userId;
    private Long chronicle;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserChronicleId that = (UserChronicleId) o;
        return Objects.equals(userId, that.userId) && Objects.equals(chronicle, that.chronicle);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, chronicle);
    }
}
