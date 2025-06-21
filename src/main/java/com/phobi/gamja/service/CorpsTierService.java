package com.phobi.gamja.service;

import com.phobi.gamja.entity.battle.StatBonus;
import com.phobi.gamja.entity.contents.CorpsTier;
import com.phobi.gamja.entity.user.UserCorps;
import com.phobi.gamja.repository.contents.CorpsTierRepository;
import com.phobi.gamja.repository.user.UserCorpsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CorpsTierService {
    private final UserCorpsRepository userCorpsRepository;
    private final CorpsTierRepository corpsTierRepository;
    public void updateCorpsXp(Long userId, int gainedXp) {
        UserCorps corps = userCorpsRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("감자단 정보가 없습니다."));

        int currentXp = corps.getCorpsXp() + gainedXp;
        int currentLevel = corps.getCorpsLevel();
        int maxXp = corps.getCorpsMaxXp();
        long tierId = corps.getTier().getTierId();

        while (currentXp >= maxXp) {
            currentXp -= maxXp;
            currentLevel++;

            // 레벨 10 초과 시 티어 상승
            if (currentLevel >= 10) {
                tierId++;
                currentLevel = 1;
                CorpsTier nextTier = corpsTierRepository.findById(tierId)
                        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 티어입니다."));
                corps.setTier(nextTier);
                maxXp = getInitialMaxXp(tierId);
            } else {
                maxXp = (int) Math.ceil(maxXp * (1 + getLevelIncreaseRate(tierId)));
            }
        }

        corps.setCorpsXp(currentXp);
        corps.setCorpsLevel(currentLevel);
        corps.setCorpsMaxXp(maxXp);
        userCorpsRepository.save(corps);
    }

    private double getLevelIncreaseRate(long tierId) {
        return tierId * 0.1; // 티어 1 → 10%, 티어 2 → 20%, ...
    }

    private int getInitialMaxXp(long tierId) {
        int baseXp = 100;
        double multiplier = 1.0;

        for (int i = 1; i < tierId; i++) {
            multiplier *= 1 + (i * 0.1); // 누적 증가율
        }

        return (int) Math.ceil(baseXp * multiplier);
    }

    public StatBonus calculateTierStatBonus(UserCorps userCorps) {
        if (userCorps == null || userCorps.getTier() == null) {
            return new StatBonus(0, 0, 0);
        }

        int tierId = Math.toIntExact(userCorps.getTier().getTierId()); // 1 ~ 12
        int corpsLevel = userCorps.getCorpsLevel();   // 1 ~ 10

        int base = (tierId - 1) * 3;
        int levelBonus = switch (corpsLevel) {
            case 1, 2, 3 -> 1;
            case 4, 5, 6, 7 -> 2;
            case 8, 9, 10 -> 3;
            default -> 1;
        };

        int total = base + levelBonus;
        return new StatBonus(total, total, total);
    }

}
