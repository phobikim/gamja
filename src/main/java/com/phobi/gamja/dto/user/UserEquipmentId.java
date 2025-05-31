package com.phobi.gamja.dto.user;

import java.io.Serializable;

public class UserEquipmentId implements Serializable {
    private Long userId;
    private String slot;
    private EquipmentType type;

    // equals, hashCode 필수 구현
}