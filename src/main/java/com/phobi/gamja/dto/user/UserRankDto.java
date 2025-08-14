package com.phobi.gamja.dto.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserRankDto {

    @JsonIgnore
    private Long id;

    private String username;
    private String characterImage;
    private Long tierId;
    private int corpsLevel;
    private String corpsTierName;
    private String corpsTierIconPath;
    private String titleName;
    private String titleIconPath;
    private String borderSkinName;
    private String borderSkinImageUrl;
    private boolean mine = false;

    public UserRankDto(Long id, String username, String characterImage,
                       Long tierId, int corpsLevel,
                       String corpsTierName, String corpsTierIconPath,
                       String borderSkinImageUrl, String borderSkinName,
                       String titleName, String titleIconPath) {
        this.id = id;
        this.username = username;
        this.characterImage = characterImage;
        this.tierId = tierId;
        this.corpsLevel = corpsLevel;
        this.corpsTierName = corpsTierName;
        this.corpsTierIconPath = corpsTierIconPath;
        this.borderSkinImageUrl = borderSkinImageUrl;
        this.borderSkinName = borderSkinName;
        this.titleName = titleName;
        this.titleIconPath = titleIconPath;
    }

    public int getTotal() {
        return (int) ((tierId != null ? tierId : 0) * 100 + corpsLevel);
    }
}