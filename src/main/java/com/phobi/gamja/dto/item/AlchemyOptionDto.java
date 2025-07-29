package com.phobi.gamja.dto.item;

import com.phobi.gamja.entity.item.ItemAlchemyOption;

import java.math.BigDecimal;

public record AlchemyOptionDto(
        ItemAlchemyOption.OptionType optionType,
        ItemAlchemyOption.ValueType valueType,
        BigDecimal optionValue,
        String description
) {}
