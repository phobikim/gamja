package com.phobi.gamja.entity.user;

import javax.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_chronicle_complete")
@Getter
@Setter
@NoArgsConstructor
@IdClass(UserChronicleCompleteId.class)
public class UserChronicleComplete {

    @Id
    private Long userId;

    @Id
    private Long mapId;

    @Column(nullable = false)
    private boolean completed = true;

    @Column(name = "completed_at", nullable = false, insertable = false, updatable = false,
            columnDefinition = "timestamp default current_timestamp")
    private LocalDateTime completedAt;

    public UserChronicleComplete(Long userId, Long mapId) {
        this.userId = userId;
        this.mapId = mapId;
        this.completed = true;
    }
}