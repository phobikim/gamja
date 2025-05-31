package com.phobi.gamja.repository.user;

import com.phobi.gamja.dto.user.EquipmentType;
import com.phobi.gamja.dto.user.UserEquipment;
import com.phobi.gamja.dto.user.UserEquipmentId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserEquipmentRepository extends JpaRepository<UserEquipment, UserEquipmentId> {
    List<UserEquipment> findByUserIdAndType(Long userId, EquipmentType type);
}