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
    private int maxExp;
    private String dexName;

    private String attribute;
    private String attributeIconPath;

    private int maxCombo;
    private Long dexId;
    private int affinity;


    // 기본값(maxCombo = 0), 칭호 없음
    public UserCharInfoDto(UserDtl userDtl, UserDexStat stat) {
        this(userDtl, stat, 0, null);
    }

    // maxCombo만 지정, 칭호 없음
    public UserCharInfoDto(UserDtl userDtl, UserDexStat stat, int maxCombo) {
        this(userDtl, stat, maxCombo, null);
    }

    // 착용 칭호명까지 받는 메인 생성자
    public UserCharInfoDto(UserDtl userDtl, UserDexStat stat, int maxCombo, String equippedTitleName) {
        this.dexId = stat.getDex().getId();
        this.username = userDtl.getUser().getUsername();
        this.characterImage = userDtl.getCharacterImage();
        this.level = stat.getLevel();
        this.xp = stat.getXp();
        this.maxExp = stat.getMaxExp();
        this.dexName = stat.getDex().getName();
        this.title = equippedTitleName;
        this.affinity = stat.getAffinity();
        this.maxCombo = maxCombo;

        DexAttribute attr = stat.getDex().getAttribute();
        this.attribute = attr != null ? attr.getName() : null;
        this.attributeIconPath = attr != null ? attr.getIconPath() : null;
    }

}
