package com.example.gamja.controller;

import com.example.gamja.dto.UserDtlDto;
import com.example.gamja.entity.User;
import com.example.gamja.entity.UserDtl;
import com.example.gamja.message.GamJaResponse;
import com.example.gamja.repository.UserDtlRepository;
import com.example.gamja.repository.UserRepository;
import com.example.gamja.util.CommonUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/main")
public class MainController {
    private final CommonUtil commonUtil;
    private final UserDtlRepository userDtlRepository;
    private final UserRepository userRepository;

    @ResponseBody
    @GetMapping("/list")
    public ResponseEntity<GamJaResponse> getAllUsers() {
        List<UserDtl> userDtls = userDtlRepository.findAll();

        List<UserDtlDto> userList = userDtls.stream()
                .map(userDtl -> {
                    String finalImage = commonUtil.resolveCharacterImage(userDtl);
                    return UserDtlDto.builder()
                            .id(userDtl.getId())
                            .characterImage(finalImage) // ✅ 계산된 이미지
                            .username(userDtl.getUser().getUsername())
                            .usernickname(userDtl.getUsernickname())
                            .characterDexId(userDtl.getCharacterDexId())
                            .level(userDtl.getLevel())
                            .xp(userDtl.getXp())
                            .build();
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(GamJaResponse.success("정상 조회", userList));
    }

    @PostMapping("/login")
    public ResponseEntity<GamJaResponse> login(@RequestBody Map<String, String> request, HttpSession session) {
        try {
            Long userId = Long.valueOf(request.get("userId"));
            String inputPin = request.get("pin");

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

            if (!user.getPin().equals(inputPin)) {
                return ResponseEntity.ok(GamJaResponse.fail("잘못된 PIN입니다."));
            }

            // ✅ 세션 저장
            session.setAttribute("userId", user.getId());
            session.setAttribute("username", user.getUsername());

            return ResponseEntity.ok(GamJaResponse.ok("로그인 성공"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(GamJaResponse.fail("로그인 처리 중 오류가 발생했습니다."));
        }
    }

}
