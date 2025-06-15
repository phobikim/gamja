package com.phobi.gamja.entity.quest;

import lombok.*;

import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserQuestId implements Serializable {
    private Long userId;
    private Long questId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserQuestId)) return false;
        UserQuestId that = (UserQuestId) o;
        return Objects.equals(userId, that.userId) && Objects.equals(questId, that.questId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, questId);
    }
}
