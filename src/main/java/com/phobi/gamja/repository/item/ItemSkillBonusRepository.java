package com.phobi.gamja.repository.item;

import com.phobi.gamja.entity.item.ItemSkillBonus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ItemSkillBonusRepository extends JpaRepository<ItemSkillBonus, Long> {
    List<ItemSkillBonus> findByItemIdIn(Collection<Long> itemIds);
}