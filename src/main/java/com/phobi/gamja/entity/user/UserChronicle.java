package com.phobi.gamja.entity.user;

import com.phobi.gamja.entity.chronicle.Chronicle;
import com.phobi.gamja.entity.quest.Quest;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_chronicle")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(UserChronicleId.class)
public class UserChronicle {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chronicle_id")
    private Chronicle chronicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quest_id") // nullable 허용
    private Quest quest;

    @Column(name = "progress_count", nullable = false)
    private int progressCount;

    @Column(name = "completed", nullable = false)
    private boolean completed;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}