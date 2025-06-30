package com.phobi.gamja.repository.server;

import com.phobi.gamja.entity.server.ServerNotice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ServerNoticeRepository extends JpaRepository<ServerNotice, Long> {

    // 현재 시간 기준 활성 공지 (기본적으로 보여줄 리스트용)
    List<ServerNotice> findByUseFlagTrueAndStartTimeBeforeAndEndTimeAfterOrderByPriorityDescStartTimeDesc(
            LocalDateTime now1, LocalDateTime now2
    );
    List<ServerNotice> findByUseFlagTrueOrderByPriorityDescStartTimeDesc();
    // 공지 유형 필터용
    List<ServerNotice> findByTypeAndUseFlagTrueOrderByPriorityDescStartTimeDesc(ServerNotice.NoticeType type);
}