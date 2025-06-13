package com.phobi.gamja.repository.user;

import com.phobi.gamja.dto.item.EquipmentSlot;
import com.phobi.gamja.dto.item.EquipmentType;
import com.phobi.gamja.dto.user.UserEquipment;
import com.phobi.gamja.dto.user.UserEquipmentId;
import com.phobi.gamja.entity.item.Item;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserEquipmentRepository extends JpaRepository<UserEquipment, UserEquipmentId> {
    List<UserEquipment> findByUserIdAndType(Long userId, EquipmentType type);
    void deleteByUserIdAndSlotAndType(Long userId, EquipmentSlot slot, EquipmentType type);
    Optional<UserEquipment> findByUserIdAndSlot(Long userId, EquipmentSlot slot);


}