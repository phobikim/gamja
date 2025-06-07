package com.phobi.gamja.util;

import com.phobi.gamja.entity.dex.Dex;
import com.phobi.gamja.entity.user.UserDexStat;
import com.phobi.gamja.entity.user.UserDtl;
import com.phobi.gamja.repository.contents.DexRepository;
import com.phobi.gamja.repository.user.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommonUtil {

    private final DexRepository dexRepository;
    private final UserDtlRepository userDtlRepository;
    private final UserDexStatRepository userDexStatRepository;

    public String resolveCharacterImage(UserDtl userDtl) {
        if (userDtl.getCharacterDexId() != null) {
            Dex dex = dexRepository.findById(userDtl.getCharacterDexId())
                    .orElse(null);
            if (dex != null) {
                return dex.getImage();
            }
        }
        return userDtl.getCharacterImage(); // 기본 이미지
    }

    public void levelUp(UserDexStat stat) {
        stat.setLevel(stat.getLevel() + 1);
        stat.setPower(stat.getPower() + 1);
        stat.setHp(stat.getHp() + 1);
        stat.setSpeed(stat.getSpeed() + 1);

        userDexStatRepository.save(stat);
    }

}
