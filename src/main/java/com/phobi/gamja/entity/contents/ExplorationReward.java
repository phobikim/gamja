package com.phobi.gamja.entity.contents;

import lombok.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExplorationReward implements Serializable {
    private int stage;
    private int totalExp;
    private List<Map<String, Object>> drops = new ArrayList<>();

    public void addDrop(Long itemId, String itemName, String iconPath, int count) {
        for (Map<String, Object> item : drops) {
            if (item.get("itemId").equals(itemId)) {
                int prevCount = (int) item.get("count");
                item.put("count", prevCount + count);
                return;
            }
        }
        Map<String, Object> drop = new HashMap<>();
        drop.put("itemId", itemId);
        drop.put("itemName", itemName);
        drop.put("iconPath", iconPath);
        drop.put("count", count);
        drops.add(drop);
    }
}