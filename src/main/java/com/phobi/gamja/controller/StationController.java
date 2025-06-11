package com.phobi.gamja.controller;

import com.phobi.gamja.dto.item.ItemRecipeDto;
import com.phobi.gamja.message.CraftRequest;
import com.phobi.gamja.message.GamJaResponse;
import com.phobi.gamja.service.StationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/station")
public class StationController {

    private final StationService stationService;

    @GetMapping("/list")
    public ResponseEntity<GamJaResponse> getStationList(HttpSession session) {
        return ResponseEntity.ok(
                GamJaResponse.success("정상 조회", stationService.getStationList())
        );
    }

    @PostMapping("/{stationCategory}/recipe")
    public ResponseEntity<GamJaResponse> getRecipeList(
            @PathVariable String stationCategory,
            HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        return ResponseEntity.ok(
                GamJaResponse.success("정상 조회", stationService.getRecipeList(userId, stationCategory))
        );
    }

    @PostMapping("/{stationCategory}/craft")
    public ResponseEntity<GamJaResponse> craftItem(
            @PathVariable String stationCategory,
            @RequestBody CraftRequest request,
            HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return ResponseEntity.status(403).body(GamJaResponse.fail("로그인이 필요합니다."));
        }

        try {
            List<ItemRecipeDto> updatedRecipes = stationService.craftItem(userId, stationCategory, request);
            return ResponseEntity.ok(GamJaResponse.success("제작 완료", updatedRecipes));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(GamJaResponse.fail(e.getMessage()));
        }
    }
}
