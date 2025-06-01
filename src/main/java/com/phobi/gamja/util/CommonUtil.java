package com.phobi.gamja.util;

import com.phobi.gamja.entity.contents.Dex;
import com.phobi.gamja.entity.user.UserDtl;
import com.phobi.gamja.entity.user.UserStat;
import com.phobi.gamja.repository.contents.DexRepository;
import com.phobi.gamja.repository.user.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommonUtil {

    private final DexRepository dexRepository;
    private final UserDtlRepository userDtlRepository;
    private final UserStatRepository userStatRepository;

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

    public void levelUp(UserDtl userDtl) {
        userDtl.setLevel(userDtl.getLevel() + 1);

        UserStat stat = userStatRepository.findById(userDtl.getId())
                .orElseThrow(() -> new RuntimeException("스탯 없음"));

        stat.setUserPower(stat.getUserPower() + 1);
        stat.setUserHp(stat.getUserHp() + 1);
        stat.setUserSpeed(stat.getUserSpeed() + 1);

        userStatRepository.save(stat);
    }

}
