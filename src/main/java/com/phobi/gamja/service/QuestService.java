package com.phobi.gamja.service;

import com.phobi.gamja.dto.quest.*;
import com.phobi.gamja.entity.contents.Monster;
import com.phobi.gamja.entity.item.Item;
import com.phobi.gamja.entity.quest.*;
import com.phobi.gamja.entity.user.CounterType;
import com.phobi.gamja.entity.user.UserCounterDetail;
import com.phobi.gamja.message.GamJaResponse;
import com.phobi.gamja.repository.contents.DexRepository;
import com.phobi.gamja.repository.contents.MonsterRepository;
import com.phobi.gamja.repository.item.ItemRepository;
import com.phobi.gamja.repository.quest.*;
import com.phobi.gamja.repository.user.UserCounterDetailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestService {
    private final QuestRepository questRepository;
    private final QuestConditionRepository questConditionRepository;
    private final QuestRewardRepository questRewardRepository;
    private final UserCounterDetailRepository userCounterDetailRepository;

    private final MonsterRepository monsterRepository;
    private final ItemRepository itemRepository;
    private final DexRepository dexRepository;
    public ResponseEntity<GamJaResponse> getQuestList(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");

        List<Quest> quests = questRepository.findAll();
        List<Long> questIds = quests.stream().map(Quest::getId).toList();

        List<QuestCondition> allConditions = questConditionRepository.findByQuestIdIn(questIds);
        List<QuestReward> allRewards = questRewardRepository.findByQuestIdIn(questIds);
        List<UserCounterDetail> counters = userCounterDetailRepository.findByUserId(userId);

        Map<Long, List<QuestCondition>> conditionMap = allConditions.stream()
                .collect(Collectors.groupingBy(q -> q.getQuest().getId()));
        Map<Long, List<QuestReward>> rewardMap = allRewards.stream()
                .collect(Collectors.groupingBy(r -> r.getQuest().getId()));

        Map<String, Integer> counterMap = counters.stream()
                .collect(Collectors.toMap(
                        c -> c.getCounterType().name() + ":" + c.getTargetId(),
                        UserCounterDetail::getCounterValue,
                        Integer::sum
                ));

        int totalDrawCount = counters.stream()
                .filter(c -> c.getCounterType() == CounterType.CHARACTER_DRAW)
                .mapToInt(UserCounterDetail::getCounterValue)
                .sum();

        List<QuestDto> result = quests.stream().map(q -> {
            List<QuestCondition> conds = conditionMap.getOrDefault(q.getId(), List.of());
            List<QuestReward> rewards = rewardMap.getOrDefault(q.getId(), List.of());

            boolean achieved = true;
            List<QuestConditionDto> conditionDtos = new ArrayList<>();

            for (QuestCondition cond : conds) {
                int current = switch (cond.getCounterType()) {
                    case CHARACTER_DRAW -> totalDrawCount;
                    default -> counterMap.getOrDefault(cond.getCounterType().name() + ":" + cond.getTargetId(), 0);
                };

                boolean pass = current >= cond.getRequiredCount();
                achieved &= pass;

                String targetName = switch (cond.getCounterType()) {
                    case NONE, CHARACTER_DRAW -> null;
                    case MONSTER_KILL -> {
                        Monster m = monsterRepository.findById(cond.getTargetId()).orElse(null);
                        yield m != null ? m.getName() : "???";
                    }
                    case ITEM_CRAFT -> {
                        Item item = itemRepository.findById(cond.getTargetId()).orElse(null);
                        yield item != null ? item.getName() : "???";
                    }
                    case LIFE_ACTION -> {
                        yield switch (cond.getTargetId().intValue()) {
                            case 1 -> "벌목";
                            case 2 -> "낚시";
                            case 3 -> "채광";
                            case 4 -> "채집";
                            default -> "알 수 없음";
                        };
                    }
                };

                conditionDtos.add(QuestConditionDto.builder()
                        .counterType(cond.getCounterType())
                        .targetId(cond.getTargetId())
                        .targetName(targetName)
                        .requiredCount(cond.getRequiredCount())
                        .currentCount(current)
                        .achieved(pass)
                        .build());
            }

            List<QuestRewardDto> rewardDtos = rewards.stream().map(r -> {
                String itemName = null;
                if (r.getRewardType() == QuestReward.RewardType.ITEM && r.getItemId() != null) {
                    Item item = itemRepository.findById(r.getItemId()).orElse(null);
                    itemName = item != null ? item.getName() : "???";
                }

                return QuestRewardDto.builder()
                        .rewardType(r.getRewardType())
                        .itemId(r.getItemId())
                        .itemName(itemName) // 추가된 필드
                        .amount(r.getAmount())
                        .build();
            }).toList();

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
        }).toList();

        return ResponseEntity.ok(GamJaResponse.success("퀘스트 조회 성공", result));
    }
}
