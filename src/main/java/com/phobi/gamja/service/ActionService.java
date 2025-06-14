// ActionService.java
package com.phobi.gamja.service;

import com.phobi.gamja.dto.contents.ActionDto;
import com.phobi.gamja.dto.contents.CardEventDto;
import com.phobi.gamja.dto.contents.DropTableEntryDto;
import com.phobi.gamja.dto.user.UserCharInfoDto;
import com.phobi.gamja.dto.user.UserSkillDto;
import com.phobi.gamja.entity.contents.*;
import com.phobi.gamja.entity.item.Item;
import com.phobi.gamja.entity.title.Title;
import com.phobi.gamja.entity.title.UserTitle;
import com.phobi.gamja.entity.title.UserTitleId;
import com.phobi.gamja.entity.user.*;
import com.phobi.gamja.message.GamJaResponse;
import com.phobi.gamja.repository.contents.*;
import com.phobi.gamja.repository.item.ItemRepository;
import com.phobi.gamja.repository.title.TitleEffectRepository;
import com.phobi.gamja.repository.title.TitleRepository;
import com.phobi.gamja.repository.title.UserTitleRepository;
import com.phobi.gamja.repository.user.*;
import com.phobi.gamja.util.CommonUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ActionService {

    private final ActionDropRepository actionDropRepository;
    private final ItemRepository itemRepository;
    private final UserInventoryRepository userInventoryRepository;
    private final UserSkillRepository userSkillRepository;
    private final ActionRepository actionRepository;
    private final UserDtlRepository userDtlRepository;
    private final UserDexStatRepository userDexStatRepository;
    private final ActionCardEventRepository actionCardEventRepository;
    private final ActionCardEventDropRepository actionCardEventDropRepository;
    // 타이틀 관련
    private final TitleRepository titleRepository;
    private final TitleEffectRepository titleEffectRepository;
    private final UserTitleRepository userTitleRepository;
    private final UserCounterDetailRepository userCounterDetailRepository;

    private final LogService logService;
    private final CommonUtil commonUtil;

    public ResponseEntity<GamJaResponse> getActionsByCategory(String activityType, HttpSession session) {
        Long userId = getUserId(session);
        try {
            SkillType actionCategory = SkillType.valueOf(activityType.toUpperCase());
            ActivityType type = ActivityType.valueOf(activityType.toUpperCase());
            Optional<UserSkill> userSkillOpt = userSkillRepository.findByUserIdAndSkillType(userId, actionCategory);
            int level = userSkillOpt.map(UserSkill::getLevel).orElse(1);
            int exp = userSkillOpt.map(UserSkill::getExp).orElse(0);
            int maxCombo = userSkillOpt.map(UserSkill::getMaxCombo).orElse(0);

            List<Action> actions = actionRepository.findByCategoryAndIsEnabledOrderByRankAsc(type, true);
            List<ActionDto> result = actions.stream()
                    .map(action -> ActionDto.of(action, level, exp, maxCombo))
                    .toList();
            return ResponseEntity.ok(GamJaResponse.success("정상 조회", result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(GamJaResponse.fail("잘못된 카테고리"));
        }
    }

    public ResponseEntity<GamJaResponse> getCardEvents(String activity, int rank) {
        ActivityType activityType = ActivityType.valueOf(activity.toUpperCase());
        List<ActionCardEvent> eventList = actionCardEventRepository
                .findByActivityTypeAndRankAndIsEnabledTrue(activityType, rank);

        List<CardEventDto> result = eventList.stream()
                .map(event -> {
                    List<ActionCardEventDrop> drops = Optional.ofNullable(event.getDropGroupId())
                            .map(actionCardEventDropRepository::findByDropGroupId)
                            .orElse(List.of());
                    return CardEventDto.of(event, drops);
                }).toList();

        return ResponseEntity.ok(GamJaResponse.success("카드 이벤트 조회 완료", result));
    }

    public ResponseEntity<GamJaResponse> getDropTable(String activityType, int spotRank, HttpSession session) {
        Long userId = getUserId(session);
        ActivityType type = ActivityType.valueOf(activityType.toUpperCase());

        List<ActionDrop> drops = actionDropRepository.findByActivityTypeAndSpotRank(type, spotRank);
        List<DropTableEntryDto> result = drops.stream().map(drop -> {
            Item item = itemRepository.findById(drop.getItemId())
                    .orElseThrow(() -> new IllegalArgumentException("아이템 없음: " + drop.getItemId()));
            return DropTableEntryDto.of(drop, item);
        }).toList();

        return ResponseEntity.ok(GamJaResponse.success("정상 조회", result));
    }

    public ResponseEntity<GamJaResponse> endBattle(HttpSession session, Map<String, Object> request) {
        Long userId = getUserId(session);
        Long monsterId = ((Number) request.get("monsterId")).longValue();

        UserDtl userDtl = getUserDtl(userId);
        Long dexId = Optional.ofNullable(userDtl.getCharacterDexId())
                .orElseThrow(() -> new IllegalArgumentException("착용 중인 캐릭터가 없습니다."));

        UserDexStat stat = updateCharacterExp(userId, dexId, (int) request.get("exp"));
        processItemRewards(userId, request);
        userDtl.setCharacterImage(commonUtil.resolveCharacterImage(userDtl));

        logService.recordCounter(userId, CounterType.MONSTER_KILL, monsterId);
        return ResponseEntity.ok(GamJaResponse.success("아이템 추가 완료", new UserCharInfoDto(userDtl, stat)));
    }

    public ResponseEntity<GamJaResponse> endExploration(HttpSession session, Map<String, Object> request) {
        Long userId = getUserId(session);
        SkillType skillType = SkillType.valueOf(((String) request.get("activityType")).toUpperCase());
        double exp = ((Number) request.getOrDefault("exp", 0)).doubleValue();
        int maxCombo = ((Number) request.getOrDefault("maxCombo", 0)).intValue();

        UserSkill userSkill = updateUserSkill(userId, skillType, exp, maxCombo);
        processItemRewards(userId, request);

        UserSkillDto result = new UserSkillDto(skillType, userSkill.getLevel(), userSkill.getExp(), getRequiredExp(userSkill.getLevel()), userSkill.getMaxCombo());
//        logService.recordCounter(userId, CounterType.LIFE_ACTION, actionId);
        return ResponseEntity.ok(GamJaResponse.success("아이템 추가 완료", result));
    }

    // ✅ 칭호 수동 획득
    public ResponseEntity<GamJaResponse> claimTitle(HttpSession session, Map<String, Object> request) {
        Long userId = getUserId(session);
        Long titleId = ((Number) request.get("titleId")).longValue();

        Title title = titleRepository.findById(titleId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 칭호입니다."));

        String key = title.getCounterType().name() + ":" + title.getTargetId();
        int current = userCounterDetailRepository.findByUserId(userId).stream()
                .filter(c -> key.equals(c.getCounterType().name() + ":" + c.getTargetId()))
                .mapToInt(UserCounterDetail::getCounterValue)
                .sum();

        if (current < title.getRequiredCount()) {
            return ResponseEntity.badRequest().body(GamJaResponse.fail("칭호 획득 조건을 만족하지 않습니다."));
        }

        boolean alreadyOwned = userTitleRepository.existsById(new UserTitleId(userId, titleId));
        if (alreadyOwned) {
            return ResponseEntity.ok(GamJaResponse.success("이미 보유한 칭호입니다.", null));
        }

        UserTitle ut = UserTitle.builder()
                .id(new UserTitleId(userId, titleId))
                .title(title)
                .isOwned(true)
                .isEquipped(false)
                .build();

        userTitleRepository.save(ut);

        return ResponseEntity.ok(GamJaResponse.success("칭호를 획득했습니다.", null));
    }

    // ✅ 칭호 착용
    public ResponseEntity<GamJaResponse> equipTitle(HttpSession session, Map<String, Object> request) {
        Long userId = getUserId(session);
        Long titleId = ((Number) request.get("titleId")).longValue();

        List<UserTitle> userTitles = userTitleRepository.findByIdUserId(userId);

        boolean hasTitle = userTitles.stream().anyMatch(ut -> ut.getTitle().getId().equals(titleId));
        if (!hasTitle) {
            return ResponseEntity.badRequest().body(GamJaResponse.fail("해당 칭호를 보유하고 있지 않습니다."));
        }

        for (UserTitle ut : userTitles) {
            ut.setEquipped(ut.getTitle().getId().equals(titleId));
        }

        userTitleRepository.saveAll(userTitles);

        // ✅ 최신 UserCharInfoDto 내려주기
        UserDtl userDtl = userDtlRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));
        Long dexId = userDtl.getCharacterDexId();

        UserCharInfoDto dto = null;
        if (dexId != null) {
            UserDexStat stat = userDexStatRepository.findById(new UserDexStatId(userId, dexId))
                    .orElseThrow(() -> new IllegalArgumentException("캐릭터 스탯이 없습니다."));
            String equippedTitleName = userTitleRepository.findByIdUserIdAndIsEquippedTrue(userId)
                    .map(t -> t.getTitle().getName())
                    .orElse(null);
            dto = new UserCharInfoDto(userDtl, stat, 0, equippedTitleName);
        }

        return ResponseEntity.ok(GamJaResponse.success("칭호를 착용했습니다.", dto));
    }

    // ===== 공통 처리 함수 =====
    private Long getUserId(HttpSession session) {
        return Optional.ofNullable((Long) session.getAttribute("userId"))
                .orElseThrow(() -> new IllegalArgumentException("로그인이 필요합니다."));
    }

    private UserDtl getUserDtl(Long userId) {
        return userDtlRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));
    }

    private void processItemRewards(Long userId, Map<String, Object> request) {
        List<Map<String, Object>> items = (List<Map<String, Object>>) request.get("items");
        for (Map<String, Object> itemMap : items) {
            Long itemId = ((Number) itemMap.get("itemId")).longValue();
            int count = ((Number) itemMap.get("count")).intValue();

            UserInventory inv = userInventoryRepository.findByUserIdAndItemId(userId, itemId)
                    .orElseGet(() -> new UserInventory(userId, itemId, 0));
            inv.setQuantity(inv.getQuantity() + count);
            userInventoryRepository.save(inv);
        }
    }

    private UserSkill updateUserSkill(Long userId, SkillType skillType, double gainedExp, int maxCombo) {
        UserSkillId skillId = new UserSkillId(userId, skillType);
        UserSkill skill = userSkillRepository.findById(skillId).orElseGet(() -> new UserSkill(userId, skillType, 1, 0));

        if (skill.getMaxCombo() == null || skill.getMaxCombo() < maxCombo) {
            skill.setMaxCombo(maxCombo);
        }

        double totalExp = skill.getExp() + gainedExp;
        int level = skill.getLevel();
        int maxExp = getRequiredExp(level);

        while (totalExp >= maxExp) {
            totalExp -= maxExp;
            level++;
        }

        skill.setLevel(level);
        skill.setExp((int) totalExp);
        return userSkillRepository.save(skill);
    }

    private UserDexStat updateCharacterExp(Long userId, Long dexId, int gainedExp) {
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
        return userDexStatRepository.save(stat);
    }

    private int getRequiredExp(int level) {
        return 100 + (level - 1) * 20;
    }


}
