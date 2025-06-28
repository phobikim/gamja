package com.phobi.gamja.service;

import com.phobi.gamja.entity.server.ServerMaintenance;
import com.phobi.gamja.repository.server.ServerMaintenanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MaintenanceService {
    private final ServerMaintenanceRepository maintenanceRepository;

    public boolean isUnderMaintenance() {
        LocalDateTime now = LocalDateTime.now();
        boolean result = maintenanceRepository.existsByStartTimeBeforeAndEndTimeAfter(now, now);
        return result;
    }
    public Optional<ServerMaintenance> getCurrentMaintenance() {
        LocalDateTime now = LocalDateTime.now();
        return maintenanceRepository.findFirstByStartTimeBeforeAndEndTimeAfter(now, now);
    }
}