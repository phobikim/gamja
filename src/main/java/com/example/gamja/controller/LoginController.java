package com.example.gamja.controller;

import com.example.gamja.entity.*;
import com.example.gamja.message.GamJaResponse;
import com.example.gamja.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpSession;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api")
public class LoginController {
    private final UserRepository userRepository;
    private final UserDtlRepository userDtlRepository;
    private final UserInventoryRepository userInventoryRepository;
    private final UserSkillRepository userSkillRepository;
    private final UserDexRepository userDexRepository;


    @Transactional
    @ResponseBody
    @PostMapping("/login")
    public ResponseEntity<GamJaResponse> login(
            @RequestParam String username,
            @RequestParam String pin,
            HttpSession session ) {

        Optional<User> userOpt = userRepository.findByUsernameAndPin(username, pin);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            session.setAttribute("userId", user.getId()); // 세션에 사용자 ID 저장
            return ResponseEntity.ok(GamJaResponse.ok("로그인 완료"));
        } else {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(GamJaResponse.fail("이름 또는 PIN이 일치하지 않습니다."));
        }
    }

    @Transactional
    @ResponseBody
    @PostMapping("/signup")
    public ResponseEntity<GamJaResponse> signup(
            @RequestParam String username,
            @RequestParam String pin,
            HttpSession session ) {
        if (userRepository.findByUsername(username).isPresent()) {
            return ResponseEntity.ok(GamJaResponse.fail("이미 존재하는 아이디입니다."));
        } else {
            User newUser = new User();
            newUser.setUsername(username);
            newUser.setPin(pin);
            User savedUser = userRepository.save(newUser);

            UserDtl userDtl = new UserDtl();
            userDtl.setUser(savedUser); // FK 매핑
            userDtl.setCharacterImage("default.png");
            userDtl.setLevel(1);
            userDtl.setXp(0);
            userDtlRepository.save(userDtl);

            UserInventory userInventory = new UserInventory();
            userInventory.setUser(savedUser);
            userInventoryRepository.save(userInventory);

            UserSkill userSkill = new UserSkill();
            userSkill.setUser(savedUser);
            userSkillRepository.save(userSkill);

            List<Long> dexIds = List.of(100L, 101L);
            List<UserDex> toSave = dexIds.stream()
                    .map(dexId -> UserDex.builder()
                            .user(savedUser)
                            .dexId(dexId)
                            .acquiredAt(new Date())
                            .build())
                    .toList();
            userDexRepository.saveAll(toSave);

            session.setAttribute("userId", savedUser.getId());

            return ResponseEntity.ok(GamJaResponse.ok("회원가입 완료"));
        }
    }

    @ResponseBody
    @GetMapping("/check-username")
    public ResponseEntity<GamJaResponse> checkUsername(@RequestParam String username) {
        if (!username.matches("^[a-zA-Z가-힣0-9]{1,50}$")) {
            return ResponseEntity.ok(GamJaResponse.fail("아이디는 한글 또는 영문만, 최대 50자까지 가능합니다."));
        }

        if (userRepository.findByUsername(username).isPresent()) {
            return ResponseEntity.ok(GamJaResponse.fail("이미 존재하는 아이디입니다."));
        } else {
            return ResponseEntity.ok(GamJaResponse.ok("사용 가능한 아이디입니다. "));
        }
    }

}
