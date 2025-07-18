package com.phobi.gamja.dto.item;

import com.phobi.gamja.entity.user.UserEnhancement;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EnhanceResultDto {
    private int enhancementLevel;
    private int enhancementXp;
    private boolean success;
    private int bonusPower;
    private int bonusHp;
    private int bonusSpeed;

    public static EnhanceResultDto from(UserEnhancement e, boolean success) {
        return EnhanceResultDto.builder()
                .enhancementLevel(e.getEnhancementLevel())
                .enhancementXp(e.getEnhancementXp())
                .bonusPower(e.getBonusPower())
                .bonusHp(e.getBonusHp())
                .bonusSpeed(e.getBonusSpeed())
                .success(success)
                .build();
    }
}