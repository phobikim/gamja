package com.phobi.gamja.repository.item;

import com.phobi.gamja.entity.item.ItemRecipe;
import com.phobi.gamja.entity.item.ItemRecipe.StationCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemRecipeRepository extends JpaRepository<ItemRecipe, Long> {
    List<ItemRecipe> findByStationCategory(StationCategory category);
}
