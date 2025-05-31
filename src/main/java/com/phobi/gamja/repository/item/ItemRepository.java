package com.phobi.gamja.repository.item;

import com.phobi.gamja.entity.item.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {
}
