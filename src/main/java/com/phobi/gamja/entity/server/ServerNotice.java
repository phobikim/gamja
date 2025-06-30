package com.phobi.gamja.entity.server;

import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "server_notice")
@Getter
@Setter
public class ServerNotice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NoticeType type = NoticeType.SYSTEM;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private Integer priority = 0;

    private Boolean useFlag = true;

    private LocalDateTime createdAt = LocalDateTime.now();

    public enum NoticeType {
        SYSTEM, EVENT, UPDATE, MAINTENANCE
    }
}