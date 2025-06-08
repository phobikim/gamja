package com.phobi.gamja.repository.item;

import com.phobi.gamja.entity.item.ItemPotionEffect;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ItemPotionEffectRepository extends JpaRepository<ItemPotionEffect, Long> {
    List<ItemPotionEffect> findByItemIdIn(Collection<Long> itemIds);
}