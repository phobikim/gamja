package com.phobi.gamja.entity.user;

import javax.persistence.*;

import com.phobi.gamja.entity.contents.CorpsTier;
import lombok.*;

@Entity
@Table(name = "user_corps")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCorps {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tier_id")
    private CorpsTier tier;

    @Column(name = "corps_level", nullable = false)
    private Integer corpsLevel;

    @Column(name = "xp", nullable = false)
    private Integer corpsXp;
    @Column(name = "max_xp", nullable = false)
    private Integer corpsMaxXp;

    @Column(name = "updated_at")
    private java.sql.Timestamp updatedAt;
}