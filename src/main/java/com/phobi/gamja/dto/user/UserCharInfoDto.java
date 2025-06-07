package com.phobi.gamja.dto.user;

import com.phobi.gamja.entity.user.UserDexStat;
import com.phobi.gamja.entity.user.UserDtl;
import lombok.Data;

@Data
public class UserCharInfoDto {
    private String username;
    private String characterImage;
    private int level;
    private int xp;
    private String title;
    private int maxExp;
    private String dexName;
    private String attribute;
    private int maxCombo;
    private Long dexId;
    private int affinity;


    // 기존 생성자 유지
    public UserCharInfoDto(UserDtl userDtl, UserDexStat stat) {
        this(userDtl, stat, 0); // default maxCombo = 0
    }


    // 캐릭터 Dex 의 stat 반환
    public UserCharInfoDto(UserDtl userDtl, UserDexStat stat, int maxCombo) {
        this.dexId = stat.getDex().getId();
        this.username = userDtl.getUser().getUsername();
        this.characterImage = userDtl.getCharacterImage();
        this.level = stat.getLevel();
        this.xp = stat.getXp();
        this.maxExp = stat.getMaxExp();
        this.dexName = stat.getDex().getName();
        this.title = getTitleByLevel(stat.getLevel());
        this.attribute = stat.getDex().getAttribute();
        this.affinity = stat.getAffinity();
        this.maxCombo = maxCombo;
    }
    public String getTitleByLevel(int level) {
        if (level >= 1   && level <= 50)   return "씨앗 감자";           // 막 태어난
        if (level >= 51  && level <= 100)  return "흙속의 감자";         // 세상 구경 시작
        if (level >= 101 && level <= 150)  return "풋감자";              // 설익은 느낌
        if (level >= 151 && level <= 200)  return "삶은 감자";           // 이제 좀 쓸모있어짐
        if (level >= 201 && level <= 250)  return "버터 감자";           // 맛이 붙는 시기

        if (level >= 251 && level <= 300)  return "견습 감자단";         // 조직 입단
        if (level >= 301 && level <= 350)  return "정식 감자단";         // 레귤러 멤버
        if (level >= 351 && level <= 400)  return "우수 감자단";         // 성과 좋음
        if (level >= 401 && level <= 450)  return "고급 감자단";         // 인정받음
        if (level >= 451 && level <= 500)  return "마스터 감자단";       // 완전 숙련
        return "🥔 방황하는 감자"; // 1미만/예외처리
    }
}
