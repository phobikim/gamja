package com.phobi.gamja.dto.user;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LifeStatDetailDto {
    private int fromUser=0;
    private int fromBase;
    private int fromEquip;

    public LifeStatDetailDto(int fromBase, int fromEquip) {
        this.fromUser = 0;
        this.fromBase = fromBase;
        this.fromEquip = fromEquip;
    }


    public int getTotal() {
        return fromUser + fromBase + fromEquip;
    }

}