package com.phobi.gamja.repository.item;

import com.phobi.gamja.entity.item.ItemShop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ItemShopRepository extends JpaRepository<ItemShop, Long> {
    Optional<ItemShop> findByIdAndOnSaleTrue(Long id);
    List<ItemShop> findByOnSaleTrueOrderByDisplayOrderAsc();
    List<ItemShop> findByCategoryAndOnSaleTrueOrderByDisplayOrderAsc(ItemShop.ItemCategory category);

    Optional<Object> findByCategoryAndTargetIdAndOnSaleTrue(ItemShop.ItemCategory category, Long targetId);
}