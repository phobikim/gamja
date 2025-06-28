package com.phobi.gamja.repository.server;

import com.phobi.gamja.entity.server.ServerMaintenance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ServerMaintenanceRepository extends JpaRepository<ServerMaintenance, Long> {
    boolean existsByStartTimeBeforeAndEndTimeAfter(LocalDateTime start, LocalDateTime end);
    Optional<ServerMaintenance> findFirstByStartTimeBeforeAndEndTimeAfter(LocalDateTime start, LocalDateTime end);
}