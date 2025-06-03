package com.phobi.gamja.entity.contents;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "action_card_event")
@Getter
@Setter
public class ActionCardEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", nullable = false)
    private ActivityType activityType;

    @Column(nullable = false)
    private int rank;

    @Column(name = "card_text", nullable = false)
    private String cardText;

    @Column(name = "event_message", nullable = false)
    private String eventMessage;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private EventType eventType;

    @Column(name = "drop_group_id")
    private Long dropGroupId;

    @Column(name = "hp_change")
    private Integer hpChange;

    @Column(name = "exp_reward")
    private Float expReward;

    @Column
    private Integer weight;

    @Column(name = "is_enabled")
    private Boolean isEnabled;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Transient
    private List<ActionCardEventDrop> drops; // API 응답용
}
