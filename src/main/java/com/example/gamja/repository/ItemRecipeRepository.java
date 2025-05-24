package com.example.gamja.repository;

import com.example.gamja.entity.ItemRecipe;
import com.example.gamja.entity.ItemRecipe.StationCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemRecipeRepository extends JpaRepository<ItemRecipe, Long> {
    List<ItemRecipe> findByStationCategory(StationCategory category);
}
