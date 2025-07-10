package com.phobi.gamja.controller;

import com.phobi.gamja.dto.dex.DexGrowthRequest;
import com.phobi.gamja.message.GamJaResponse;
import com.phobi.gamja.service.DexGrowthService;
import com.phobi.gamja.service.DexService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dex/growth")
public class DexGrowthController {

    private final DexGrowthService dexGrowthService;

    @GetMapping("/item-list")
    public ResponseEntity<GamJaResponse> getGrowthItemList(HttpSession request) {
        return dexGrowthService.getGrowthItemList(request);
    }
    @PostMapping("/execute")
    public ResponseEntity<GamJaResponse> executeGrowth(
            HttpServletRequest request,
            @RequestBody DexGrowthRequest dto
    ) {
        Long userId = (Long) request.getSession().getAttribute("userId");
        return dexGrowthService.executeGrowth(userId, dto);
    }
}
