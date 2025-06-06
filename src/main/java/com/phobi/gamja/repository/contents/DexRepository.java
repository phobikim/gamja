package com.phobi.gamja.repository.contents;

import com.phobi.gamja.entity.contents.Dex;
import com.phobi.gamja.entity.item.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DexRepository extends JpaRepository<Dex, Long> {
    @Query("SELECT d FROM Dex d WHERE d.userFlag = true")
    List<Dex> findAllEnabledForUser();

    List<Dex> findByRarity(Dex.DexRarity rarity);
}
