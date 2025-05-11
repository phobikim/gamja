package com.example.gamja.util;

import com.example.gamja.entity.Dex;
import com.example.gamja.entity.UserDtl;
import com.example.gamja.repository.DexRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommonUtil {

    private final DexRepository dexRepository;

    public String resolveCharacterImage(UserDtl userDtl) {
        if (userDtl.getCharacterDexId() != null) {
            Dex dex = dexRepository.findById(userDtl.getCharacterDexId())
                    .orElse(null);
            if (dex != null) {
                return dex.getImage();
            }
        }
        return userDtl.getCharacterImage(); // 기본 이미지
    }
}
