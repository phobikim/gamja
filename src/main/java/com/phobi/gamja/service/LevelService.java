package com.phobi.gamja.service;

import com.phobi.gamja.dto.user.UserDexXpDto;
import com.phobi.gamja.entity.user.UserDexStat;
import com.phobi.gamja.entity.user.UserDexStatId;
import com.phobi.gamja.repository.user.UserDexStatRepository;
import com.phobi.gamja.util.CommonUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LevelService {
    private final CommonUtil commonUtil;

    private final UserDexStatRepository userDexStatRepository;
    public UserDexXpDto updateCharacterExp(Long userId, Long dexId, int gainedExp) {
        UserDexStatId statId = new UserDexStatId(userId, dexId);
        UserDexStat stat = userDexStatRepository.findById(statId)
                .orElseThrow(() -> new IllegalArgumentException("캐릭터 스탯 정보가 없습니다."));

        int xp = stat.getXp() + gainedExp;
        int level = stat.getLevel();
        int maxExp = stat.getMaxExp();

        while (xp >= maxExp) {
            xp -= maxExp;
            level++;
            commonUtil.levelUp(stat);
            maxExp = getRequiredExp(level);
        }

        stat.setXp(xp);
        stat.setLevel(level);
        stat.setMaxExp(maxExp);
        userDexStatRepository.save(stat);
        return new UserDexXpDto(stat.getLevel(), stat.getXp(), stat.getMaxExp());
    }

    public int getRequiredExp(int level) {
        return 100 + (level - 1) * 20;
    }
}
