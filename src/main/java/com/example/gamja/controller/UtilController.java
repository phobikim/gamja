package com.example.gamja.controller;

import com.example.gamja.dto.*;
import com.example.gamja.entity.*;
import com.example.gamja.message.GamJaResponse;
import com.example.gamja.repository.*;
import com.example.gamja.service.QuestService;
import com.example.gamja.util.CommonUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import com.example.gamja.dto.UserCharInfoDto;

import static com.example.gamja.config.XpCofig.XP_PER_LEVEL;

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
    private final UserQuestRepository userQuestRepository;
    QuestService questService;


    @ResponseBody
    @GetMapping("/rank")
    public ResponseEntity<GamJaResponse> getCharInfo() {
        List<UserInventory> inventories = userInventoryRepository.findAll();

        List<RankDto> rankList = inventories.stream()
                .map(inv -> {
                    var user = inv.getUser();
                    Optional<UserDtl> userDtlOpt = userDtlRepository.findByUser(user);
                    if (userDtlOpt.isEmpty()) {
                        return null;
                    }
                    UserDtl userDtl = userDtlOpt.get();
                    String finalImage = commonUtil.resolveCharacterImage(userDtl);
                    int total = inv.getFish() + inv.getWood() + inv.getStone() + inv.getFood();

                    String nickname = Optional.ofNullable(userDtl.getUsernickname())
                            .orElse(user.getUsername());

                    return new RankDto(
                            user.getId(),
                            nickname,
                            finalImage,
                            total
                    );
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingInt(RankDto::getTotal).reversed())
                .collect(Collectors.toList());


        return ResponseEntity.ok(GamJaResponse.success("정상 조회", rankList));
    }

    @ResponseBody
    @GetMapping("/quest/list")
    public ResponseEntity<GamJaResponse> getQuestList(HttpSession session) {
        Long sessionUserId = (Long) session.getAttribute("userId");
        if (sessionUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(GamJaResponse.error("로그인이 필요합니다"));
        }
        List<QuestListResponseDto> questList = questService.getTodayQuestList(sessionUserId);


        return ResponseEntity.ok(GamJaResponse.success("정상 조회", null));
    }

    @ResponseBody
    @PostMapping("/quest/complete")
    public ResponseEntity<GamJaResponse> completeQuest(@RequestParam Long id, HttpSession session) {


        return ResponseEntity.ok(GamJaResponse.ok("정상 조회"));
    }

}
