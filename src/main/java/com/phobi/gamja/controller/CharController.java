package com.phobi.gamja.controller;

import com.phobi.gamja.dto.user.UserInventoryDto;
import com.phobi.gamja.message.GamJaResponse;
import com.phobi.gamja.service.CharService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/char")
public class CharController {

    private final CharService charService;

    /*
     * desc : 캐릭터 정보 > 대표 캐릭터 정보 조회
     * */
    @GetMapping("")
    public ResponseEntity<GamJaResponse> getCharInfo(HttpServletRequest request) {
        return ResponseEntity.ok(charService.getUserInfo(request));
    }

    @GetMapping("/{username}")
    public ResponseEntity<GamJaResponse> getCharInfoByUsername(@PathVariable String username) {
        return ResponseEntity.ok(charService.getUserInfoByUsername(username));
    }
    /*
     * desc : 캐릭터 정보 > 전투 stat 조회
     * */
    @GetMapping("/battle")
    public ResponseEntity<GamJaResponse> getBattleInfo(HttpServletRequest request) {
        return ResponseEntity.ok(charService.getBattleInfo(request));
    }

    @GetMapping("/{username}/battle")
    public ResponseEntity<GamJaResponse> getBattleInfoByUsername(@PathVariable String username) {
        return ResponseEntity.ok(charService.getBattleInfoByUsername(username));
    }

    /*
     * desc : 캐릭터 정보 > 생활 stat 조회
     * */
    @GetMapping("/life")
    public ResponseEntity<GamJaResponse> getLifeInfo(HttpServletRequest request) {
        return ResponseEntity.ok(charService.getLifeInfo(request));
    }

    @PostMapping("/get-equip-items")
    public ResponseEntity<GamJaResponse> getEquipItems(HttpServletRequest request, @RequestBody Map<String, String> payload) {
        return ResponseEntity.ok(charService.getEquipItems(request, payload));
    }

    @PostMapping("/set-equip-items")
    public ResponseEntity<GamJaResponse> setEquipItems(HttpServletRequest request, @RequestBody Map<String, String> payload) {
        charService.setEquipItems(request, payload);
        return ResponseEntity.ok(GamJaResponse.success("장착 완료", null));
    }
    /*
    * desc : 보유한 캐릭터 조회
    * */
    @GetMapping("/owned")
    public ResponseEntity<GamJaResponse> getOwnedDex(HttpServletRequest request) {
        return ResponseEntity.ok(charService.getOwnedDex(request));
    }

    /*
    * desc : 대표 캐릭터로 지정
    * */
    @PostMapping("/setDex")
    public ResponseEntity<GamJaResponse> setCharacterImage(@RequestBody Map<String, Long> payload, HttpServletRequest request) throws InterruptedException {
        return ResponseEntity.ok(charService.setCharacterImage(request, payload));
    }

    /*
     * desc : 감자 뽑기
     * */
    @GetMapping("/gacha")
    public ResponseEntity<GamJaResponse> getGacha(HttpServletRequest request) {
        return ResponseEntity.ok(charService.getGacha(request));
    }

    @GetMapping("/gacha/multi")
    public ResponseEntity<GamJaResponse> getMultiGacha(HttpServletRequest request) {
        return ResponseEntity.ok(charService.getMultiGacha(request));
    }

    /*
     * desc : 보유 미감정 감자 수
     * */
    @GetMapping("/ticketCount")
    public ResponseEntity<GamJaResponse> ticketCount(HttpServletRequest request) {
        return ResponseEntity.ok(charService.ticketCount(request));
    }

    /*
    * 티어 정보 list
    * */
    @GetMapping("/tier/list")
    public ResponseEntity<GamJaResponse> tierList(HttpServletRequest request) {
        return ResponseEntity.ok(charService.tierList(request));
    }

}
