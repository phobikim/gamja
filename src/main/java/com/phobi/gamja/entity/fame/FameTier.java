package com.phobi.gamja.entity.fame;

import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "fame_tier")
public class FameTier {

    @Id
    @Column(name = "fame_id", nullable = false)
    private Integer fameId; // smallint -> Integer 매핑

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Lob
    @Column(name = "description")
    private String description;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt; // DB default CURRENT_TIMESTAMP
}