package com.phobi.gamja.service;

import com.phobi.gamja.entity.chronicle.Chronicle;
import com.phobi.gamja.entity.user.UserChronicle;
import com.phobi.gamja.message.GamJaResponse;
import com.phobi.gamja.repository.chronicle.ChronicleRepository;
import com.phobi.gamja.repository.item.ItemRepository;
import com.phobi.gamja.repository.quest.QuestRepository;
import com.phobi.gamja.repository.user.UserChronicleRepository;
import com.phobi.gamja.util.CommonUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChronicleService {

    private final ChronicleRepository chronicleRepository;
    private final UserChronicleRepository userChronicleRepository;
    private final ItemRepository itemRepository;
    private final QuestRepository questRepository;

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

            // 유저 진행도
            UserChronicle uc = userMap.get(element.getId());
            int progress = uc != null ? uc.getProgressCount() : 0;
            boolean completed = uc != null && uc.isCompleted();
            double percent = (double) Math.min(progress, element.getRequiredCount()) / element.getRequiredCount() * 100;

            data.put("progressCount", progress);
            data.put("completed", completed);
            data.put("percent", percent);

            switch (element.getTargetType()) {
                case ITEM:
                case FOOD:
                    itemRepository.findById(element.getTargetId()).ifPresent(item -> {
                        data.put("name", item.getName());
                        data.put("icon", item.getIconPath());
                        data.put("desc", item.getDescription());
                    });
                    break;
                case QUEST:
                    questRepository.findById(element.getTargetId()).ifPresent(quest -> {
                        data.put("name", quest.getName());
                        data.put("desc", quest.getDescription());
                    });
                    break;
            }

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

        // 가중치 설정 (비율: 수집품 40, 퀘스트 20, 요리 40)
        Map<String, Double> weightMap = Map.of(
                "ITEM", 0.4,
                "QUEST", 0.2,
                "FOOD", 0.4
        );

        double totalWeightedProgress = 0;
        List<Map<String, Object>> typeList = new ArrayList<>();

        for (Chronicle.ChronicleTargetType type : Chronicle.ChronicleTargetType.values()) {
            // 각 타입 그룹 필터링
            List<Chronicle> group = all.stream()
                    .filter(c -> c.getTargetType() == type)
                    .toList();

            // 총 필요 수량과 유저 달성 수량 계산
            int requiredTotal = group.stream()
                    .mapToInt(Chronicle::getRequiredCount)
                    .sum();

            int userTotal = group.stream()
                    .mapToInt(c -> {
                        UserChronicle uc = userMap.get(c.getId());
                        return Math.min(
                                uc != null ? uc.getProgressCount() : 0,
                                c.getRequiredCount()
                        );
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
                "totalPercent", Math.round(totalWeightedProgress * 10) / 10.0, // 반올림
                "details", typeList
        );
    }




}
