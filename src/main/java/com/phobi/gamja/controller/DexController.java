package com.phobi.gamja.controller;

import com.phobi.gamja.dto.DexDto;
import com.phobi.gamja.entity.*;
import com.phobi.gamja.entity.UserDex;
import com.phobi.gamja.message.GamJaResponse;
import com.phobi.gamja.repository.*;
import com.phobi.gamja.entity.Dex;
import com.phobi.gamja.repository.DexRepository;
import com.phobi.gamja.repository.UserDexRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

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
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return ResponseEntity.status(403).body(GamJaResponse.fail("로그인이 필요합니다."));
        }
        // 1. 전체 도감 리스트 조회
//        List<Dex> dexList = dexRepository.findAll();
        List<Dex> dexList = dexRepository.findAllEnabledForUser();

        // 2. 유저가 가진 도감 ID 목록 조회
        List<UserDex> ownedDexList = userDexRepository.findByUserId(userId);

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
                .sorted(Comparator
                        .comparing(DexDto::isOwned).reversed()
                        .thenComparing(DexDto::getId))
                .collect(Collectors.toList());

        return ResponseEntity.ok(GamJaResponse.success("도감 조회 완료", result));

    }

}
