package com.phobi.gamja.controller;

import com.phobi.gamja.dto.item.ShopItemDto;
import com.phobi.gamja.dto.user.UserSellableItemDto;
import com.phobi.gamja.entity.contents.SkillShop;
import com.phobi.gamja.message.GamJaResponse;
import com.phobi.gamja.repository.contents.SkillShopRepository;
import com.phobi.gamja.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/shop")
public class ShopController {

    private final ShopService shopService;

    @GetMapping("/sell-list")
    public ResponseEntity<GamJaResponse> getSellItemList(HttpServletRequest request) {
        return shopService.getSellList(request);
    }

    @GetMapping("/inventory")
    public ResponseEntity<GamJaResponse> getUserInventory(HttpServletRequest request) {
        return shopService.getUserInventoryForSale(request);
    }

    @PostMapping("/buy")
    public ResponseEntity<GamJaResponse> buyItem(HttpServletRequest request,
                                                 @RequestBody Map<String, Long> payload) {
        return shopService.buyItem(request, payload);
    }

    @PostMapping("/sell")
    public ResponseEntity<GamJaResponse> sellItem(HttpServletRequest request,
                                                  @RequestBody Map<String, Long> payload) {
        return shopService.sellItem(request, payload);
    }



}
