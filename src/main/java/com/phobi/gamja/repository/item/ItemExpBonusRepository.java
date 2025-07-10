package com.phobi.gamja.repository.item;

import com.phobi.gamja.entity.item.ItemExpBonus;
import com.phobi.gamja.entity.user.UserInventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemExpBonusRepository extends JpaRepository<ItemExpBonus, Long> {

}