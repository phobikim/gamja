package com.phobi.gamja.repository.dex;

import com.phobi.gamja.entity.battle.BattleSkill;
import com.phobi.gamja.entity.dex.DexSkill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DexSkillRepository extends JpaRepository<DexSkill, Long> {
    List<DexSkill> findByDexAttribute(String dexAttribute);
    List<DexSkill> findBySkillType(BattleSkill.Type skillType);
    List<DexSkill> findByDexAttributeAndSkillType(String dexAttribute, BattleSkill.Type skillType);
}