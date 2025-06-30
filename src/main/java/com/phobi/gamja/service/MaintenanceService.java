package com.phobi.gamja.service;

import com.phobi.gamja.entity.server.ServerMaintenance;
import com.phobi.gamja.entity.server.ServerNotice;
import com.phobi.gamja.entity.server.ServerNoticePatch;
import com.phobi.gamja.message.GamJaResponse;
import com.phobi.gamja.repository.server.ServerMaintenanceRepository;
import com.phobi.gamja.repository.server.ServerNoticePatchRepository;
import com.phobi.gamja.repository.server.ServerNoticeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MaintenanceService {
    private final ServerMaintenanceRepository maintenanceRepository;
    private final ServerNoticeRepository serverNoticeRepository;
    private final ServerNoticePatchRepository serverNoticePatchRepository;

    public boolean isUnderMaintenance() {
        LocalDateTime now = LocalDateTime.now();
        boolean result = maintenanceRepository.existsByStartTimeBeforeAndEndTimeAfter(now, now);
        return result;
    }
    public Optional<ServerMaintenance> getCurrentMaintenance() {
        LocalDateTime now = LocalDateTime.now();
        return maintenanceRepository.findFirstByStartTimeBeforeAndEndTimeAfter(now, now);
    }

    public ResponseEntity<GamJaResponse> getNoticeList(HttpServletRequest request) {
        LocalDateTime now = LocalDateTime.now();

        List<ServerNotice> notices = serverNoticeRepository
                .findByUseFlagTrueOrderByPriorityDescStartTimeDesc();

        List<Map<String, Object>> result = notices.stream().map(notice -> {
            Map<String, Object> n = new HashMap<>();
            n.put("id", notice.getId());
            n.put("title", notice.getTitle());
            n.put("content", notice.getContent());
            n.put("type", notice.getType().name());
            n.put("startTime", notice.getStartTime());
            n.put("endTime", notice.getEndTime());

            // ✅ patch 리스트 조회
            List<ServerNoticePatch> patchList = serverNoticePatchRepository.findByNoticeIdOrderBySortOrderAsc(notice.getId());
            if (!patchList.isEmpty()) {
                List<String> patchContentList = patchList.stream()
                        .map(ServerNoticePatch::getContent)
                        .collect(Collectors.toList());
                n.put("patchNotes", patchContentList);
            }

            return n;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(GamJaResponse.success("공지 목록을 불러왔습니다.", result));
    }
}