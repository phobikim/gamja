package com.phobi.gamja.entity.user;

import javax.persistence.*;

import com.phobi.gamja.entity.quest.Quest;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_daily_quest_log")
@IdClass(UserDailyQuestLogId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDailyQuestLog {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Id
    @Column(name = "quest_id")
    private Long questId;

    @Id
    @Column(name = "log_date")
    private LocalDate logDate;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quest_id", insertable = false, updatable = false)
    private Quest quest;
}