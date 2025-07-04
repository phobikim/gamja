package com.phobi.gamja.service;

import com.phobi.gamja.dto.quest.*;
import com.phobi.gamja.entity.battle.Monster;
import com.phobi.gamja.entity.chronicle.Chronicle;
import com.phobi.gamja.entity.item.Item;
import com.phobi.gamja.entity.quest.*;
import com.phobi.gamja.entity.title.*;
import com.phobi.gamja.entity.user.*;
import com.phobi.gamja.message.GamJaResponse;
import com.phobi.gamja.repository.chronicle.ChronicleRepository;
import com.phobi.gamja.repository.dex.DexRepository;
import com.phobi.gamja.repository.battle.MonsterRepository;
import com.phobi.gamja.repository.item.ItemRepository;
import com.phobi.gamja.repository.quest.*;
import com.phobi.gamja.repository.title.TitleRepository;
import com.phobi.gamja.repository.title.UserTitleRepository;
import com.phobi.gamja.repository.user.*;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestService {
    private final ActionService actionService;
    private final LogService logService;
    private final CorpsTierService corpsTierService;
    private final UserLogService userLogService;
    private final LevelService levelService;
    private final QuestRepository questRepository;
    private final QuestConditionRepository questConditionRepository;
    private final QuestRewardRepository questRewardRepository;
    private final UserCounterDetailRepository userCounterDetailRepository;
    private final UserQuestRepository userQuestRepository;
    private final UserDailyQuestLogRepository userDailyQuestLogRepository;
    private final UserDailyActionLogRepository userDailyActionLogRepository;
    private final UserChronicleRepository userChronicleRepository;
    private final ChronicleRepository chronicleRepository;

    private final MonsterRepository monsterRepository;
    private final ItemRepository itemRepository;
    private final DexRepository dexRepository;

    private final UserDtlRepository userDtlRepository;
    private final UserInventoryRepository userInventoryRepository;
    private final UserDexStatRepository userDexStatRepository;
    private final UserEquipmentRepository userEquipmentRepository;
    private final UserTitleRepository userTitleRepository;
    private final TitleRepository titleRepository;


    @Transactional(readOnly = true)
    public ResponseEntity<GamJaResponse> getQuestList(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");

        List<QuestDto> mainQuests = getQuestListByType(userId, Quest.QuestType.MAIN, 5);
        List<QuestDto> huntQuests = getQuestListByType(userId, Quest.QuestType.HUNT, 5);
        List<QuestDto> deliveryQuests = getQuestListByType(userId, Quest.QuestType.REQUEST, 5);

        List<QuestDto> combined = new ArrayList<>();
        combined.addAll(mainQuests);
        combined.addAll(huntQuests);
        combined.addAll(deliveryQuests);

        return ResponseEntity.ok(GamJaResponse.success("퀘스트 조회 성공", combined));
    }


    public List<QuestDto> getQuestListByType(Long userId, Quest.QuestType type, int limit) {
        return switch (type) {
            case MAIN -> getMainQuestList(userId, limit);
            case HUNT -> getHuntQuestList(userId, limit);
            case REQUEST -> getRequestQuestList(userId, limit);
        };
    }

    private List<QuestDto> getMainQuestList(Long userId, int limit) {
        List<Quest> allQuests = questRepository.findByTypeAndEnabledIsTrueOrderByMainOrderAsc(Quest.QuestType.MAIN);
        Map<Long, UserQuest> userQuestMap = userQuestRepository.findByIdUserId(userId).stream()
                .collect(Collectors.toMap(uq -> uq.getQuest().getId(), uq -> uq));

        // 누적 카운터
        Map<String, Integer> counterMap = userCounterDetailRepository.findByUserId(userId).stream()
                .collect(Collectors.toMap(
                        c -> c.getCounterType().name() + ":" + c.getTargetId(),
                        UserCounterDetail::getCounterValue,
                        Integer::sum
                ));

        int totalDrawCount = counterMap.entrySet().stream()
                .filter(e -> e.getKey().startsWith("CHARACTER_DRAW:"))
                .mapToInt(Map.Entry::getValue)
                .sum();

        return allQuests.stream()
                .filter(q -> !q.isChronicleFlag())
                .filter(q -> {
                    UserQuest uq = userQuestMap.get(q.getId());
                    return uq == null || uq.getCompletedAt() == null;
                })
                .limit(limit)
                .map(q -> buildQuestDto(userId, q, counterMap, totalDrawCount, null))
                .toList();
    }


    private List<QuestDto> getHuntQuestList(Long userId, int limit) {
        LocalDate today = ZonedDateTime.now(ZoneId.of("Asia/Seoul")).toLocalDate();
        List<Quest> allQuests = questRepository.findByTypeAndEnabledIsTrue(Quest.QuestType.HUNT);

        // ✅ 오늘 완료한 퀘스트 제외 (REQUEST와 동일 로직)
        Set<Long> completedIds = userDailyQuestLogRepository.findByUserIdAndLogDate(userId, today).stream()
                .map(UserDailyQuestLog::getQuestId)
                .collect(Collectors.toSet());

        List<Quest> filtered = allQuests.stream()
                .filter(q -> !q.isChronicleFlag())
                .filter(q -> !completedIds.contains(q.getId()))
                .toList();

        Map<Long, List<QuestCondition>> conditionMap = questConditionRepository.findByQuestIdIn(
                filtered.stream().map(Quest::getId).toList()
        ).stream().collect(Collectors.groupingBy(q -> q.getQuest().getId()));

        // 오늘 일자 로그 (monster_id 기준)
        Map<Long, Integer> huntKillMap = userDailyActionLogRepository.findByUserIdAndLogDate(userId, today).stream()
                .collect(Collectors.toMap(UserDailyActionLog::getMonsterId, UserDailyActionLog::getCount, Integer::sum));

        return filtered.stream()
                .limit(limit)
                .map(q -> buildQuestDto(userId, q, null, 0, huntKillMap))
                .toList();
    }

    private List<QuestDto> getRequestQuestList(Long userId, int limit) {
        LocalDate today = ZonedDateTime.now(ZoneId.of("Asia/Seoul")).toLocalDate();
        List<Quest> allQuests = questRepository.findByTypeAndEnabledIsTrue(Quest.QuestType.REQUEST);

        Set<Long> completedIds = userDailyQuestLogRepository.findByUserIdAndLogDate(userId, today).stream()
                .map(UserDailyQuestLog::getQuestId)
                .collect(Collectors.toSet());

        List<Quest> filtered = allQuests.stream()
                .filter(q -> !q.isChronicleFlag())
                .filter(q -> !completedIds.contains(q.getId()))
                .toList();

        // grade별 제한 5개
        Map<Quest.QuestDifficulty, List<Quest>> grouped = filtered.stream()
                .collect(Collectors.groupingBy(Quest::getGrade));

        List<Quest> limited = new ArrayList<>();
        for (Quest.QuestDifficulty diff : List.of(Quest.QuestDifficulty.EASY, Quest.QuestDifficulty.NORMAL, Quest.QuestDifficulty.HARD)) {
            List<Quest> group = grouped.getOrDefault(diff, List.of());
            limited.addAll(group.stream().limit(limit).toList());
        }

        return limited.stream()
                .map(q -> buildQuestDto(userId, q, null, 0, null))
                .toList();
    }


    private QuestDto buildQuestDto(Long userId, Quest quest,
                                   Map<String, Integer> counterMap,
                                   int totalDrawCount,
                                   Map<Long, Integer> huntKillMap) {

        Map<Long, List<QuestCondition>> conditionMap =
                questConditionRepository.findByQuestIdIn(List.of(quest.getId()))
                        .stream().collect(Collectors.groupingBy(q -> q.getQuest().getId()));

        Map<Long, List<QuestReward>> rewardMap =
                questRewardRepository.findByQuestIdIn(List.of(quest.getId()))
                        .stream().collect(Collectors.groupingBy(r -> r.getQuest().getId()));

        List<QuestConditionDto> conditionDtos = new ArrayList<>();
        boolean achieved = true;

        for (QuestCondition cond : conditionMap.getOrDefault(quest.getId(), List.of())) {
            int current;
            String targetName;

            switch (cond.getCounterType()) {
                case CHARACTER_DRAW -> {
                    current = totalDrawCount;
                    targetName = null;
                }
                case MONSTER_KILL -> {
                    if (quest.isChronicleFlag()) {
                        // 연대기 퀘스트일 경우 map_id → 몬스터 리스트 → 총 킬 수 합산
                        List<Long> monsterIds = monsterRepository.findByMapId(cond.getTargetId()).stream()
                                .map(Monster::getId)
                                .toList();
                        current = monsterIds.stream()
                                .mapToInt(id -> huntKillMap != null ? huntKillMap.getOrDefault(id, 0) : 0)
                                .sum();
                        targetName = monsterRepository.findFirstByMapId(cond.getTargetId())
                                .map(m -> m.getMap().getName())
                                .orElse("해당 지역");
                    } else if (quest.getType() == Quest.QuestType.HUNT) {
                        current = huntKillMap != null
                                ? huntKillMap.getOrDefault(cond.getTargetId(), 0) : 0;
                        targetName = monsterRepository.findById(cond.getTargetId()).map(Monster::getName).orElse("???");
                    } else {
                        String key = "MONSTER_KILL:" + cond.getTargetId();
                        current = counterMap != null ? counterMap.getOrDefault(key, 0) : 0;
                        targetName = monsterRepository.findById(cond.getTargetId()).map(Monster::getName).orElse("???");
                    }
                }
                case ITEM_CRAFT -> {
                    String key = "ITEM_CRAFT:" + cond.getTargetId();
                    current = counterMap != null ? counterMap.getOrDefault(key, 0) : 0;
                    targetName = itemRepository.findById(cond.getTargetId()).map(Item::getName).orElse("???");
                }
                case LIFE_ACTION -> {
                    String key = "LIFE_ACTION:" + cond.getTargetId();
                    current = counterMap != null ? counterMap.getOrDefault(key, 0) : 0;
                    targetName = switch (cond.getTargetId().intValue()) {
                        case 1 -> "벌목"; case 2 -> "낚시"; case 3 -> "채광"; case 4 -> "채집"; default -> "알 수 없음";
                    };
                }
                case EQUIP_ITEM -> {
                    boolean equipped = userEquipmentRepository.existsByUserIdAndItemId(userId, cond.getTargetId());
                    current = equipped ? 1 : 0;
                    targetName = itemRepository.findById(cond.getTargetId()).map(Item::getName).orElse("???");
                }
                case EQUIP_TITLE -> {
                    boolean equipped = userTitleRepository.existsEquippedTitle(userId, cond.getTargetId());
                    current = equipped ? 1 : 0;
                    targetName = titleRepository.findById(cond.getTargetId()).map(Title::getName).orElse("???");
                }
                case DELIVER_ITEM -> {
                    current = Optional.ofNullable(userInventoryRepository.getQuantity(userId, cond.getTargetId())).orElse(0);
                    targetName = itemRepository.findById(cond.getTargetId()).map(Item::getName).orElse("???");
                }
                default -> {
                    current = 0;
                    targetName = null;
                }
            }

            boolean pass = current >= cond.getRequiredCount();
            achieved &= pass;

            conditionDtos.add(QuestConditionDto.builder()
                    .counterType(cond.getCounterType())
                    .targetId(cond.getTargetId())
                    .targetName(targetName)
                    .requiredCount(cond.getRequiredCount())
                    .currentCount(current)
                    .deliverableCount(current)
                    .achieved(pass)
                    .build());
        }

        List<QuestRewardDto> rewardDtos = rewardMap.getOrDefault(quest.getId(), List.of()).stream()
                .map(r -> QuestRewardDto.builder()
                        .rewardType(r.getRewardType())
                        .itemId(r.getItemId())
                        .itemName(r.getItemId() != null
                                ? itemRepository.findById(r.getItemId()).map(Item::getName).orElse("???")
                                : null)
                        .amount(r.getAmount())
                        .build())
                .toList();

        return QuestDto.builder()
                .id(quest.getId())
                .name(quest.getName())
                .description(quest.getDescription())
                .type(quest.getType())
                .difficulty(quest.getGrade())
                .conditions(conditionDtos)
                .rewards(rewardDtos)
                .achieved(achieved)
                .chronicleFlag(quest.isChronicleFlag())
                .mapId(quest.getMonsterMap() != null ? quest.getMonsterMap().getId() : null)
                .build();
    }


    @Transactional
    public ResponseEntity<GamJaResponse> completeQuest(HttpServletRequest request, Map<String, Object> payload) {

        LocalDateTime koreaNow = ZonedDateTime.now(ZoneId.of("Asia/Seoul")).toLocalDateTime();
        Long userId = (Long) request.getAttribute("userId");
        Long questId = ((Number) payload.get("questId")).longValue();
        // 0. 퀘스트 및 조건 조회
        Quest quest = questRepository.findById(questId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 퀘스트입니다."));
        List<QuestCondition> conditions = questConditionRepository.findByQuestId(questId);

        if (quest.getType() == Quest.QuestType.REQUEST || quest.getType() == Quest.QuestType.HUNT) {
            LocalDate today = ZonedDateTime.now(ZoneId.of("Asia/Seoul")).toLocalDate();
            boolean alreadyCompleted = userDailyQuestLogRepository.existsByUserIdAndQuestIdAndLogDate(userId, questId, today);
            if (alreadyCompleted) {
                return ResponseEntity.badRequest().body(GamJaResponse.fail("오늘 이미 완료한 퀘스트입니다."));
            }
        }

        // 1. 납품 퀘스트일 경우, 아이템 수량 차감
        for (QuestCondition cond : conditions) {
            if (cond.getCounterType() == CounterType.DELIVER_ITEM) {
                int owned = Optional.ofNullable(userInventoryRepository.getQuantity(userId, cond.getTargetId()))
                        .orElse(0);
                if (owned < cond.getRequiredCount()) {
                    return ResponseEntity.badRequest().body(GamJaResponse.fail("납품에 필요한 아이템이 부족합니다."));
                }

                int result = userInventoryRepository.consumeItem(userId, cond.getTargetId(), cond.getRequiredCount());
                if (result == 0) {
                    return ResponseEntity.badRequest().body(GamJaResponse.fail("아이템 차감 실패: 수량 부족"));
                }
            }
        }

        List<Map<String, Object>> rewardResults = new ArrayList<>();
        // 2. 보상 처리
        List<QuestReward> rewards = questRewardRepository.findByQuestId(questId);
        for (QuestReward reward : rewards) {
            switch (reward.getRewardType()) {
                case ITEM -> {
                    userInventoryRepository.upsertItem(userId, reward.getItemId(), reward.getAmount());
                }
                case EXP -> {
                    Long dexId = userDtlRepository.findCharacterDexIdByUserId(userId);
                    if (dexId != null) {
                        levelService.updateCharacterExp(userId, dexId, reward.getAmount());
                    }
                }
                case RANDOM_ITEM -> {
                    boolean win = Math.random() < 0.5; //확률 50%
                    Map<String, Object> rewardInfo = new HashMap<>();
                    rewardInfo.put("rewardType", "RANDOM_ITEM");
                    rewardInfo.put("itemId", reward.getItemId());
                    rewardInfo.put("itemName", itemRepository.findById(reward.getItemId())
                            .map(Item::getName).orElse("???"));
                    rewardInfo.put("acquired", win);
                    rewardInfo.put("message", win ? " 뭔가 바스락… 오잉 풀잎 득템!" : "풀향기만 스쳐갔다.");
                    rewardResults.add(rewardInfo);
                    if (win) {
                        userInventoryRepository.upsertItem(userId, reward.getItemId(), 1);
                    }
                }
            }
        }


        // 3. 완료 기록
        UserQuestId uqId = new UserQuestId(userId, questId);
        UserQuest userQuest = userQuestRepository.findById(uqId).orElse(null);
        if (!quest.isRepeatable() && userQuest != null && Boolean.TRUE.equals(userQuest.isCompleted())) {
            return ResponseEntity.badRequest().body(GamJaResponse.fail("이미 완료한 퀘스트입니다."));
        }
        if (userQuest == null) {
            userQuest = UserQuest.builder()
                    .id(uqId)
                    .quest(quest)
                    .build();
        }

        userQuest.setCompleted(true);
        userQuest.setCompletedAt(koreaNow);
        userQuest.setUpdatedAt(koreaNow);

        userQuestRepository.save(userQuest);
        // ✅ QUEST_COMPLETE 카운터 (누적)
        logService.recordCounter(userId, CounterType.QUEST_COMPLETE, 0L);

        // ✅ REQUEST 퀘스트일 경우, 일일 완료 로그 저장
        if (quest.getType() == Quest.QuestType.REQUEST || quest.getType() == Quest.QuestType.HUNT) {
            userLogService.recordDailyQuest(userId, questId);
        }

        corpsTierService.updateCorpsXp(userId, 5);

        Map<String, Object> response = new HashMap<>();
        response.put("rewards", rewardResults); // 랜덤 보상만 포함됨

        return ResponseEntity.ok(GamJaResponse.success("보상 처리 완료", response));
    }



    @Transactional(readOnly = true)
    public ResponseEntity<GamJaResponse> getChronicleQuestList(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        LocalDate today = ZonedDateTime.now(ZoneId.of("Asia/Seoul")).toLocalDate();

        List<Quest> allChronicleQuests = questRepository.findByChronicleFlagTrueAndEnabledIsTrue();

        Set<Long> completedIds = userDailyQuestLogRepository.findByUserIdAndLogDate(userId, today).stream()
                .map(UserDailyQuestLog::getQuestId)
                .collect(Collectors.toSet());

        List<Quest> filtered = allChronicleQuests.stream()
                .filter(q -> !completedIds.contains(q.getId()))
                .toList();

        // 몬스터 잡은 로그 조회
        Map<Long, Integer> huntKillMap = userDailyActionLogRepository.findByUserIdAndLogDate(userId, today).stream()
                .collect(Collectors.toMap(UserDailyActionLog::getMonsterId, UserDailyActionLog::getCount, Integer::sum));

        // 중간 납품 현황 조회
        Map<Long, Integer> progressMap = userChronicleRepository.findByUserId(userId).stream()
                .filter(uc -> uc.getQuest() != null)
                .collect(Collectors.toMap(
                        uc -> uc.getQuest().getId(), // quest_id 기준
                        UserChronicle::getProgressCount
                ));

        // 유저 보유량 조회
        Map<Long, Integer> inventoryMap = userInventoryRepository.findByUserId(userId).stream()
                .collect(Collectors.toMap(UserInventory::getItemId, UserInventory::getQuantity));

        List<ChronicleQuestDto> result = filtered.stream()
                .map(q -> buildChronicleQuestDto(userId, q, huntKillMap, progressMap, inventoryMap))
                .toList();

        return ResponseEntity.ok(GamJaResponse.success("연대기 퀘스트 조회 성공", result));
    }

    private ChronicleQuestDto buildChronicleQuestDto(
            Long userId,
            Quest quest,
            Map<Long, Integer> huntKillMap,
            Map<Long, Integer> progressMap,
            Map<Long, Integer> inventoryMap) {

        List<QuestCondition> conditions = questConditionRepository.findByQuestId(quest.getId());
        List<QuestReward> rewards = questRewardRepository.findByQuestId(quest.getId());

        List<ChronicleQuestConditionDto> conditionDtos = new ArrayList<>();
        boolean achieved = true;

        for (QuestCondition cond : conditions) {
            int current = 0;
            int deliverable = 0;
            String name = null;

            switch (cond.getCounterType()) {
                case MONSTER_KILL -> {
                    List<Long> monsterIds = monsterRepository.findByMapId(cond.getTargetId()).stream()
                            .map(Monster::getId).toList();

                    current = monsterIds.stream()
                            .mapToInt(id -> huntKillMap.getOrDefault(id, 0))
                            .sum();

                    name = monsterRepository.findFirstByMapId(cond.getTargetId())
                            .map(m -> m.getMap().getName()).orElse("해당 지역");
                }

                case DELIVER_ITEM -> {
                    deliverable = inventoryMap.getOrDefault(cond.getTargetId(), 0);
                    name = itemRepository.findById(cond.getTargetId()).map(Item::getName).orElse("???");

                    if (quest.isRepeatable()) {
                        // ✅ 반복 가능 퀘스트: current는 인벤토리 수량
                        current = deliverable;
                    } else {
                        // ✅ 반복 불가능 퀘스트: 중간 납품 기준으로 진행도 조회
                        current = progressMap.getOrDefault(quest.getId(), 0);
                    }
                }

                default -> {
                    // 연대기는 MONSTER_KILL, DELIVER_ITEM만 씀
                }
            }

            boolean pass = current >= cond.getRequiredCount();
            achieved &= pass;

            conditionDtos.add(ChronicleQuestConditionDto.builder()
                    .counterType(cond.getCounterType())
                    .targetId(cond.getTargetId())
                    .targetName(name)
                    .requiredCount(cond.getRequiredCount())
                    .currentCount(current)
                    .deliverableCount(deliverable)
                    .achieved(pass)
                    .build());
        }

        List<QuestRewardDto> rewardDtos = rewards.stream()
                .map(r -> QuestRewardDto.builder()
                        .rewardType(r.getRewardType())
                        .itemId(r.getItemId())
                        .itemName(r.getItemId() != null
                                ? itemRepository.findById(r.getItemId()).map(Item::getName).orElse("???")
                                : null)
                        .amount(r.getAmount())
                        .build())
                .toList();

        return ChronicleQuestDto.builder()
                .id(quest.getId())
                .name(quest.getName())
                .description(quest.getDescription())
                .mapId(quest.getMonsterMap() != null ? quest.getMonsterMap().getId() : null)
                .difficulty(quest.getGrade())
                .achieved(achieved)
                .repeated(quest.isRepeatable())
                .allowPartialDelivery(quest.isAllowPartialDelivery())
                .conditions(conditionDtos)
                .rewards(rewardDtos)
                .build();
    }

    @Transactional
    public ResponseEntity<GamJaResponse> progressChronicleQuest(HttpServletRequest request, Map<String, Object> payload) {
        Long userId = (Long) request.getAttribute("userId");
        Long questId = ((Number) payload.get("questId")).longValue();

        // 1. 퀘스트 조회
        Quest quest = questRepository.findById(questId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 퀘스트입니다."));

        if (!quest.isChronicleFlag() || quest.isRepeatable()) {
            return ResponseEntity.ok(GamJaResponse.fail("중간납품이 불가능한 퀘스트입니다."));
        }

        List<QuestCondition> conditions = questConditionRepository.findByQuestId(questId);

        // 2. 인벤토리, 중간납품 현황 조회
        Map<Long, Integer> inventoryMap = userInventoryRepository.findByUserId(userId).stream()
                .collect(Collectors.toMap(UserInventory::getItemId, UserInventory::getQuantity));

        Map<Long, UserChronicle> chronicleMap = userChronicleRepository.findByUserId(userId).stream()
                .filter(uc -> uc.getQuest() != null && uc.getQuest().getId().equals(questId))
                .collect(Collectors.toMap(uc -> uc.getChronicle().getId(), uc -> uc));

        for (QuestCondition cond : conditions) {
            if (cond.getCounterType() != CounterType.DELIVER_ITEM) continue;

            Long itemId = cond.getTargetId();

            Chronicle chronicle = chronicleRepository.findByTargetTypeAndTargetId(Chronicle.ChronicleTargetType.ITEM, itemId)
                    .orElse(null);

            if (chronicle == null) continue;

            int owned = inventoryMap.getOrDefault(itemId, 0);
            int progress = chronicleMap.getOrDefault(chronicle.getId(), new UserChronicle()).getProgressCount();
            int required = cond.getRequiredCount();

            // 납품 가능한 수량
            int deliverAmount = Math.min(required - progress, owned);
            if (deliverAmount <= 0) continue;

            // 3. 인벤토리 차감
            userInventoryRepository.consumeItem(userId, itemId, deliverAmount);

            // 4. user_chronicle 갱신 (있으면 update, 없으면 insert)
            UserChronicle userChronicle = chronicleMap.get(chronicle.getId());
            if (userChronicle == null) {
                userChronicle = UserChronicle.builder()
                        .userId(userId)
                        .quest(quest)
                        .chronicle(chronicle)
                        .progressCount(deliverAmount)
                        .completed(false)
                        .build();
            } else {
                userChronicle.setProgressCount(userChronicle.getProgressCount() + deliverAmount);
            }

            userChronicleRepository.save(userChronicle);
        }

        return ResponseEntity.ok(GamJaResponse.success("중간납품 완료", null));
    }

    @Transactional
    public ResponseEntity<GamJaResponse> completeChronicleQuest(HttpServletRequest request, Map<String, Object> payload) {
        LocalDateTime koreaNow = ZonedDateTime.now(ZoneId.of("Asia/Seoul")).toLocalDateTime();
        Long userId = (Long) request.getAttribute("userId");
        Long questId = ((Number) payload.get("questId")).longValue();

        // 1. 퀘스트 확인
        Quest quest = questRepository.findById(questId)
                .orElseThrow(() -> new IllegalArgumentException("퀘스트가 존재하지 않습니다."));

        if (!quest.isChronicleFlag()) {
            throw new IllegalArgumentException("연대기 퀘스트가 아닙니다.");
        }

        List<QuestCondition> conditions = questConditionRepository.findByQuestId(questId);

        for (QuestCondition condition : conditions) {
            if (condition.getCounterType() != CounterType.DELIVER_ITEM) continue;
            long itemId = condition.getTargetId();
            int required = condition.getRequiredCount();

            // 먼저 인벤토리 차감 처리
            int owned = userInventoryRepository.getQuantity(userId, itemId);
            if (owned < required) {
                throw new IllegalArgumentException("아이템 수량이 부족합니다.");
            }

            int updated = userInventoryRepository.consumeItem(userId, itemId, required);
            if (updated == 0) {
                throw new IllegalArgumentException("아이템 차감에 실패했습니다.");
            }

            // 연대기 기록은 repeatable이 아닐 때만 수행
            if (quest.isRepeatable()) continue;


            Chronicle chronicle = chronicleRepository.findByTargetTypeAndTargetId(
                            Chronicle.ChronicleTargetType.ITEM, itemId)
                    .orElseThrow(() -> new IllegalArgumentException("연결된 연대기 정보가 없습니다."));

            // 5. user_chronicle 생성 또는 갱신
            UserChronicle userChronicle = userChronicleRepository.findByUserIdAndChronicle(userId, chronicle)
                    .orElseGet(() -> {
                        UserChronicle uc = new UserChronicle();
                        uc.setUserId(userId);
                        uc.setChronicle(chronicle);
                        uc.setQuest(quest);
                        return uc;
                    });

            userChronicle.setProgressCount(required);
            userChronicle.setCompleted(true);
            userChronicleRepository.save(userChronicle);
        }

        // ✅ 보상 처리
        List<Map<String, Object>> rewardResults = new ArrayList<>();
        List<QuestReward> rewards = questRewardRepository.findByQuestId(questId);

        for (QuestReward reward : rewards) {
            switch (reward.getRewardType()) {
                case ITEM -> {
                    userInventoryRepository.upsertItem(userId, reward.getItemId(), reward.getAmount());
                }
                case EXP -> {
                    Long dexId = userDtlRepository.findCharacterDexIdByUserId(userId);
                    if (dexId != null) {
                        levelService.updateCharacterExp(userId, dexId, reward.getAmount());
                    }
                }
                case RANDOM_ITEM -> {
                    boolean win = Math.random() < 0.5;
                    Map<String, Object> rewardInfo = new HashMap<>();
                    rewardInfo.put("rewardType", "RANDOM_ITEM");
                    rewardInfo.put("itemId", reward.getItemId());
                    rewardInfo.put("itemName", itemRepository.findById(reward.getItemId())
                            .map(Item::getName).orElse("???"));
                    rewardInfo.put("acquired", win);
                    rewardInfo.put("message", win ? " 뭔가 바스락… 오잉 풀잎 득템!" : "풀향기만 스쳐갔다.");
                    rewardResults.add(rewardInfo);
                    if (win) {
                        userInventoryRepository.upsertItem(userId, reward.getItemId(), 1);
                    }
                }
            }
        }

        // 완료 기록
        UserQuestId uqId = new UserQuestId(userId, questId);
        UserQuest userQuest = userQuestRepository.findById(uqId).orElse(null);
        if (!quest.isRepeatable() && userQuest != null && Boolean.TRUE.equals(userQuest.isCompleted())) {
            return ResponseEntity.badRequest().body(GamJaResponse.fail("이미 완료한 퀘스트입니다."));
        }
        if (userQuest == null) {
            userQuest = UserQuest.builder()
                    .id(uqId)
                    .quest(quest)
                    .build();
        }

        userQuest.setCompleted(true);
        userQuest.setCompletedAt(koreaNow);
        userQuest.setUpdatedAt(koreaNow);

        userQuestRepository.save(userQuest);
        // ✅ QUEST_COMPLETE 카운터 (누적)
        logService.recordCounter(userId, CounterType.QUEST_COMPLETE, 0L);

        // ✅ REQUEST 퀘스트일 경우, 일일 완료 로그 저장
        if (quest.getType() == Quest.QuestType.REQUEST || quest.getType() == Quest.QuestType.HUNT) {
            userLogService.recordDailyQuest(userId, questId);
        }

        corpsTierService.updateCorpsXp(userId, 5);

        Map<String, Object> response = new HashMap<>();
        response.put("rewards", rewardResults); // 랜덤 보상만 포함됨

        return ResponseEntity.ok(GamJaResponse.success("퀘스트가 완료되었습니다.", response));
    }


}
