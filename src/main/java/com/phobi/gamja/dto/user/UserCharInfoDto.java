package com.phobi.gamja.dto.user;

import com.phobi.gamja.entity.battle.StatBonus;
import com.phobi.gamja.entity.dex.DexAttribute;
import com.phobi.gamja.entity.user.UserCorps;
import com.phobi.gamja.entity.user.UserDexStat;
import com.phobi.gamja.entity.user.UserDtl;
import lombok.Data;

@Data
public class UserCharInfoDto {
    private String username;
    private String characterImage;
    private int level;
    private int xp;
    private Long gold;

    private String title;
    private String titleIconPath;

    private int maxExp;
    private String dexName;
    private String description;

    private String attribute;
    private String attributeIconPath;

    private int maxCombo;
    private Long dexId;
    private int affinity;

    /* 배경 */
    private String backgroundImageName;
    private String backgroundImageUrl;
    // 보더 스킨
    private String borderSkinName;
    private String borderSkinImageUrl;

    /* 감자단 랭크 */
    private String corpsTierName;
    private String corpsTierIcon;
    private int corpsTierExp;
    private int corpsTierMaxExp;
    private int corpsTierLevel;
    private Long corpsTierId;
    private int tierAtk;
    private int tierHp;


    //메인 생성자
    public UserCharInfoDto(String username, UserDtl userDtl, UserDexStat stat, int maxCombo,
                           String equippedTitleName, String equippedTitleIcon,
                           String backgroundImageUrl, String backgroundImageName,
                           String borderSkinImageUrl, String borderSkinName,
                           UserCorps userCorps, StatBonus statBonus) {

        // 기존 데이터 세팅
        this.username = username;
        this.dexId = stat.getDex().getId();
        this.characterImage = userDtl.getCharacterImage();
        this.level = stat.getLevel();
        this.xp = stat.getXp();
        this.maxExp = stat.getMaxExp();
        this.dexName = stat.getDex().getName();
        this.description = stat.getDex().getDescription();
        this.title = equippedTitleName;
        this.titleIconPath = equippedTitleIcon;
        this.affinity = stat.getAffinity();
        this.maxCombo = maxCombo;
        this.gold = userDtl.getGold();

        DexAttribute attr = stat.getDex().getAttribute();
        this.attribute = attr != null ? attr.getName() : null;
        this.attributeIconPath = attr != null ? attr.getIconPath() : null;

        // 배경 정보
        this.backgroundImageUrl = backgroundImageUrl;
        this.backgroundImageName = backgroundImageName;
        // 스킨 정보
        this.borderSkinImageUrl = borderSkinImageUrl;
        this.borderSkinName = borderSkinName;

        // 감자단 정보 from UserCorps
        this.corpsTierName = userCorps.getTier().getName();
        this.corpsTierIcon = userCorps.getTier().getIconPath();
        this.corpsTierLevel = userCorps.getCorpsLevel();
        this.corpsTierExp = userCorps.getCorpsXp();
        this.corpsTierMaxExp = userCorps.getCorpsMaxXp();
        this.corpsTierId = userCorps.getTier().getTierId();
        this.tierAtk = statBonus.power();
        this.tierHp = statBonus.hp();
    }

}

