package com.phobi.gamja.entity.user;

import com.phobi.gamja.dto.item.EquipmentSlot;
import com.phobi.gamja.dto.item.EquipmentType;
import com.phobi.gamja.dto.user.UserEquipmentId;
import com.phobi.gamja.entity.item.Item;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_equipment")
@Getter
@Setter
public class UserEquipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EquipmentSlot slot;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EquipmentType type;

    @Column(name = "item_id", nullable = false)
    private Long itemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", insertable = false, updatable = false)
    private Item item;

    @Column(name = "equipped_at")
    private LocalDateTime equippedAt;
}