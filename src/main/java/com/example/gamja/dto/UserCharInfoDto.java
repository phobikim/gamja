package com.example.gamja.dto;

import com.example.gamja.entity.UserDtl;
import com.example.gamja.entity.UserInventory;
import com.example.gamja.entity.UserSkill;
import lombok.Data;
import lombok.Getter;

@Data
public class UserCharInfoDto {
    private String username;
    private String nickname;
    private String characterImage;
    private int level;
    private int xp;
    private Inventory inventory;
    private Skill skill;
    private String title;

    @Getter
    public static class Inventory {

    }

    @Getter
    public static class Skill { // 🔥 추가
        private int fishLevel;
        private int woodLevel;
        private int stoneLevel;
        private int cookLevel;

        public Skill(UserSkill userSkill) {
            this.fishLevel = userSkill.getFishLevel();
            this.woodLevel = userSkill.getWoodLevel();
            this.stoneLevel = userSkill.getStoneLevel();
            this.cookLevel = userSkill.getCookLevel();
        }
    }


    public UserCharInfoDto(UserDtl userDtl, UserSkill userSkill) {
        this.username = userDtl.getUser().getUsername();
        this.nickname = userDtl.getUsernickname();
        this.characterImage = userDtl.getCharacterImage();
        this.level = userDtl.getLevel();
        this.xp = userDtl.getXp();
        this.skill = new Skill(userSkill); // 🔥 추가
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
