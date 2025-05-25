package com.example.gamja.controller;

import com.example.gamja.dto.ActionDto;
import com.example.gamja.dto.DropTableEntryDto;
import com.example.gamja.dto.UserCharInfoDto;
import com.example.gamja.dto.UserDtlDto;
import com.example.gamja.entity.*;
import com.example.gamja.message.GamJaResponse;
import com.example.gamja.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/action")
@RequiredArgsConstructor
public class ActionController {

    private final ActionDropRepository actionDropRepository;
    private final ItemRepository itemRepository;
    private final UserInventoryRepository userInventoryRepository;
    private final UserSkillRepository userSkillRepository;
    private final ActionRepository actionRepository;
    private final UserDtlRepository userDtlRepository;

    /* action 테이블 조회 및 메타데이터 반환 */
    @GetMapping("/{activityType}/{userId}")
    public ResponseEntity<GamJaResponse> getActionsByCategory(
            @PathVariable String activityType,
            @PathVariable Long userId,
            HttpSession session) {

        Long sessionUserId = (Long) session.getAttribute("userId");
        if (sessionUserId == null || !sessionUserId.equals(userId)) {
            return ResponseEntity.status(403).body(GamJaResponse.fail("로그인이 필요합니다."));
        }

        try {
            SkillType actionCategory = SkillType.valueOf(activityType.toUpperCase());
            ActivityType type = ActivityType.valueOf(activityType.toUpperCase());
            // 사용자 스킬 정보 조회
            Optional<UserSkill> userSkillOpt = userSkillRepository.findByUserIdAndSkillType(userId, actionCategory);
            int level = userSkillOpt.map(UserSkill::getLevel).orElse(1);
            int exp = userSkillOpt.map(UserSkill::getExp).orElse(0);

            List<Action> actions = actionRepository.findByCategoryAndIsEnabledOrderByRankAsc(type, true);
            List<ActionDto> result = actions.stream()
                    .map(action -> ActionDto.of(action, level, exp))
                    .toList();
            return ResponseEntity.ok(GamJaResponse.success("정상 조회", result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build(); // 잘못된 카테고리
        }
    }


    /*action 별 drop item 조회*/
    @GetMapping("/{activityType}/{spotRank}/{userId}")
    public ResponseEntity<GamJaResponse> getDropTable(
            @PathVariable String activityType,
            @PathVariable int spotRank,
            @PathVariable Long userId,
            HttpSession session
    ) {
        Long sessionUserId = (Long) session.getAttribute("userId");

        if (sessionUserId == null || !sessionUserId.equals(userId)) {
            return ResponseEntity.status(403).body(GamJaResponse.fail("로그인이 필요합니다."));
        }

        ActivityType type = ActivityType.valueOf(activityType.toUpperCase());

        List<ActionDrop> drops = actionDropRepository.findByActivityTypeAndSpotRank(type, spotRank);

        List<DropTableEntryDto> result = drops.stream().map(drop -> {
            Item item = itemRepository.findById(drop.getItemId())
                    .orElseThrow(() -> new IllegalArgumentException("아이템 없음: " + drop.getItemId()));
            return DropTableEntryDto.of(drop, item);
        }).toList();

        return ResponseEntity.ok(GamJaResponse.success("정상 조회", result));
    }

    /*action 완료 후 skill lv,xp 조정 및 아이템 획득 처리 */
    @PostMapping("/addItems/{userId}")
    @Transactional
    public ResponseEntity<GamJaResponse> addItem(
            @RequestBody Map<String, Object> request,
            @PathVariable Long userId,
            HttpSession session) {
        Long sessionUserId = (Long) session.getAttribute("userId");

        if (sessionUserId == null || !sessionUserId.equals(userId)) {
            return ResponseEntity.status(403).body(GamJaResponse.fail("로그인이 필요합니다."));
        }

        // 활동 type 검증
        String activityTypeStr = (String) request.get("activityType");
        double exp = ((Number) request.getOrDefault("exp", 0)).doubleValue();

        SkillType skillType;
        try {
            skillType = SkillType.valueOf(activityTypeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(GamJaResponse.fail("유효하지 않은 활동 타입입니다."));
        }

        // 스킬 레벨업 처리
        UserSkillId skillId = new UserSkillId(userId, skillType);
        UserSkill userSkill = userSkillRepository.findById(skillId)
                .orElseGet(() -> {
                    UserSkill s = new UserSkill();
                    s.setUserId(userId);
                    s.setSkillType(skillType);
                    s.setLevel(1);
                    s.setExp(0);
                    return s;
                });

        int baseExp = userSkill.getExp();
        double totalExp = baseExp + exp;


        // 스킬 레벨업 (레벨업 공식: 100 + 10*레벨 등 자유 조절 가능)
        int level = userSkill.getLevel();
        while (totalExp >= getRequiredExp(level)) {
            totalExp -= getRequiredExp(level);
            level++;
        }

        userSkill.setLevel(level);
        userSkill.setExp((int) totalExp);
        userSkillRepository.save(userSkill);
        UserDtl userDtl = new UserDtl();
        // 🟠 추가 처리: ATTACK 타입이면 캐릭터 레벨도 증가
        if (skillType == SkillType.ATTACK) {
            userDtl = userDtlRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

            int charXp = userDtl.getXp() + (int) exp;
            int charLevel = userDtl.getLevel();

            while (charXp >= getRequiredExp(charLevel)) {
                charXp -= getRequiredExp(charLevel);
                charLevel++;
            }

            userDtl.setLevel(charLevel);
            userDtl.setXp(charXp);
            userDtlRepository.save(userDtl);
        }

        // 아이템 획득 처리
        List<Map<String, Object>> items = (List<Map<String, Object>>) request.get("items");

        for (Map<String, Object> itemMap : items) {
            Long itemId = ((Number) itemMap.get("itemId")).longValue();
            int count = ((Number) itemMap.get("count")).intValue();

            Optional<UserInventory> optional = userInventoryRepository.findByUserIdAndItemId(userId, itemId);
            if (optional.isPresent()) {
                UserInventory inventory = optional.get();
                inventory.setQuantity(inventory.getQuantity() + count);
                userInventoryRepository.save(inventory);
            } else {
                UserInventory newInventory = new UserInventory();
                newInventory.setUserId(userId);
                newInventory.setItemId(itemId);
                newInventory.setQuantity(count);
                userInventoryRepository.save(newInventory);
            }
        }

        UserCharInfoDto result = new UserCharInfoDto(userDtl);
        return ResponseEntity.ok(GamJaResponse.success("아이템 추가 완료", result));
    }

    // 필요 경험치 20씩 증가
    private int getRequiredExp(int level) {
        return 100 + (level - 1) * 20;
    }

}