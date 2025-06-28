package com.phobi.gamja.entity.server;

import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "server_maintenance")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ServerMaintenance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(length = 1000)
    private String message;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
