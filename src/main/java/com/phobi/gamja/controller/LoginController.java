package com.phobi.gamja.controller;

import com.phobi.gamja.entity.contents.SkillType;
import com.phobi.gamja.entity.user.*;
import com.phobi.gamja.message.GamJaResponse;
import com.phobi.gamja.repository.user.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.util.*;
import javax.servlet.http.HttpSession;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api")
public class LoginController {
    private final UserRepository userRepository;
    private final UserDtlRepository userDtlRepository;
    private final UserSkillRepository userSkillRepository;
    private final UserDexRepository userDexRepository;
    private final UserStatRepository userStatRepository;


    @Transactional
    @ResponseBody
    @PostMapping("/login")
    public ResponseEntity<GamJaResponse> login(
            @RequestParam String username,
            @RequestParam String pin,
            HttpSession session,
            HttpServletRequest httpServletRequest) {

        Optional<User> userOpt = userRepository.findByUsernameAndPin(username, pin);
        if (userOpt.isPresent()) {
            User user = userOpt.get();

            //세션 저장
            session.setAttribute("userId", user.getId());
            session.setAttribute("username", user.getUsername());
            session.setAttribute("userAgent", httpServletRequest.getHeader("User-Agent"));

            Map<String, Object> body = new HashMap<>();

            body.put("userId", user.getId());
            body.put("username", user.getUsername());
            return ResponseEntity.ok(GamJaResponse.success("로그인 완료", body));
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
            HttpSession session,
            HttpServletRequest httpServletRequest) {
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
            userDtl.setCharacterDexId(100L);
            userDtlRepository.save(userDtl);

            // ✅ 기본 stat 생성
            UserStat userStat = new UserStat();
            userStat.setUser(savedUser);
            userStat.setUserPower(1);
            userStat.setUserHp(1);
            userStat.setUserSpeed(1);
            userStatRepository.save(userStat);


            // ✅ 활동/제작 스킬 초기화
            List<UserSkill> skillList = Arrays.stream(SkillType.values())
                    .map(skillType -> {
                        UserSkill skill = new UserSkill();
                        skill.setUserId(savedUser.getId());
                        skill.setSkillType(skillType);
                        skill.setLevel(1);
                        skill.setExp(0);
                        return skill;
                    })
                    .toList();
            userSkillRepository.saveAll(skillList);

            // ✅ 기본 캐릭터 지급
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

            Map<String, Object> body = new HashMap<>();

            body.put("userId", savedUser.getId());
            body.put("username", savedUser.getUsername());
            //세션 저장
            session.setAttribute("userId", savedUser.getId());
            session.setAttribute("username", savedUser.getUsername());
            session.setAttribute("userAgent", httpServletRequest.getHeader("User-Agent"));
            return ResponseEntity.ok(GamJaResponse.success("회원가입 완료", body));
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
