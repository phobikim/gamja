package com.phobi.gamja.dto.dex;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DexGrowthRequest {
    private Long dexId;
    private Long itemId;
    private int quantity;
}