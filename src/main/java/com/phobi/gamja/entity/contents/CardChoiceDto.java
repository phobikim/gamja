package com.phobi.gamja.entity.contents;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CardChoiceDto {
    private Long id;
    private String cardText;

    public static CardChoiceDto of(ActionCardEvent e) {
        return new CardChoiceDto(
                e.getId(),
                e.getCardText()
        );
    }
}