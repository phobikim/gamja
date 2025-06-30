package com.phobi.gamja.repository.server;

import com.phobi.gamja.entity.server.ServerNoticePatch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServerNoticePatchRepository extends JpaRepository<ServerNoticePatch, Long> {
    List<ServerNoticePatch> findByNoticeIdOrderBySortOrderAsc(Long noticeId);
}