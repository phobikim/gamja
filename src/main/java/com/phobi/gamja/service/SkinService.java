package com.phobi.gamja.service;

import com.phobi.gamja.entity.skin.BackgroundImage;
import com.phobi.gamja.entity.skin.SkinBorder;
import com.phobi.gamja.entity.user.UserDtl;
import com.phobi.gamja.entity.user.UserSkin;
import com.phobi.gamja.message.GamJaResponse;
import com.phobi.gamja.repository.skin.BackgroundImageRepository;
import com.phobi.gamja.repository.skin.SkinBorderRepository;
import com.phobi.gamja.repository.user.UserDtlRepository;
import com.phobi.gamja.repository.user.UserSkinRepository;
import com.phobi.gamja.util.CommonUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SkinService {

    private final CommonUtil commonUtil;
    private final BackgroundImageRepository backgroundImageRepository;
    private final SkinBorderRepository skinBorderRepository;
    private final UserSkinRepository userSkinRepository;
    private final UserDtlRepository userDtlRepository;

    @Transactional(readOnly = true)
    public GamJaResponse getBackgroundList(HttpSession session) {
        Long userId = commonUtil.getUserId(session);
        // 1) 전체 사용 가능 배경
        List<BackgroundImage> all = backgroundImageRepository.findByEnabledTrue();
        // 2) 유저가 보유한 배경 (user_skin에서 skin_background_id가 있는 행만)
        List<UserSkin> ownedList = userSkinRepository.findByUserIdAndSkinBackgroundIsNotNull(userId);
        Set<Long> ownedIds = ownedList.stream()
                .map(us -> us.getSkinBackground().getId())
                .collect(Collectors.toSet());

        List<Map<String, Object>> result = all.stream()
                .map(bg -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", bg.getId());
                    map.put("name", bg.getName());
                    map.put("imageUrl", bg.getImageUrl());
                    map.put("owned", ownedIds.contains(bg.getId()));
                    return map;
                })
                .toList();

        return GamJaResponse.success("배경 이미지 목록", result);
    }

    @Transactional
    public GamJaResponse setBackgroundList(Map<String, Long> payload, HttpSession session) {
        Long userId = commonUtil.getUserId(session);
        Long backgroundId = payload.get("backgroundId"); // 프론트에서 보내는 키 확인

        if (backgroundId == null) {
            return GamJaResponse.fail("backgroundId가 필요합니다.");
        }

        // 1) 배경 존재/활성 확인
        BackgroundImage bg = backgroundImageRepository.findById(backgroundId)
                .filter(BackgroundImage::isEnabled)
                .orElse(null);
        if (bg == null) {
            return GamJaResponse.fail("해당 배경이 존재하지 않거나 비활성화 상태입니다.");
        }

        // 2) 소유 여부 확인 (user_skin)
        boolean owned = userSkinRepository.findByUserIdAndSkinBackgroundIsNotNull(userId).stream()
                .anyMatch(us -> us.getSkinBackground().getId().equals(backgroundId));
        if (!owned) {
            return GamJaResponse.fail("해당 배경을 보유하지 않았습니다.");
        }

        // 3) user_dtl 업데이트
        UserDtl userDtl = userDtlRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("USER_DTL을 찾을 수 없습니다."));
        userDtl.setBackgroundImage(bg);
        // JPA dirty checking으로 저장

        Map<String, Object> result = Map.of(
                "backgroundId", bg.getId(),
                "imageUrl", bg.getImageUrl()
        );
        return GamJaResponse.success("배경이 적용되었습니다.", result);
    }

    @Transactional(readOnly = true)
    public GamJaResponse getBorderList(HttpSession session) {
        Long userId = commonUtil.getUserId(session);

        List<SkinBorder> all = skinBorderRepository.findByEnabledTrue();

        List<UserSkin> ownedList = userSkinRepository.findByUserIdAndSkinBorderIsNotNull(userId);
        Set<Long> ownedIds = ownedList.stream()
                .map(us -> us.getSkinBorder().getId())
                .collect(Collectors.toSet());

        List<Map<String, Object>> result = all.stream().map(b -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", b.getId());
            m.put("name", b.getName());
            m.put("imageUrl", b.getImageUrl());
            m.put("owned", ownedIds.contains(b.getId()));
            return m;
        }).toList();

        return GamJaResponse.success("테두리 스킨 목록", result);
    }


    @Transactional
    public GamJaResponse setBorder(Map<String, Long> payload, HttpSession session) {
        Long userId = commonUtil.getUserId(session);
        Long borderId = payload.get("borderId");
        UserDtl userDtl = userDtlRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("USER_DTL을 찾을 수 없습니다."));

        if (borderId == null) {
            userDtl.setBorderSkin(null);

            Map<String, Object> result = new HashMap<>();
            result.put("borderId", null);

            return GamJaResponse.success("테두리가 제거되었습니다.", result);
        }

        SkinBorder border = skinBorderRepository.findById(borderId)
                .filter(SkinBorder::isEnabled)
                .orElse(null);
        if (border == null) return GamJaResponse.fail("해당 테두리가 존재하지 않거나 비활성화 상태입니다.");

        boolean owned = userSkinRepository.findByUserIdAndSkinBorderIsNotNull(userId).stream()
                .anyMatch(us -> Objects.equals(us.getSkinBorder().getId(), borderId));
        if (!owned) return GamJaResponse.fail("해당 테두리를 보유하지 않았습니다.");

        userDtl.setBorderSkin(border);
        return GamJaResponse.success(
                "테두리가 적용되었습니다.",
                Map.of("borderId", border.getId(), "imageUrl", border.getImageUrl())
        );
    }
}
