package com.phobi.gamja.service;

import com.phobi.gamja.dto.fame.UserFameDto;
import com.phobi.gamja.entity.fame.FameTier;
import com.phobi.gamja.entity.user.UserFame;
import com.phobi.gamja.message.GamJaResponse;
import com.phobi.gamja.repository.fame.FameTierRepository;
import com.phobi.gamja.repository.user.UserFameRepository;
import com.phobi.gamja.util.CommonUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpSession;

@Service
@RequiredArgsConstructor
public class FameService {
    private final CommonUtil commonUtil;
    private final UserFameRepository userFameRepository;
    private final FameTierRepository fameTierRepository;
    @Transactional
    public GamJaResponse getOrCreateUserFame(HttpSession session) {
        Long userId = commonUtil.getUserId(session);

        // DB 조회
        UserFame uf = userFameRepository.findByUserId(userId).orElse(null);

        // 없으면 신규 생성
        if (uf == null) {
            FameTier tier = fameTierRepository.findById(1)
                    .orElseThrow(() -> new IllegalStateException("기본 FameTier(1) 없음"));

            uf = UserFame.builder()
                    .fameTier(tier)
                    .fameLevel(1)
                    .xp(0)
                    .maxXp(100)
                    .famePoint(0)
                    .build();

            uf = userFameRepository.save(uf);
        }
        UserFameDto dto = UserFameDto.builder()
                .fameId(uf.getFameTier().getFameId())
                .fameName(uf.getFameTier().getName())
                .fameDesc(uf.getFameTier().getDescription())
                .fameLevel(uf.getFameLevel())
                .xp(uf.getXp())
                .maxXp(uf.getMaxXp())
                .famePoint(uf.getFamePoint())
                .build();
        // 결과 반환
        return GamJaResponse.success("유저 명성 정보", dto);
    }
}
