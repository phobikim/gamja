package com.phobi.gamja.entity.user;

import com.phobi.gamja.entity.item.ItemAlchemyOption;
import lombok.*;
import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Entity
@Table(name = "user_item_alchemy_option")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserItemAlchemyOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private Long userItemId;

    private Integer optionIndex; // 1부터 시작

    @Enumerated(EnumType.STRING)
    private ItemAlchemyOption.OptionType optionType;

    @Enumerated(EnumType.STRING)
    private ItemAlchemyOption.ValueType valueType; // JUNK일 경우 null

    @Column(precision = 5, scale = 2)
    private BigDecimal optionValue;

    @Column(length = 255)
    private String description;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
