package com.phobi.gamja.entity.server;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "server_notice_patch")
@Getter
@Setter
public class ServerNoticePatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notice_id")
    private ServerNotice notice;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", columnDefinition = "ENUM('NEW','CHANGE','FIX')", nullable = false)
    private PatchType type;
    public enum PatchType {
        NEW,       // 신규 기능
        CHANGE,    // 기능/밸런스 수정
        FIX        // 버그 수정
    }
}