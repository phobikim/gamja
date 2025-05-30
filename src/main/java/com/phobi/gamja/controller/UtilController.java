package com.phobi.gamja.controller;

import com.phobi.gamja.dto.*;
import com.phobi.gamja.entity.*;
import com.phobi.gamja.entity.UserInventory;
import com.phobi.gamja.message.GamJaResponse;
import com.phobi.gamja.repository.*;
import com.phobi.gamja.util.CommonUtil;
import com.phobi.gamja.dto.UserInventoryDto;
import com.phobi.gamja.entity.Item;
import com.phobi.gamja.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/util")
public class UtilController {
    private final CommonUtil commonUtil;
    private final UserDtlRepository userDtlRepository;
    private final UserInventoryRepository userInventoryRepository;
    private final UserSkillRepository userSkillRepository;
    private final DexRepository dexRepository;
    private final UserDexRepository userDexRepository;
    private final DailyQuestRepository dailyQuestRepository;

    @ResponseBody
    @GetMapping("/item/list")
    public ResponseEntity<GamJaResponse> getItemList (HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return ResponseEntity.status(403).body(GamJaResponse.fail("로그인이 필요합니다."));
        }
        List<UserInventory> inventoryList = userInventoryRepository.findByUserId(userId);

        List<UserInventoryDto> responseList = inventoryList.stream().map(inv -> {
            Item item = inv.getItem();
            UserInventoryDto dto = new UserInventoryDto();

            dto.setItemId(inv.getItemId());
            dto.setQuantity(inv.getQuantity());
            dto.setUpdatedAt(inv.getUpdatedAt() != null ? inv.getUpdatedAt().toString() : null);

            if (item != null) {
                dto.setName(item.getName());
                dto.setDescription(item.getDescription());
                dto.setRank(item.getRank());
                dto.setRarity(item.getRarity().name());       // enum → String
                dto.setItemType(item.getItemType().name());   // enum → String
                dto.setEquipSlot(item.getEquipSlot().name()); // enum → String
                dto.setIconPath(item.getIconPath());
            }

            return dto;
        }).toList();


        return ResponseEntity.ok(GamJaResponse.success("정상 조회", responseList));
    }
}
