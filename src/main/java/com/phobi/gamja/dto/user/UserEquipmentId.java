package com.phobi.gamja.dto.user;

import com.phobi.gamja.dto.item.EquipmentSlot;
import com.phobi.gamja.dto.item.EquipmentType;

import java.io.Serializable;

public class UserEquipmentId implements Serializable {
    private Long userId;
    private EquipmentSlot slot;
    private EquipmentType type;

    // equals, hashCode 필수 구현
}