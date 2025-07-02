package com.phobi.gamja.service;

import com.phobi.gamja.entity.chronicle.Chronicle;
import com.phobi.gamja.message.GamJaResponse;
import com.phobi.gamja.repository.chronicle.ChronicleRepository;
import com.phobi.gamja.repository.item.ItemRepository;
import com.phobi.gamja.repository.quest.QuestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChronicleService {

    private final ChronicleRepository chronicleRepository;
    private final ItemRepository itemRepository;
    private final QuestRepository questRepository;

    public ResponseEntity<GamJaResponse> getChronicleList(Long mapId, HttpServletRequest request) {
        List<Chronicle> elements = chronicleRepository.findByMapIdAndUseFlagTrue(mapId);

        List<Map<String, Object>> result = new ArrayList<>();

        for (Chronicle element : elements) {
            Map<String, Object> data = new HashMap<>();
            data.put("id", element.getId());
            data.put("type", element.getTargetType().name());
            data.put("requiredCount", element.getRequiredCount());
            data.put("order", element.getOrderInUi());

            switch (element.getTargetType()) {
                case ITEM:
                case FOOD:
                    itemRepository.findById(element.getTargetId()).ifPresent(item -> {
                        data.put("name", item.getName());
                        data.put("icon", item.getIconPath());
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

        return ResponseEntity.ok(GamJaResponse.success("감자 연대기 조회 성공", result));
    }
}
