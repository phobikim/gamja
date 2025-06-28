// ActionService.java
package com.phobi.gamja.service;


import com.phobi.gamja.dto.user.LifeStatDto;
import com.phobi.gamja.dto.user.UserCharInfoDto;
import com.phobi.gamja.dto.user.UserDexXpDto;
import com.phobi.gamja.entity.battle.Monster;
import com.phobi.gamja.entity.battle.MonsterDrop;
import com.phobi.gamja.entity.contents.*;
import com.phobi.gamja.entity.item.Item;
import com.phobi.gamja.entity.item.ItemReward;
import com.phobi.gamja.entity.title.Title;
import com.phobi.gamja.entity.title.TitleCondition;
import com.phobi.gamja.entity.title.UserTitle;
import com.phobi.gamja.entity.title.UserTitleId;
import com.phobi.gamja.entity.user.*;
import com.phobi.gamja.message.GamJaResponse;
import com.phobi.gamja.repository.battle.MonsterDropRepository;
import com.phobi.gamja.repository.battle.MonsterRepository;
import com.phobi.gamja.repository.contents.*;
import com.phobi.gamja.repository.item.ItemRepository;
import com.phobi.gamja.repository.title.TitleEffectRepository;
import com.phobi.gamja.repository.title.TitleRepository;
import com.phobi.gamja.repository.title.UserTitleRepository;
import com.phobi.gamja.repository.user.*;
import com.phobi.gamja.util.CommonUtil;
import com.phobi.gamja.util.StatCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

import javax.servlet.http.HttpSession;
import java.util.*;

import static com.phobi.gamja.entity.contents.SkillType.*;

@Service
@RequiredArgsConstructor
public class ActionService {

    private final ItemRepository itemRepository;
    private final UserInventoryRepository userInventoryRepository;
    private final UserSkillRepository userSkillRepository;
    private final ActionRepository actionRepository;
    private final UserDtlRepository userDtlRepository;
    private final UserDexStatRepository userDexStatRepository;
    private final ActionCardEventRepository actionCardEventRepository;
    private final ActionCardEventDropRepository actionCardEventDropRepository;
    private final MonsterRepository monsterRepository;
    private final MonsterDropRepository monsterDropRepository;

    // 타이틀 관련
    private final TitleRepository titleRepository;
    private final TitleEffectRepository titleEffectRepository;
    private final UserTitleRepository userTitleRepository;
    private final UserCounterDetailRepository userCounterDetailRepository;


    private final LogService logService;
    private final UserLogService userLogService;
    private final CorpsTierService corpsTierService;
    private final CommonUtil commonUtil;
    private final StatCalculator statCalculator;



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

            List<SpotPreviewDto> spots = actions.stream()
                    .map(SpotPreviewDto::of)
                    .toList();

            Map<String, Object> result = new HashMap<>();
            result.put("level", level);
            result.put("exp", exp);
            result.put("maxCombo", maxCombo);
            result.put("spots", spots);

            return ResponseEntity.ok(GamJaResponse.success("정상 조회", result));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(GamJaResponse.fail("잘못된 카테고리"));
        }
    }


    public ResponseEntity<GamJaResponse> getCardEvents(String activity, int rank, HttpSession session) {
        Long userId = getUserId(session);
        ActivityType activityType = ActivityType.valueOf(activity.toUpperCase());

        // 카드 풀 불러오기
        List<ActionCardEvent> allEvents = actionCardEventRepository
                .findByActivityTypeAndRankAndIsEnabledTrue(activityType, rank);

        if (allEvents.size() < 2) {
            return ResponseEntity.ok(GamJaResponse.fail("카드 이벤트가 부족합니다."));
        }

        // 랜덤 2장 추출
        Collections.shuffle(allEvents);
        List<ActionCardEvent> selectedEvents = allEvents.subList(0, 2);

        // ExplorationSession 생성
        ExplorationSession exploration = new ExplorationSession();
        exploration.setUserId(userId);
        exploration.setHp(3);
        exploration.setStage(1);
        exploration.setUsedCardIds(new ArrayList<>());
        exploration.setRewards(new ArrayList<>());
        exploration.setCurrentChoices(selectedEvents);

        session.setAttribute("explorationSession", exploration);

        // ✅ drops 조회 및 DTO 변환
        List<CardPreviewDto> result = selectedEvents.stream()
                .map(event -> {
                    List<ActionCardEventDrop> drops = Optional.ofNullable(event.getDropGroupId())
                            .map(actionCardEventDropRepository::findByDropGroupId)
                            .orElse(List.of());
                    return CardPreviewDto.of(event, drops);
                })
                .toList();

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("hp", exploration.getHp());
        responseBody.put("stage", exploration.getStage());
        responseBody.put("currentChoices", result);

        return ResponseEntity.ok(GamJaResponse.success("카드 이벤트 시작", responseBody));
    }


    public record DropResult(List<Map<String, Object>> visibleRewards, List<ItemReward> internalRewards) {}
    public ResponseEntity<GamJaResponse> endBattle(HttpSession session, Map<String, Object> request) {

        Long userId = getUserId(session);
        Long monsterId = ((Number) request.get("monsterId")).longValue();

        UserDtl userDtl = getUserDtl(userId);
        Long dexId = Optional.ofNullable(userDtl.getCharacterDexId())
                .orElseThrow(() -> new IllegalArgumentException("착용 중인 캐릭터가 없습니다."));
        // 몬스터 조회
        Monster monster = monsterRepository.findById(monsterId)
                .orElseThrow(() -> new IllegalArgumentException("몬스터 없음"));

        // 경험치 획득
        int gainedXp = monster.getMonsterXp();
        UserDexXpDto stat = updateCharacterExp(userId, dexId, gainedXp);

        // 드랍 계산
        DropResult dropResult = getDropResult(monster);
        processItemRewards(userId, dropResult.internalRewards());


        logService.recordCounter(userId, CounterType.MONSTER_KILL, monsterId);
        userLogService.recordDailyMonster(userId, monsterId);
        corpsTierService.updateCorpsXp(userId, 1);

        Map<String, Object> result = new HashMap<>();
        result.put("xp", stat.getXp());
        result.put("maxExp", stat.getMaxExp());
        result.put("level", stat.getLevel());
        result.put("gainedXp", gainedXp);
        result.put("items", dropResult.visibleRewards());

        return ResponseEntity.ok(GamJaResponse.success("전투 보상 처리 완료", result));
    }



    public DropResult getDropResult(Monster monster) {
        List<MonsterDrop> drops = monsterDropRepository.findByMonster(monster);
        List<Map<String, Object>> visible = new ArrayList<>();
        List<ItemReward> internal = new ArrayList<>();

        for (MonsterDrop drop : drops) {
            if (Math.random() * 100 <= drop.getDropRate()) {
                int count = drop.getMinCount() + new Random().nextInt(drop.getMaxCount() - drop.getMinCount() + 1);
                Item item = drop.getItem();

                // 클라이언트용
                visible.add(Map.of(
                        "name", item.getName(),
                        "iconPath", item.getIconPath(),
                        "rarity", item.getRarity().name(),
                        "count", count
                ));

                // 내부 처리용
                internal.add(new ItemReward(item, count));
            }
        }

        return new DropResult(visible, internal);
    }

    public ResponseEntity<GamJaResponse> resolveCardDropResponse(HttpSession session, Map<String, Object> request) {
        Long eventId = ((Number) request.get("eventId")).longValue();

        // 1. 세션 유효성 검증
        ExplorationSession exploration = (ExplorationSession) session.getAttribute("explorationSession");
        if (exploration == null) {
            return ResponseEntity.ok(GamJaResponse.fail("진행 중인 탐사가 없습니다."));
        }

        Long userId = getUserId(session);

        // 2. 현재 선택 가능한 카드인지 검증
        boolean validChoice = exploration.getCurrentChoices().stream()
                .anyMatch(e -> e.getId().equals(eventId));
        if (!validChoice) {
            return ResponseEntity.ok(GamJaResponse.fail("유효하지 않은 카드 선택입니다."));
        }

        // 3. 카드 이벤트 조회
        ActionCardEvent event = actionCardEventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("카드 이벤트가 존재하지 않습니다."));

        // 4. HP 처리
        int hp = exploration.getHp() + event.getHpChange();
        exploration.setHp(hp);

        int stage = exploration.getStage() + 1;
        exploration.setStage(stage);

        Map<String, Object> dropResult = null;
        // 6. 드랍 처리
        if (event.getEventType() == EventType.RESOURCE && event.getDropGroupId() != null) {
            List<ActionCardEventDrop> drops = actionCardEventDropRepository.findByDropGroupId(event.getDropGroupId());
            Random rand = new Random();

            List<ActionCardEventDrop> candidates = drops.stream()
                    .filter(drop -> rand.nextFloat() <= drop.getDropRate())
                    .toList();

            ActionCardEventDrop selectedDrop = candidates.isEmpty()
                    ? drops.get(rand.nextInt(drops.size()))
                    : candidates.get(rand.nextInt(candidates.size()));

            int qty = rand.nextInt(selectedDrop.getMaxQuantity() - selectedDrop.getMinQuantity() + 1)
                    + selectedDrop.getMinQuantity();

            // stage 보상 배수 적용
            int multiplier = stage >= 20 ? 3 : (stage >= 10 ? 2 : 1);
            qty *= multiplier;

            Item item = selectedDrop.getItem();

            ExplorationReward reward = new ExplorationReward();
            reward.setStage(stage);
            reward.setTotalExp(calculateExplorationExp(stage));
            reward.addDrop(item.getId(), item.getName(), item.getIconPath(), qty);
            exploration.getRewards().add(reward);

            dropResult = Map.of(
                    "itemId", item.getId(),
                    "itemName", item.getName(),
                    "iconPath", item.getIconPath(),
                    "count", qty,
                    "multiplier", multiplier
            );
        }
        // 7. 사용한 카드 ID 기록
        exploration.getUsedCardIds().add(eventId);

        // 8. 다음 카드 2장 갱신
        List<ActionCardEvent> pool = actionCardEventRepository.findByActivityTypeAndRankAndIsEnabledTrue(
                event.getActivityType(), event.getRank());
        pool.removeIf(e -> exploration.getUsedCardIds().contains(e.getId()));
        Collections.shuffle(pool);

        List<ActionCardEvent> nextChoices = pool.size() >= 2 ? pool.subList(0, 2) : List.of();
        exploration.setCurrentChoices(nextChoices);

        // 9. 세션 갱신
        session.setAttribute("explorationSession", exploration);

        // ✅ 카드 미리보기 DTO 변환
        List<CardPreviewDto> previewDtos = nextChoices.stream()
                .map(card -> {
                    List<ActionCardEventDrop> drops = Optional.ofNullable(card.getDropGroupId())
                            .map(actionCardEventDropRepository::findByDropGroupId)
                            .orElse(List.of());
                    return CardPreviewDto.of(card, drops);
                })
                .toList();

        Map<String, Object> response = new HashMap<>();
        response.put("hp", exploration.getHp());
        response.put("stage", exploration.getStage());
        response.put("isEnd", exploration.getHp() <= 0);
        response.put("nextChoices", previewDtos);

        if (dropResult != null) {
            response.putAll(dropResult);
        }

        return ResponseEntity.ok(GamJaResponse.success(
                dropResult != null ? "아이템 획득" : "아이템 없음", response
        ));
    }


    public ResponseEntity<GamJaResponse> endExploration(HttpSession session, Map<String, Object> request) {
        Long userId = getUserId(session);
        SkillType skillType = SkillType.valueOf(((String) request.get("activityType")).toUpperCase());

        // 1. 세션 꺼내기
        ExplorationSession exploration = (ExplorationSession) session.getAttribute("explorationSession");
        if (exploration == null) {
            return ResponseEntity.badRequest().body(GamJaResponse.fail("진행 중인 탐사가 없습니다."));
        }

        int stage = exploration.getStage();
        int maxCombo = stage;
        int totalExp = calculateExplorationExp(stage);

        // 2. 인벤토리 보상 반영 + 누적 아이템 리스트 구성
        List<Map<String, Object>> itemList = new ArrayList<>();
        Map<Long, Map<String, Object>> merged = new HashMap<>();

        for (ExplorationReward reward : exploration.getRewards()) {
            for (Map<String, Object> drop : reward.getDrops()) {
                Long itemId = ((Number) drop.get("itemId")).longValue();
                int count = ((Number) drop.get("count")).intValue();
                String itemName = (String) drop.get("itemName");
                String iconPath = (String) drop.get("iconPath");

                // 인벤토리에 저장
                UserInventory inv = userInventoryRepository.findByUserIdAndItemId(userId, itemId)
                        .orElseGet(() -> new UserInventory(userId, itemId, 0));
                inv.setQuantity(inv.getQuantity() + count);
                userInventoryRepository.save(inv);

                // 누적 정리 (itemId는 key로만 사용하고, 내려보내지 않음)
                if (!merged.containsKey(itemId)) {
                    Map<String, Object> itemInfo = new HashMap<>();
                    itemInfo.put("itemName", itemName);
                    itemInfo.put("iconPath", iconPath);
                    itemInfo.put("count", count);
                    merged.put(itemId, itemInfo);
                } else {
                    Map<String, Object> existing = merged.get(itemId);
                    int old = (int) existing.get("count");
                    existing.put("count", old + count);
                }
            }
        }

        itemList.addAll(merged.values());

        // 3. EXP 계산 + 스킬 업데이트
        UserSkill userSkill = updateUserSkill(userId, skillType, totalExp, maxCombo);

        // 4. 로그 기록
        if (EnumSet.of(SkillType.WOODCUTTING, FISHING, MINING, GATHERING).contains(skillType)) {
            Long actionId = getLifeActionTargetId(skillType);
            logService.recordCounter(userId, CounterType.LIFE_ACTION, actionId);
        }

        // 5. 부가 보상
        corpsTierService.updateCorpsXp(userId, 1);

        // 6. 세션 초기화
        session.removeAttribute("explorationSession");

        // ✅ 최종 응답
        Map<String, Object> result = new HashMap<>();
        result.put("skillType", skillType.name());
        result.put("level", userSkill.getLevel());
        result.put("xp", userSkill.getExp());
        result.put("maxExp", getRequiredExp(userSkill.getLevel()));
        result.put("maxCombo", userSkill.getMaxCombo());
        result.put("stage", exploration.getStage()); // 탐사에서 실제 도달한 스테이지
        result.put("gainedExp", totalExp); // 서버 계산한 경험치
        result.put("items", itemList);

        return ResponseEntity.ok(GamJaResponse.success("탐사 완료", result));
    }


    private int calculateExplorationExp(int stage) {
        if (stage <= 5) return 0;
        if (stage <= 10) return 5;
        if (stage <= 20) return 10;
        if (stage <= 30) return 15;
        if (stage <= 40) return 20;
        return 20;
    }


    private Long getLifeActionTargetId(SkillType skillType) {
        return switch (skillType) {
            case WOODCUTTING -> 1L;
            case FISHING    -> 2L;
            case MINING     -> 3L;
            case GATHERING  -> 4L;
            default -> throw new IllegalArgumentException("LIFE_ACTION에 해당하지 않는 스킬입니다: " + skillType);
        };
    }

    // ✅ 칭호 수동 획득
    public ResponseEntity<GamJaResponse> claimTitle(HttpSession session, Map<String, Object> request) {
        Long userId = getUserId(session);
        Long titleId = ((Number) request.get("titleId")).longValue();

        Title title = titleRepository.findById(titleId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 칭호입니다."));

        List<TitleCondition> conditions = title.getConditions();

        if (title.getCounterType() == CounterType.LIFE_ACTION) {
            checkLifeStatConditions(userId, conditions);
        } else if (isTargetlessCondition(conditions)) {
            checkTotalCounterConditions(userId, title.getCounterType(), conditions);
        } else {
            checkTargetCounterConditions(userId, title.getCounterType(), conditions);
        }


        // 이미 보유 중인지 체크
        boolean alreadyOwned = userTitleRepository.existsById(new UserTitleId(userId, titleId));
        if (alreadyOwned) {
            return ResponseEntity.badRequest().body(GamJaResponse.fail("이미 보유 중인 칭호입니다."));
        }

        // 저장
        UserTitle newTitle = UserTitle.builder()
                .id(new UserTitleId(userId, titleId))  // 복합키
                .title(title)
                .isOwned(true)
                .isEquipped(false)
                .build();
        userTitleRepository.save(newTitle);

        corpsTierService.updateCorpsXp(userId, 10);
        return ResponseEntity.ok(GamJaResponse.success("칭호를 획득했습니다.",null));
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
            Title equippedTitle = userTitleRepository.findByIdUserIdAndIsEquippedTrue(userId)
                    .map(UserTitle::getTitle)
                    .orElse(null);
        }

        return ResponseEntity.ok(GamJaResponse.success("칭호를 착용했습니다.", null));
    }

    /* 타이틀 계산 함수 */

    //생활 스탯 조건 함수
    private void checkLifeStatConditions(Long userId, List<TitleCondition> conditions) {
        LifeStatDto stat = statCalculator.calculateLifeSkill(userId);

        for (TitleCondition cond : conditions) {
            int level = switch (cond.getLifeType()) {
                case FISHING -> stat.getFishing().getTotal();
                case MINING -> stat.getMining().getTotal();
                case WOODCUTTING -> stat.getWoodcutting().getTotal();
                case GATHERING -> stat.getGathering().getTotal();
                case MAKING -> stat.getMaking().getTotal();
                default -> 0;
            };

            if (level < cond.getRequiredCount()) {
                throw new IllegalStateException("생활 스킬 조건 미달");
            }
        }
    }

    //특정 대상 기반 조건 함수 (targetId 있음)
    private void checkTargetCounterConditions(Long userId, CounterType type, List<TitleCondition> conditions) {
        List<UserCounterDetail> counters = userCounterDetailRepository.findByUserId(userId);
        for (TitleCondition cond : conditions) {
            Long targetId = cond.getTargetId();
            int required = cond.getRequiredCount();

            int current = counters.stream()
                    .filter(c -> c.getCounterType() == type && c.getTargetId().equals(targetId))
                    .mapToInt(UserCounterDetail::getCounterValue)
                    .sum();

            if (current < required) {
                throw new IllegalStateException("카운터 조건 미달 (타겟 기반)");
            }
        }
    }
    //누적 카운트 조건 함수 (targetId == 0)
    private void checkTotalCounterConditions(Long userId, CounterType type, List<TitleCondition> conditions) {
        List<UserCounterDetail> counters = userCounterDetailRepository.findByUserId(userId);
        int total = counters.stream()
                .filter(c -> c.getCounterType() == type)
                .mapToInt(UserCounterDetail::getCounterValue)
                .sum();

        for (TitleCondition cond : conditions) {
            if (total < cond.getRequiredCount()) {
                throw new IllegalStateException("카운터 누적합 조건 미달");
            }
        }
    }
    private boolean isTargetlessCondition(List<TitleCondition> conditions) {
        return conditions.stream().allMatch(c -> c.getTargetId() == null || c.getTargetId() == 0);
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

    private void processItemRewards(Long userId, List<ItemReward> items) {
        for (ItemReward reward : items) {
            Long itemId = reward.getItem().getId();
            int count = reward.getCount();

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

    private int getRequiredExp(int level) {
        return 100 + (level - 1) * 20;
    }


}
