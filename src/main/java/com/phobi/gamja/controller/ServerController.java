package com.phobi.gamja.controller;

import com.phobi.gamja.entity.server.ServerMaintenance;
import com.phobi.gamja.message.GamJaResponse;
import com.phobi.gamja.service.MaintenanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/maintenance")
public class ServerController {
    private final MaintenanceService maintenanceService;

    @GetMapping("/server/current")
    public ResponseEntity<?> getCurrentMaintenance() {
        Optional<ServerMaintenance> maintenance = maintenanceService.getCurrentMaintenance();
        if (maintenance.isPresent()) {
            ServerMaintenance m = maintenance.get();
            Map<String, String> result = Map.of(
                    "startTime", m.getStartTime().toString(),
                    "endTime", m.getEndTime().toString()
            );
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
    }
}
