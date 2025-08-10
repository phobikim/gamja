package com.phobi.gamja.repository.battle;

import com.phobi.gamja.entity.battle.Monster;
import com.phobi.gamja.entity.battle.MonsterBossPattern;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MonsterBossPatternRepository extends JpaRepository<MonsterBossPattern, Long> {

    List<MonsterBossPattern> findByMonsterAndPhaseOrderByPhaseOrderAsc(Monster monster, int phase);

    List<MonsterBossPattern> findByMonsterAndPhaseGreaterThanOrderByPhaseAscPhaseOrderAsc(Monster monster, int minPhase);

    List<MonsterBossPattern> findByMonsterAndPhase(int monsterId, int phase);

    List<MonsterBossPattern> findByMonsterAndIsRepeatableTrueAndPhase(int monsterId, int phase);
}