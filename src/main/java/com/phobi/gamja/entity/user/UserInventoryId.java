package com.phobi.gamja.entity.user;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class UserInventoryId implements Serializable {
    private Long userId;
    private Long itemId;
}