package com.example.gamja.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDtlDto {
    private long   id;
    private String characterImage;
    private String username;
    private Long characterDexId;
    private String usernickname;
    private int level;
    private int xp;
}