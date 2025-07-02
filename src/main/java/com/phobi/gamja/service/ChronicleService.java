package com.phobi.gamja.service;

import com.phobi.gamja.entity.chronicle.Chronicle;
import com.phobi.gamja.entity.user.CounterType;
import com.phobi.gamja.entity.user.UserChronicle;
import com.phobi.gamja.entity.user.UserCounterDetail;
import com.phobi.gamja.message.GamJaResponse;
import com.phobi.gamja.repository.battle.MonsterRepository;
import com.phobi.gamja.repository.chronicle.ChronicleRepository;
import com.phobi.gamja.repository.item.ItemRepository;
import com.phobi.gamja.repository.quest.QuestRepository;
import com.phobi.gamja.repository.user.UserChronicleRepository;
import com.phobi.gamja.repository.user.UserCounterDetailRepository;
import com.phobi.gamja.util.CommonUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChronicleService {

    private final ChronicleRepository chronicleRepository;
    private final UserChronicleRepository userChronicleRepository;
    private final ItemRepository itemRepository;
    private final QuestRepository questRepository;
    private final MonsterRepository monsterRepository;
    private final UserCounterDetailRepository userCounterDetailRepository;

    private final CommonUtil commonUtil;

    public ResponseEntity<GamJaResponse> getChronicleList(Long mapId, HttpSession session) {
        Long userId = commonUtil.getUserId(session);
        List<Chronicle> elements = chronicleRepository.findByMapIdAndUseFlagTrue(mapId);
        // 연대기 요소가 없다면 바로 null 응답
        if (elements == null || elements.isEmpty()) {
            return ResponseEntity.ok(GamJaResponse.success("연대기 항목이 없습니다.", null));
        }
        // 메인 상세 리스트
        List<Map<String, Object>> list = buildChronicleDetailList(userId, elements);

        // 요약 퍼센트 계산
        Map<String, Object> summary = calculateChronicleProgressGrouped(userId, mapId);

        Map<String, Object> result = Map.of(
                "list", list,
                "summary", summary
        );

        return ResponseEntity.ok(GamJaResponse.success("감자 연대기 조회 성공", result));
    }

    private List<Map<String, Object>> buildChronicleDetailList(Long userId, List<Chronicle> elements) {
        Map<Long, UserChronicle> userMap = userChronicleRepository
                .findByUserIdAndChronicleIdIn(userId, elements.stream().map(Chronicle::getId).toList())
                .stream()
                .collect(Collectors.toMap(uc -> uc.getChronicle().getId(), uc -> uc));

        List<Map<String, Object>> result = new ArrayList<>();

        for (Chronicle element : elements) {
            Map<String, Object> data = new HashMap<>();
            data.put("id", element.getId());
            data.put("type", element.getTargetType().name());
            data.put("requiredCount", element.getRequiredCount());
            data.put("order", element.getOrderInUi());

            int progress = 0;
            boolean completed = false;

            // 타입별 처리
            switch (element.getTargetType()) {
                case ITEM:
                case FOOD:
                    itemRepository.findById(element.getTargetId()).ifPresent(item -> {
                        data.put("name", item.getName());
                        data.put("icon", item.getIconPath());
                        data.put("desc", item.getDescription());
                    });

                    UserChronicle uc1 = userMap.get(element.getId());
                    progress = uc1 != null ? uc1.getProgressCount() : 0;
                    completed = uc1 != null && uc1.isCompleted();
                    break;

                case QUEST:
                    questRepository.findById(element.getTargetId()).ifPresent(quest -> {
                        data.put("name", quest.getName());
                        data.put("desc", quest.getDescription());
                    });

                    UserChronicle uc2 = userMap.get(element.getId());
                    progress = uc2 != null ? uc2.getProgressCount() : 0;
                    completed = uc2 != null && uc2.isCompleted();
                    break;

                case MONSTER:
                    monsterRepository.findById(element.getTargetId()).ifPresent(monster -> {
                        data.put("name", monster.getName());
                        data.put("icon", monster.getImagePath());
                        data.put("desc", monster.getDesc());

                        Optional<UserCounterDetail> counter = userCounterDetailRepository
                                .findByUserIdAndCounterTypeAndTargetId(userId, CounterType.MONSTER_KILL, monster.getId());

                        int monsterProgress = counter.map(UserCounterDetail::getCounterValue).orElse(0);
                        data.put("progressCount", monsterProgress);
                        data.put("completed", monsterProgress >= element.getRequiredCount());
                        data.put("percent", Math.min(monsterProgress, element.getRequiredCount()) * 100.0 / element.getRequiredCount());
                    });
                    result.add(data);
                    continue;
            }

            // 공통 계산 처리
            data.put("progressCount", progress);
            data.put("completed", completed);
            data.put("percent", Math.min(progress, element.getRequiredCount()) * 100.0 / element.getRequiredCount());

            result.add(data);
        }

        return result;
    }
    public Map<String, Object> calculateChronicleProgressGrouped(Long userId, Long mapId) {
        List<Chronicle> all = chronicleRepository.findByMapIdAndUseFlagTrue(mapId);

        // 유저 진행도 불러오기 (한 번에)
        Map<Long, UserChronicle> userMap = userChronicleRepository
                .findByUserIdAndChronicleIdIn(userId, all.stream().map(Chronicle::getId).toList())
                .stream()
                .collect(Collectors.toMap(
                        uc -> uc.getChronicle().getId(),
                        uc -> uc
                ));
        // MONSTER_KILL 전체 미리 조회
        Map<Long, Integer> monsterKillMap = userCounterDetailRepository
                .findAllByUserIdAndCounterType(userId, CounterType.MONSTER_KILL)
                .stream()
                .collect(Collectors.toMap(
                        UserCounterDetail::getTargetId,
                        UserCounterDetail::getCounterValue
                ));
        // 가중치 설정 (비율: 몬스터 25, 수집품 25, 퀘스트 25, 요리 25)
        Map<String, Double> weightMap = Map.of(
                "MONSTER", 0.25,
                "ITEM", 0.25,
                "QUEST", 0.25,
                "FOOD", 0.25
        );

        double totalWeightedProgress = 0;
        List<Map<String, Object>> typeList = new ArrayList<>();

        for (Chronicle.ChronicleTargetType type : Chronicle.ChronicleTargetType.values()) {
            List<Chronicle> group = all.stream()
                    .filter(c -> c.getTargetType() == type)
                    .toList();

            int requiredTotal = group.stream()
                    .mapToInt(Chronicle::getRequiredCount)
                    .sum();

            int userTotal = group.stream()
                    .mapToInt(c -> {
                        if (type == Chronicle.ChronicleTargetType.MONSTER) {
                            int count = monsterKillMap.getOrDefault(c.getTargetId(), 0);
                            return Math.min(count, c.getRequiredCount());
                        } else {
                            UserChronicle uc = userMap.get(c.getId());
                            return Math.min(
                                    uc != null ? uc.getProgressCount() : 0,
                                    c.getRequiredCount()
                            );
                        }
                    })
                    .sum();

            double percent = requiredTotal == 0 ? 0.0 : (userTotal * 100.0 / requiredTotal);
            double weighted = percent * weightMap.get(type.name());

            totalWeightedProgress += weighted;

            typeList.add(Map.of(
                    "type", type.name(),
                    "requiredCount", requiredTotal,
                    "userProgress", userTotal,
                    "percent", percent
            ));
        }

        return Map.of(
                "totalPercent", Math.round(totalWeightedProgress * 10) / 10.0,
                "details", typeList
        );
    }




}
