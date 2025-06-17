package com.phobi.gamja.entity.quest;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_quest")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserQuest {

    @EmbeddedId
    private UserQuestId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("questId")
    @JoinColumn(name = "quest_id", insertable = false, updatable = false)
    private Quest quest;

    @Column(name = "is_completed", nullable = false)
    private boolean completed;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
