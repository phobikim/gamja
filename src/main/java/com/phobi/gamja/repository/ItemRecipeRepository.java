package com.phobi.gamja.repository;

import com.phobi.gamja.entity.ItemRecipe;
import com.phobi.gamja.entity.ItemRecipe.StationCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemRecipeRepository extends JpaRepository<ItemRecipe, Long> {
    List<ItemRecipe> findByStationCategory(StationCategory category);
}
