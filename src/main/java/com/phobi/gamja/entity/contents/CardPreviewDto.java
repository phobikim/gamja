package com.phobi.gamja.entity.contents;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class CardPreviewDto {
    private Long id;
    private String cardText;
    private String eventMessage;
    private String eventType;
    private List<DropPreviewDto> items;

    public static CardPreviewDto of(ActionCardEvent e, List<ActionCardEventDrop> drops) {
        return new CardPreviewDto(
                e.getId(),
                e.getCardText(),
                e.getEventMessage(),
                e.getEventType().name(),
                drops.stream().map(d -> new DropPreviewDto(
                        d.getItem().getName(),
                        d.getItem().getIconPath()
                )).toList()
        );
    }
}