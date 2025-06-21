package com.phobi.gamja.dto.user;

import com.phobi.gamja.entity.dex.DexAttribute;
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
    private String titleIconPath;

    private int maxExp;
    private String dexName;

    private String attribute;
    private String attributeIconPath;

    private int maxCombo;
    private Long dexId;
    private int affinity;

    /* 배경 */
    private String backgroundImageName;
    private String backgroundImageUrl;

    /* 감자단 랭크 */
    private String corpsTierName;
    private String corpsTierIcon;
    private int corpsTierExp;
    private int corpsTierMaxExp;


    //메인 생성자
    public UserCharInfoDto(String username, UserDtl userDtl, UserDexStat stat, int maxCombo,
                           String equippedTitleName, String equippedTitleIcon,
                           String backgroundImageUrl, String backgroundImageName,
                           String corpsTierName, String corpsTierIcon, int corpsTierExp, int corpsTierMaxExp) {

        // 기존 데이터 세팅
        this.username = username;
        this.dexId = stat.getDex().getId();
        this.characterImage = userDtl.getCharacterImage();
        this.level = stat.getLevel();
        this.xp = stat.getXp();
        this.maxExp = stat.getMaxExp();
        this.dexName = stat.getDex().getName();
        this.title = equippedTitleName;
        this.titleIconPath = equippedTitleIcon;
        this.affinity = stat.getAffinity();
        this.maxCombo = maxCombo;

        DexAttribute attr = stat.getDex().getAttribute();
        this.attribute = attr != null ? attr.getName() : null;
        this.attributeIconPath = attr != null ? attr.getIconPath() : null;

        // 배경 정보
        this.backgroundImageUrl = backgroundImageUrl;
        this.backgroundImageName = backgroundImageName;

        // 감자단 정보
        this.corpsTierName = corpsTierName;
        this.corpsTierIcon = corpsTierIcon;
        this.corpsTierExp = corpsTierExp;
        this.corpsTierMaxExp = corpsTierMaxExp;
    }

}
