package com.phobi.gamja.entity.item;

import com.phobi.gamja.dto.item.EquipmentSlot;
import lombok.*;
import javax.persistence.*;
import java.math.BigDecimal;
@Entity
@Table(name = "item_alchemy_option")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemAlchemyOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)

    @Column(nullable = false)
    private Item.Rarity rarity;

    @Enumerated(EnumType.STRING)
    @Column(name = "equip_slot", nullable = false)
    private EquipmentSlot equipSlot;

    @Enumerated(EnumType.STRING)
    @Column(name = "option_type", nullable = false)
    private OptionType optionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "value_type")
    private ValueType valueType; // null 가능 (JUNK)

    @Column(name = "min_value", precision = 5, scale = 2)
    private BigDecimal minValue;

    @Column(name = "max_value", precision = 5, scale = 2)
    private BigDecimal maxValue;

    @Column
    private Integer weight;

    @Column(length = 255)
    private String description;

    public enum OptionType {
        ATTACK, HP, DEFENSE, CRIT_RATE, CRIT_DMG, EXP_GAIN, GOLD_GAIN
    }

    public enum ValueType {
        FLAT, PERCENT
    }
}
