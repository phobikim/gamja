package com.phobi.gamja.dto.contents;

import com.phobi.gamja.entity.contents.ActionCardEvent;
import com.phobi.gamja.entity.contents.ActionCardEventDrop;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class CardEventDto {
    private String cardText;
    private String eventMessage;
    private String eventType;
    private Integer hpChange;
    private Float expReward;

    private List<CardDropDto> drops;

    public static CardEventDto of(ActionCardEvent event, List<ActionCardEventDrop> drops) {
        return CardEventDto.builder()
                .cardText(event.getCardText())
                .eventMessage(event.getEventMessage())
                .eventType(event.getEventType().name())
                .hpChange(event.getHpChange())
                .expReward(event.getExpReward())
                .drops(drops.stream().map(CardDropDto::of).toList())
                .build();
    }
}