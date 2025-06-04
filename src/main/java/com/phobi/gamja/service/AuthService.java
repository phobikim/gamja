package com.phobi.gamja.service;

import com.phobi.gamja.entity.contents.Dex;
import com.phobi.gamja.entity.contents.SkillType;
import com.phobi.gamja.entity.user.*;
import com.phobi.gamja.message.GamJaResponse;
import com.phobi.gamja.repository.user.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final UserDtlRepository userDtlRepository;
    private final UserSkillRepository userSkillRepository;
    private final UserDexRepository userDexRepository;
    private final UserDexStatRepository userDexStatRepository;

    @Transactional
    public GamJaResponse login(String username, String pin, HttpServletRequest request, HttpSession session) {
        Optional<User> userOpt = userRepository.findByUsernameAndPin(username, pin);
        if (userOpt.isEmpty()) {
            return GamJaResponse.fail("이름 또는 PIN이 일치하지 않습니다.");
        }
        User user = userOpt.get();
        session.setAttribute("userId", user.getId());
        session.setAttribute("username", user.getUsername());
        session.setAttribute("userAgent", request.getHeader("User-Agent"));

        Map<String, Object> body = new HashMap<>();
        body.put("username", user.getUsername());

        return GamJaResponse.success("로그인 완료", body);
    }
    @Transactional
    public GamJaResponse signup(String username, String pin, HttpServletRequest request, HttpSession session) {
        if (userRepository.findByUsername(username).isPresent()) {
            return GamJaResponse.fail("이미 존재하는 아이디입니다.");
        }

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPin(pin);
        User savedUser = userRepository.save(newUser);

        // 기본 정보 저장
        UserDtl userDtl = new UserDtl();
        userDtl.setUser(savedUser);
        userDtl.setCharacterImage("default.png");
        userDtl.setCharacterDexId(100L);
        userDtlRepository.save(userDtl);

        // 스킬 초기화
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

        // 기본 도감 지급
        List<Long> dexIds = List.of(100L, 101L);
        List<UserDex> userDexList = dexIds.stream()
                .map(dexId -> UserDex.builder()
                        .user(savedUser)
                        .dexId(dexId)
                        .acquiredAt(new Date())
                        .build())
                .toList();
        userDexRepository.saveAll(userDexList);

        // 기본 스탯 생성
        List<UserDexStat> dexStats = dexIds.stream()
                .map(dexId -> UserDexStat.builder()
                        .id(new UserDexStatId(savedUser.getId(), dexId))
                        .user(savedUser)
                        .dex(Dex.builder().id(dexId).build())
                        .level(1)
                        .xp(0)
                        .maxExp(100)
                        .power(1)
                        .hp(1)
                        .speed(1)
                        .build())
                .toList();
        userDexStatRepository.saveAll(dexStats);

        // 세션 저장
        session.setAttribute("userId", savedUser.getId());
        session.setAttribute("username", savedUser.getUsername());
        session.setAttribute("userAgent", request.getHeader("User-Agent"));

        Map<String, Object> body = new HashMap<>();
        body.put("userId", savedUser.getId());
        body.put("username", savedUser.getUsername());

        return GamJaResponse.success("회원가입 완료", body);
    }

    public GamJaResponse checkUsername(String username) {
        if (!username.matches("^[a-zA-Z가-힣0-9]{1,50}$")) {
            return GamJaResponse.fail("아이디는 한글 또는 영문만, 최대 50자까지 가능합니다.");
        }

        if (userRepository.findByUsername(username).isPresent()) {
            return GamJaResponse.fail("이미 존재하는 아이디입니다.");
        }

        return GamJaResponse.success("사용 가능한 아이디입니다.",null);
    }

    @Transactional
    public GamJaResponse getMyInfo(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return GamJaResponse.fail("인증 실패");
        }

        return GamJaResponse.success("세션이 유효합니다",null);
    }
}