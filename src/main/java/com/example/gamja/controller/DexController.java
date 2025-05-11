package com.example.gamja.controller;

import com.example.gamja.dto.DexDto;
import com.example.gamja.dto.UserCharInfoDto;
import com.example.gamja.entity.*;
import com.example.gamja.message.GamJaResponse;
import com.example.gamja.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.example.gamja.config.XpCofig.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dex")
public class DexController {
    private final DexRepository dexRepository;
    private final UserDexRepository userDexRepository;

    @ResponseBody
    @GetMapping("list")
    public ResponseEntity<GamJaResponse> getDexList(HttpSession session) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Long sessionUserId = (Long) session.getAttribute("userId");
        if (sessionUserId == null) {
            return ResponseEntity.status(403).body(GamJaResponse.fail("로그인이 필요합니다."));
        }
        // 1. 전체 도감 리스트 조회
//        List<Dex> dexList = dexRepository.findAll();
        List<Dex> dexList = dexRepository.findAllEnabledForUser();

        // 2. 유저가 가진 도감 ID 목록 조회
        List<UserDex> ownedDexList = userDexRepository.findByUserId(sessionUserId);

        List<DexDto> result = dexList.stream()
                .map(dex -> {
                    // ownedDexList 안에서 해당 dexId를 가진 항목 찾기
                    Optional<UserDex> matched = ownedDexList.stream()
                            .filter(ud -> ud.getDexId().equals(dex.getId()))
                            .findFirst();
                    boolean isOwned = matched.isPresent();
                    String formattedDate = isOwned ? dateFormat.format(matched.get().getAcquiredAt()) : null;

                    return DexDto.builder()
                            .id(dex.getId())
                            .name(dex.getName())
                            .description(dex.getDescription())
                            .image(dex.getImage())
                            .rank(dex.getRank())
                            .acquireCondition(dex.getAcquireCondition())
                            .acquiredCount(dex.getAcquiredCount())
                            .owned(isOwned)
                            .acquiredAt(formattedDate)
                            .build();
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(GamJaResponse.success("도감 조회 완료", result));

    }

}
