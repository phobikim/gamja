package com.phobi.gamja.repository.dex;

import com.phobi.gamja.entity.dex.Dex;
import com.phobi.gamja.entity.dex.DexRarityStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DexRepository extends JpaRepository<Dex, Long> {

    /* 미공개 감자 포함 - 도감용 */
    List<Dex> findByUseFlagTrue();

    /* 미공개 감자 미포함 - 가챠용 */
    @Query("SELECT d FROM Dex d WHERE d.rarity = :rarity AND d.useFlag = true AND d.hidden = false")
    List<Dex> getGachaCandidates(@Param("rarity") DexRarityStat.Rarity rarity);
}
