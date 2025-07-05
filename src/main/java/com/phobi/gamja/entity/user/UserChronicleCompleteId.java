package com.phobi.gamja.entity.user;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class UserChronicleCompleteId implements Serializable {
    private Long userId;
    private Long mapId;
}