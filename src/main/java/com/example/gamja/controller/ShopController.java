package com.example.gamja.controller;

import com.example.gamja.entity.SkillShop;
import com.example.gamja.message.GamJaResponse;
import com.example.gamja.repository.SkillShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/shop")
public class ShopController {

    private final SkillShopRepository skillShopRepository;
    @ResponseBody
    @GetMapping("/skills")
    public ResponseEntity<GamJaResponse> getAllSkills() {

        List<SkillShop> skills = skillShopRepository.findAll();
        return ResponseEntity.ok(GamJaResponse.success("상점 목록 조회", skills));
    }
}
