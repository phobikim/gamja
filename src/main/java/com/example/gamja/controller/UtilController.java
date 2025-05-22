package com.example.gamja.controller;

import com.example.gamja.dto.*;
import com.example.gamja.entity.*;
import com.example.gamja.message.GamJaResponse;
import com.example.gamja.repository.*;
import com.example.gamja.util.CommonUtil;
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
    @GetMapping("/rank")
    public ResponseEntity<GamJaResponse> getCharInfo() {
//        List<UserInventory> inventories = userInventoryRepository.findAll();
//
//        List<RankDto> rankList = inventories.stream()
//                .map(inv -> {
//                    var user = inv.getUser();
//                    Optional<UserDtl> userDtlOpt = userDtlRepository.findByUser(user);
//                    if (userDtlOpt.isEmpty()) {
//                        return null;
//                    }
//                    UserDtl userDtl = userDtlOpt.get();
//                    String finalImage = commonUtil.resolveCharacterImage(userDtl);
//                    int total = inv.getFish() + inv.getWood() + inv.getStone() + inv.getFood();
//
//                    String nickname = Optional.ofNullable(userDtl.getUsernickname())
//                            .orElse(user.getUsername());
//
//                    return new RankDto(
//                            user.getId(),
//                            nickname,
//                            finalImage,
//                            total
//                    );
//                })
//                .filter(Objects::nonNull)
//                .sorted(Comparator.comparingInt(RankDto::getTotal).reversed())
//                .collect(Collectors.toList());


        return ResponseEntity.ok(GamJaResponse.success("정상 조회", null));
    }

    @ResponseBody
    @PostMapping("/quest/complete")
    public ResponseEntity<GamJaResponse> completeQuest(@RequestParam Long id, HttpSession session) {


        return ResponseEntity.ok(GamJaResponse.ok("정상 조회"));
    }

    @ResponseBody
    @GetMapping("/item/list/{userId}")
    public ResponseEntity<GamJaResponse> getItemList (@PathVariable Long userId, HttpSession session) {
        Long sessionUserId = (Long) session.getAttribute("userId");

        if (sessionUserId == null || !sessionUserId.equals(userId)) {
            return ResponseEntity.status(403).body(GamJaResponse.fail("로그인이 필요합니다."));
        }
        List<UserInventory> inventoryList = userInventoryRepository.findByUserId(userId);

        List<UserInventoryDto> responseList = inventoryList.stream().map(inv -> {
            Item item = inv.getItem(); // JPA 연관 객체
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
                dto.setStationIds(item.getStationIds());
                dto.setIconPath(item.getIconPath());
            }

            return dto;
        }).toList();


        return ResponseEntity.ok(GamJaResponse.success("정상 조회", responseList));
    }
}
