package com.phobi.gamja.repository.item;

import com.phobi.gamja.entity.item.ItemShop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemShopRepository extends JpaRepository<ItemShop, Long> {
    List<ItemShop> findByOnSaleTrue();

    List<ItemShop> findByShopTypeAndOnSaleTrue(ItemShop.ShopType shopType);
}