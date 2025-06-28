package com.phobi.gamja.service;

import com.phobi.gamja.entity.server.ServerMaintenance;
import com.phobi.gamja.repository.server.ServerMaintenanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MaintenanceService {
    private final ServerMaintenanceRepository maintenanceRepository;

    public boolean isUnderMaintenance() {
        LocalDateTime now = LocalDateTime.now();
        return maintenanceRepository.existsByStartTimeBeforeAndEndTimeAfter(now, now);
    }
    public Optional<ServerMaintenance> getCurrentMaintenance() {
        LocalDateTime now = LocalDateTime.now();
        return maintenanceRepository.findFirstByStartTimeBeforeAndEndTimeAfter(now, now);
    }
}