package com.phobi.gamja.entity.contents;

import javax.persistence.*;
import lombok.*;

@Entity
@Table(name = "corps_tier")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CorpsTier {

    @Id
    @Column(name = "tier_id")
    private Long tierId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "icon_path")
    private String iconPath;

    @Column(name = "description")
    private String description;

    @Column(name = "created_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private java.sql.Timestamp createdAt;
}