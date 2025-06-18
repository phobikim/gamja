package com.phobi.gamja.service;

import com.phobi.gamja.dto.quest.*;
import com.phobi.gamja.entity.contents.Monster;
import com.phobi.gamja.entity.item.Item;
import com.phobi.gamja.entity.quest.*;
import com.phobi.gamja.entity.title.*;
import com.phobi.gamja.entity.user.CounterType;
import com.phobi.gamja.entity.user.UserCounterDetail;
import com.phobi.gamja.message.GamJaResponse;
import com.phobi.gamja.repository.contents.DexRepository;
import com.phobi.gamja.repository.contents.MonsterRepository;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestService {
    private final ActionService actionService;
    private final LogService logService;
    private final QuestRepository questRepository;
    private final QuestConditionRepository questConditionRepository;
    private final QuestRewardRepository questRewardRepository;
    private final UserCounterDetailRepository userCounterDetailRepository;
    private final UserQuestRepository userQuestRepository;

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

    private List<QuestDto> getQuestListByType(Long userId, Quest.QuestType type, int limit) {
        LocalDate today = LocalDate.now();
        List<Quest> allQuests = (type == Quest.QuestType.MAIN)
                ? questRepository.findByTypeAndEnabledIsTrueOrderByMainOrderAsc(type)
                : questRepository.findByTypeAndEnabledIsTrue(type);
        // user_quest 데이터 조회
        Map<Long, UserQuest> userQuestMap = userQuestRepository.findByIdUserId(userId).stream()
                .collect(Collectors.toMap(
                        uq -> uq.getQuest().getId(),
                        uq -> uq
                ));

        List<Quest> filtered = allQuests.stream()
                .filter(q -> {
                    UserQuest uq = userQuestMap.get(q.getId());

                    // 완료한 적 없으면 표시
                    if (uq == null || uq.getCompletedAt() == null) return true;

                    // 반복 불가면 제외
                    if (!q.isRepeatable()) return false;

                    // 오늘보다 이전에 완료한 퀘스트만 다시 표시
                    return uq.getCompletedAt().toLocalDate().isBefore(today);
                })
                .limit(limit)
                .toList();
        List<Long> questIds = filtered.stream().map(Quest::getId).toList();
        // 조건 맵: questId → List<QuestCondition>
        Map<Long, List<QuestCondition>> conditionMap =
                questConditionRepository.findByQuestIdIn(questIds).stream()
                        .collect(Collectors.groupingBy(q -> q.getQuest().getId()));

        // 보상 맵: questId → List<QuestReward>
        Map<Long, List<QuestReward>> rewardMap =
                questRewardRepository.findByQuestIdIn(questIds).stream()
                        .collect(Collectors.groupingBy(r -> r.getQuest().getId()));

        // 카운터 맵: "MONSTER_KILL:12" → 현재 수치
        Map<String, Integer> counterMap =
                userCounterDetailRepository.findByUserId(userId).stream()
                        .collect(Collectors.toMap(
                                c -> c.getCounterType().name() + ":" + c.getTargetId(),
                                UserCounterDetail::getCounterValue,
                                Integer::sum
                        ));

        int totalDrawCount = counterMap.entrySet().stream()
                .filter(e -> e.getKey().startsWith("CHARACTER_DRAW:"))
                .mapToInt(Map.Entry::getValue)
                .sum();

        return filtered.stream().map(q -> {
            List<QuestConditionDto> conditionDtos = new ArrayList<>();
            boolean achieved = true;

            for (QuestCondition cond : conditionMap.getOrDefault(q.getId(), List.of())) {
                int current;
                String targetName;

                switch (cond.getCounterType()) {
                    case CHARACTER_DRAW -> {
                        // 🔥 target_id를 무시하고 null 키로 접근
                        current = totalDrawCount;
                        targetName = null;
                    }
                    case MONSTER_KILL, ITEM_CRAFT -> {
                        current = counterMap.getOrDefault(cond.getCounterType().name() + ":" + cond.getTargetId(), 0);
                        targetName = itemRepository.findById(cond.getTargetId()).map(Item::getName).orElse("???");
                        if (cond.getCounterType() == CounterType.MONSTER_KILL)
                            targetName = monsterRepository.findById(cond.getTargetId()).map(Monster::getName).orElse("???");
                    }
                    case LIFE_ACTION -> {
                        current = counterMap.getOrDefault("LIFE_ACTION:" + cond.getTargetId(), 0);
                        targetName = switch (cond.getTargetId().intValue()) {
                            case 1 -> "벌목"; case 2 -> "낚시"; case 3 -> "채광"; case 4 -> "채집";
                            default -> "알 수 없음";
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
                        int owned = Optional.ofNullable(userInventoryRepository.getQuantity(userId, cond.getTargetId()))
                                .orElse(0);
                        current = owned;
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

            List<QuestRewardDto> rewardDtos = rewardMap.getOrDefault(q.getId(), List.of()).stream()
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
                    .id(q.getId())
                    .name(q.getName())
                    .description(q.getDescription())
                    .type(q.getType())
                    .difficulty(q.getGrade())
                    .conditions(conditionDtos)
                    .rewards(rewardDtos)
                    .achieved(achieved)
                    .build();
        }).limit(limit).toList();
    }


    @Transactional
    public ResponseEntity<GamJaResponse> completeQuest(HttpServletRequest request, Map<String, Object> payload) {
        Long userId = (Long) request.getAttribute("userId");
        Long questId = ((Number) payload.get("questId")).longValue();
        // 0. 퀘스트 및 조건 조회
        Quest quest = questRepository.findById(questId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 퀘스트입니다."));
        List<QuestCondition> conditions = questConditionRepository.findByQuestId(questId);

        // 1. 납품 퀘스트일 경우, 아이템 수량 차감
        for (QuestCondition cond : conditions) {
            if (cond.getCounterType() == CounterType.DELIVER_ITEM) {
                int owned = Optional.ofNullable(userInventoryRepository.getQuantity(userId, cond.getTargetId()))
                        .orElse(0);
                if (owned < cond.getRequiredCount()) {
                    throw new IllegalStateException("납품에 필요한 아이템이 부족합니다.");
                }

                int result = userInventoryRepository.consumeItem(userId, cond.getTargetId(), cond.getRequiredCount());
                if (result == 0) {
                    throw new IllegalStateException("아이템 차감 실패: 수량 부족");
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
                    actionService.updateCharacterExp(userId, dexId, reward.getAmount());
                }
            }
        }


        // 3. 완료 기록
        UserQuestId uqId = new UserQuestId(userId, questId);
        UserQuest userQuest = userQuestRepository.findById(uqId).orElse(null);

        if (userQuest == null) {
            userQuest = UserQuest.builder()
                    .id(uqId)
                    .quest(quest)
                    .build();
        }

        userQuest.setCompleted(true);
        userQuest.setCompletedAt(LocalDateTime.now());
        userQuest.setUpdatedAt(LocalDateTime.now());

        userQuestRepository.save(userQuest);
        logService.recordCounter(userId, CounterType.QUEST_COMPLETE, 0L);
        return ResponseEntity.ok(GamJaResponse.success("보상 처리 완료", null));
    }
}
