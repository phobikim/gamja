package com.phobi.gamja.repository.item;

import com.phobi.gamja.entity.item.ItemStatBonus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ItemStatBonusRepository extends JpaRepository<ItemStatBonus, Long> {
    List<ItemStatBonus> findByItemIdIn(Collection<Long> itemIds);
}
