package com.phobi.gamja.repository.item;

import com.phobi.gamja.entity.item.ItemRecipe;
import com.phobi.gamja.entity.item.ItemRecipe.StationCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ItemRecipeRepository extends JpaRepository<ItemRecipe, Long> {
    List<ItemRecipe> findByStationCategory(StationCategory category);
    Optional<ItemRecipe> findByResultItemId(Long resultItemId);
}
