package com.phobi.gamja.dto.user;

import com.phobi.gamja.dto.item.EquipmentSlot;
import com.phobi.gamja.dto.item.EquipmentType;
import com.phobi.gamja.entity.item.Item;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_equipment")
@IdClass(UserEquipmentId.class)
@Getter
@Setter
public class UserEquipment {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Id
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EquipmentSlot slot;;  // WEAPON, HELMET, ARMOR, PANTS, SHOES, RING, NECK, POTION

    @Id
    @Enumerated(EnumType.STRING)
    private EquipmentType type;  // BATTLE or GATHER

    @Column(name = "item_id")
    private Long itemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", insertable = false, updatable = false)
    private Item item;

    @Column(name = "equipped_at")
    private LocalDateTime equippedAt;

    // getter, setter 생략
}
