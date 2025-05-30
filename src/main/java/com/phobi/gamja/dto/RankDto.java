package com.phobi.gamja.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RankDto {
    private Long id;
    private String username;
    private String characterImage;

    private int total;
}
