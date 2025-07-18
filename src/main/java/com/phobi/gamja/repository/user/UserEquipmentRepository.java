package com.phobi.gamja.repository.user;

import com.phobi.gamja.dto.item.EquipmentSlot;
import com.phobi.gamja.dto.item.EquipmentType;
import com.phobi.gamja.entity.user.UserEquipment;
import com.phobi.gamja.dto.user.UserEquipmentId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserEquipmentRepository extends JpaRepository<UserEquipment, UserEquipmentId> {
    List<UserEquipment> findByUserIdAndType(Long userId, EquipmentType type);
    void deleteByUserIdAndSlotAndType(Long userId, EquipmentSlot slot, EquipmentType type);
    Optional<UserEquipment> findByUserIdAndSlot(Long userId, EquipmentSlot slot);
    // 특정 아이템이 장착 중인지 여부 확인 (장착 상태 = user_equipment 테이블에 존재)
    boolean existsByUserIdAndItemId(Long userId, Long itemId);

}