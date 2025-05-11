package com.example.gamja.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDexDto {
    private Long id;
    private Long userId;
    private Long dexId;
}