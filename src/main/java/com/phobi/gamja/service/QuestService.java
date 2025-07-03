package com.phobi.gamja.service;

import com.phobi.gamja.dto.quest.*;
import com.phobi.gamja.entity.battle.Monster;
import com.phobi.gamja.entity.item.Item;
import com.phobi.gamja.entity.quest.*;
import com.phobi.gamja.entity.title.*;
import com.phobi.gamja.entity.user.*;
import com.phobi.gamja.message.GamJaResponse;
import com.phobi.gamja.repository.dex.DexRepository;
import com.phobi.gamja.repository.battle.MonsterRepository;
import com.phobi.gamja.repository.item.ItemRepository;
import com.phobi.gamja.repository.quest.*;
import com.phobi.gamja.repository.title.TitleRepository;
import com.phobi.gamja.repository.title.UserTitleRepository;
import com.phobi.gamja.repository.user.*;
import lombok.RequiredArgsConstructor;
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

    @Transactional(readOnly = true)
    public ResponseEntity<GamJaResponse> getChronicleQuestList(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        LocalDate today = ZonedDateTime.now(ZoneId.of("Asia/Seoul")).toLocalDate();

        // ✅ 연대기용 퀘스트만 필터링
        List<Quest> allChronicleQuests = questRepository.findByChronicleFlagTrueAndEnabledIsTrue();

        // ✅ 오늘 완료한 퀘스트 제외
        Set<Long> completedIds = userDailyQuestLogRepository.findByUserIdAndLogDate(userId, today).stream()
                .map(UserDailyQuestLog::getQuestId)
                .collect(Collectors.toSet());

        List<Quest> filtered = allChronicleQuests.stream()
                .filter(q -> !completedIds.contains(q.getId()))
                .toList();

        List<QuestDto> chronicleQuests = filtered.stream()
                .map(q -> buildQuestDto(userId, q, null, 0, null))
                .toList();

        return ResponseEntity.ok(GamJaResponse.success("연대기 퀘스트 조회 성공", chronicleQuests));
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
                    if (quest.getType() == Quest.QuestType.HUNT) {
                        current = huntKillMap != null
                                ? huntKillMap.getOrDefault(cond.getTargetId(), 0) : 0;
                    } else {
                        String key = "MONSTER_KILL:" + cond.getTargetId();
                        current = counterMap != null ? counterMap.getOrDefault(key, 0) : 0;
                    }
                    targetName = monsterRepository.findById(cond.getTargetId()).map(Monster::getName).orElse("???");
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

        // 2. 보상 처리
        List<QuestReward> rewards = questRewardRepository.findByQuestId(questId);
        for (QuestReward reward : rewards) {
            if (reward.getRewardType() == QuestReward.RewardType.ITEM) {
                userInventoryRepository.upsertItem(userId, reward.getItemId(), reward.getAmount());
            } else if (reward.getRewardType() == QuestReward.RewardType.EXP) {
                Long dexId = userDtlRepository.findCharacterDexIdByUserId(userId);
                if (dexId != null) {
                    levelService.updateCharacterExp(userId, dexId, reward.getAmount());
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
        return ResponseEntity.ok(GamJaResponse.success("보상 처리 완료", null));
    }
}
