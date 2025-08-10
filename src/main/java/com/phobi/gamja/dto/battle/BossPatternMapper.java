package com.phobi.gamja.dto.battle;

import com.phobi.gamja.entity.battle.MonsterBossPattern;
import org.springframework.stereotype.Component;

@Component
public class BossPatternMapper {
    public PatternDTO toDto(MonsterBossPattern p) {
        PatternDTO dto = new PatternDTO();
        dto.setId(p.getId());
        dto.setMonsterId(p.getMonster().getId());
        dto.setPhase(p.getPhase());
        dto.setPhaseOrder(p.getPhaseOrder());
        dto.setDialogue(p.getDialogue());
        dto.setType(p.getPatternType().name());
        dto.setValue(p.getPatternValue());
        dto.setEnabled(p.isEnabled());
        dto.setCooldown(p.getCooldown());
        return dto;
    }
}