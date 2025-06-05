package com.phobi.gamja.dto.contents;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DexOwnedListResponseDto {
    private int totalDexCount;       // 전체 도감 수
    private int ownedDexCount;       // 내가 보유한 도감 수
    private List<DexOwnedDto> ownedDexList;  // 보유 감자 리스트
}