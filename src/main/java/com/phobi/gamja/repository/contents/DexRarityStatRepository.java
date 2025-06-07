package com.phobi.gamja.repository.contents;

import com.phobi.gamja.entity.dex.DexRarityStat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DexRarityStatRepository extends JpaRepository<DexRarityStat, DexRarityStat.Rarity> { }
