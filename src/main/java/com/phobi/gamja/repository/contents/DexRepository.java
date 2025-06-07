package com.phobi.gamja.repository.contents;

import com.phobi.gamja.entity.dex.Dex;
import com.phobi.gamja.entity.dex.DexRarityStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DexRepository extends JpaRepository<Dex, Long> {
    @Query("SELECT d FROM Dex d WHERE d.userFlag = true")
    List<Dex> findAllEnabledForUser();

    List<Dex> findByRarity(DexRarityStat.Rarity rarity);
}
