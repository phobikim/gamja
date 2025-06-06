package com.phobi.gamja.service;
import com.phobi.gamja.dto.contents.DexOwnedDto;
import com.phobi.gamja.dto.contents.DexOwnedListResponseDto;
import com.phobi.gamja.dto.user.BattleStatDto;
import com.phobi.gamja.dto.user.LifeStatDto;
import com.phobi.gamja.dto.user.UserCharInfoDto;
import com.phobi.gamja.entity.contents.Dex;
import com.phobi.gamja.entity.item.Item;
import com.phobi.gamja.entity.user.*;
import com.phobi.gamja.message.GamJaResponse;
import com.phobi.gamja.repository.contents.DexRepository;
import com.phobi.gamja.repository.item.ItemSkillBonusRepository;
import com.phobi.gamja.repository.item.ItemStatBonusRepository;
import com.phobi.gamja.repository.user.*;
import com.phobi.gamja.util.CommonUtil;
import com.phobi.gamja.util.StatCalculator;
import com.phobi.gamja.web.config.annotation.SanitizeInput;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CharService {

    private final CommonUtil commonUtil;
    private final StatCalculator statCalculator;

    private final UserDtlRepository userDtlRepository;
    private final UserSkillRepository userSkillRepository;
    private final UserInventoryRepository userInventoryRepository;
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

    @Transactional(readOnly = true)
    public GamJaResponse getBattleInfo(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        BattleStatDto result = statCalculator.calculateBattleStat(userId);
        return GamJaResponse.success("정상 조회", result);
    }

    @Transactional(readOnly = true)
    public GamJaResponse getLifeInfo(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        LifeStatDto result = statCalculator.calculateLifeSkill(userId);
        return GamJaResponse.success("정상 조회", result);
    }

    @Transactional
    @SanitizeInput
    public GamJaResponse setCharacterImage(HttpServletRequest request, Map<String, Long> payload) {
        Long userId = (Long) request.getAttribute("userId");
        Long dexId = payload.get("dexId");

        boolean owned = userDexRepository.existsByUserIdAndDexId(userId, dexId);
        if (!owned) {
            return GamJaResponse.fail("미획득한 감자는 설정할 수 없습니다.");
        }

        UserDtl userDtl = userDtlRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));
        userDtl.setCharacterDexId(dexId);
        userDtl.setCharacterImage(commonUtil.resolveCharacterImage(userDtl));
        userDtlRepository.save(userDtl);

        UserDexStatId statId = new UserDexStatId(userId, dexId);
        UserDexStat stat = userDexStatRepository.findById(statId).orElseGet(() -> {
            UserDexStat newStat = UserDexStat.builder()
                    .id(statId)
                    .user(userDtl.getUser())
                    .dex(Dex.builder().id(dexId).build())
                    .level(1)
                    .xp(0)
                    .maxExp(100)
                    .power(0)
                    .hp(0)
                    .speed(0)
                    .build();
            return userDexStatRepository.save(newStat);
        });

        UserCharInfoDto dto = new UserCharInfoDto(userDtl, stat);
        return GamJaResponse.success("대표 감자가 설정되었습니다.", dto);
    }

    @Transactional
    @SanitizeInput
    public GamJaResponse getGacha(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        // 1. 인벤토리에서 미감정 감자 확인
        final Long UNAPPRAISED_POTATO_ID = 53L;
        final Long POTATO_FRAGMENT_ID = 68L;

        // 1. 미감정 감자 보유 확인 및 수량 차감
        UserInventory unappraised = userInventoryRepository.findByUserIdAndItemId(userId, UNAPPRAISED_POTATO_ID)
                .orElse(null);
        if (unappraised == null || unappraised.getQuantity() <= 0) {
            return GamJaResponse.fail("미감정 감자가 부족합니다.");
        }
        // 수량 차감
        unappraised.setQuantity(unappraised.getQuantity() - 1);
        userInventoryRepository.save(unappraised);

        // 2. 확률 추첨
        Dex.DexRarity selectedRarity = Dex.rollRarity();
        List<Dex> candidates = dexRepository.findByRarity(selectedRarity);
        if (candidates.isEmpty()) {
            return GamJaResponse.fail("해당 등급의 감자가 없습니다.");
        }
        // 3. 감자 하나 랜덤 선택
        Dex selected = candidates.get(new Random().nextInt(candidates.size()));

        // 4. 중복 여부 확인
        boolean isDuplicate = userDexRepository.existsByUserIdAndDexId(userId, selected.getId());
        int gainedFragments = 0;

        if (isDuplicate) {
            // 5. 조각 지급 (중복 시)
            UserInventory fragment = userInventoryRepository.findByUserIdAndItemId(userId, POTATO_FRAGMENT_ID)
                    .orElse(new UserInventory(userId, POTATO_FRAGMENT_ID, 0));

            fragment.setQuantity(fragment.getQuantity() + 1);
            userInventoryRepository.save(fragment);
            gainedFragments = 1;
        } else {
            // 6. 보유 감자에 등록
            UserDex newDex = UserDex.of(userId, selected);
            userDexRepository.save(newDex);
        }
        // 7. 응답 데이터 구성
        Map<String, Object> result = new HashMap<>();
        result.put("resultType", isDuplicate ? "DUPLICATE" : "NEW");
        result.put("dexId", selected.getId());
        result.put("name", selected.getName());
        result.put("rarity", selected.getRarity());
        result.put("image", selected.getImage());
        result.put("attribute", selected.getAttribute());
        result.put("desc", selected.getDescription());
        result.put("pieceGained", gainedFragments);

        return GamJaResponse.success("감자 뽑기 성공", result);
    }

    @Transactional
    @SanitizeInput
    public GamJaResponse ticketCount(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        // 1. 인벤토리에서 미감정 감자 확인
        final Long UNAPPRAISED_POTATO_ID = 53L;
        final Long POTATO_FRAGMENT_ID = 68L;

        // 1. 미감정 감자 및 미감정 감자 조각 보유 개수 확인
        UserInventory unappraisedCount = userInventoryRepository.findByUserIdAndItemId(userId, UNAPPRAISED_POTATO_ID)
                .orElse(null);
        UserInventory potatoFragmentCount = userInventoryRepository.findByUserIdAndItemId(userId, POTATO_FRAGMENT_ID)
                .orElse(null);


        // 7. 응답 데이터 구성
        Map<String, Object> result = new HashMap<>();
        result.put("unappraisedCount", unappraisedCount != null ? unappraisedCount.getQuantity() : 0);
        result.put("potatoFragmentCount", potatoFragmentCount != null ? potatoFragmentCount.getQuantity() : 0);

        return GamJaResponse.success("미감정 감자 조회 성공", result);
    }

    @Transactional(readOnly = true)
    public GamJaResponse getOwnedDex(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");

        // 1. 보유 감자 ID 리스트
        List<UserDex> userDexList = userDexRepository.findByUserId(userId);

        // 2. 대표 감자 ID
        Long selectedDexId = userDtlRepository.findById(userId)
                .map(UserDtl::getCharacterDexId)
                .orElse(null);

        // 3. 각각의 캐릭터에 대해 DTO 생성
        List<DexOwnedDto> ownedDexList = userDexList.stream().map(userDex -> {
            Dex dex = userDex.getDex();
            UserDexStat stat = userDexStatRepository
                    .findByUserIdAndDexId(userId, dex.getId())
                    .orElse(null);

            return DexOwnedDto.builder()
                    .dexId(dex.getId())
                    .dexImage(dex.getImage())
                    .dexName(dex.getName())
                    .attribute(dex.getAttribute())
                    .rarity(dex.getRarity())
                    .level(stat != null ? stat.getLevel() : 1)
                    .xp(stat != null ? stat.getXp() : 0)
                    .maxExp(stat != null ? stat.getMaxExp() : 100)
                    .selected(dex.getId().equals(selectedDexId))
                    .build();
        }).sorted((a, b) -> Boolean.compare(!a.isSelected(), !b.isSelected()))
                .collect(Collectors.toList());

        // 전체 감자 도감 수
        int totalDexCount = (int) dexRepository.count();
        // 보유 감자 수
        int ownedDexCount = userDexList.size();

        DexOwnedListResponseDto result = DexOwnedListResponseDto.builder()
                .representDex(selectedDexId)
                .totalDexCount(totalDexCount)
                .ownedDexCount(ownedDexCount)
                .ownedDexList(ownedDexList)
                .build();


        return GamJaResponse.success("보유 감자 리스트 조회 완료", result);
    }
}
