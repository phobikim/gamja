package com.phobi.gamja.service;
import com.phobi.gamja.dto.user.BattleStatDto;
import com.phobi.gamja.dto.user.LifeStatDto;
import com.phobi.gamja.dto.user.UserCharInfoDto;
import com.phobi.gamja.entity.contents.Dex;
import com.phobi.gamja.entity.item.Item;
import com.phobi.gamja.entity.user.UserDexStat;
import com.phobi.gamja.entity.user.UserDexStatId;
import com.phobi.gamja.entity.user.UserDtl;
import com.phobi.gamja.message.GamJaResponse;
import com.phobi.gamja.repository.contents.DexRepository;
import com.phobi.gamja.repository.item.ItemSkillBonusRepository;
import com.phobi.gamja.repository.item.ItemStatBonusRepository;
import com.phobi.gamja.repository.user.*;
import com.phobi.gamja.util.CommonUtil;
import com.phobi.gamja.util.StatCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CharService {

    private final CommonUtil commonUtil;
    private final StatCalculator statCalculator;

    private final UserDtlRepository userDtlRepository;
    private final UserSkillRepository userSkillRepository;
    private final DexRepository dexRepository;
    private final UserDexRepository userDexRepository;
    private final UserDexStatRepository userDexStatRepository;
    private final UserEquipmentRepository userEquipmentRepository;
    private final ItemStatBonusRepository itemStatBonusRepository;
    private final ItemSkillBonusRepository itemSkillBonusRepository;

    @Transactional(readOnly = true)
    public GamJaResponse getUserInfo(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");

        UserDtl userDtl = userDtlRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));
        Long dexId = userDtl.getCharacterDexId();
        if (dexId == null) {
            return GamJaResponse.fail("착용 중인 캐릭터가 없습니다.");
        }

        UserDexStatId statId = new UserDexStatId(userId, dexId);
        UserDexStat stat = userDexStatRepository.findById(statId)
                .orElseThrow(() -> new IllegalArgumentException("캐릭터 스탯 정보가 없습니다."));

        String finalImage = commonUtil.resolveCharacterImage(userDtl);
        userDtl.setCharacterImage(finalImage);

        UserCharInfoDto result = new UserCharInfoDto(userDtl, stat);
        result.setTitle(result.getTitleByLevel(stat.getLevel()));

        return GamJaResponse.success("정상 조회", result);
    }
}
