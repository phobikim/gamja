package com.phobi.gamja.controller;

import com.phobi.gamja.message.GamJaResponse;
import com.phobi.gamja.repository.contents.DailyQuestRepository;
import com.phobi.gamja.repository.dex.DexRepository;
import com.phobi.gamja.repository.user.*;
import com.phobi.gamja.service.RankService;
import com.phobi.gamja.service.UtilService;
import com.phobi.gamja.util.CommonUtil;
import com.phobi.gamja.dto.user.UserInventoryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/util")
public class UtilController {
    private final CommonUtil commonUtil;
    private final UtilService itemService;
    private final RankService rankService;
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
        List<UserInventoryDto> responseList = itemService.getUserInventoryWithEquipStatus(userId);

        return ResponseEntity.ok(GamJaResponse.success("정상 조회", responseList));
    }

    @ResponseBody
    @PostMapping("/item/equip")
    public ResponseEntity<GamJaResponse> itemEquip (HttpSession session, @RequestBody Map<String, Long> body) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(403).body(GamJaResponse.fail("로그인이 필요합니다."));
        }
        Long itemId = body.get("itemId");
        try {
            List<UserInventoryDto> responseList = itemService.equipItem(userId, itemId);
            return ResponseEntity.ok(GamJaResponse.success("장착 완료", responseList));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(GamJaResponse.fail("장착 실패: " + e.getMessage()));
        }
    }

    @GetMapping("/rank")
    public ResponseEntity<GamJaResponse> rank (HttpServletRequest request) {
        return ResponseEntity.ok(rankService.getRank(request));
    }
}
