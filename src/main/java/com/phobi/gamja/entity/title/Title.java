package com.phobi.gamja.entity.title;

import com.phobi.gamja.entity.item.Item;
import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import com.phobi.gamja.entity.user.CounterType;

@Entity
@Table(name = "title")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Title {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "counter_type", nullable = false)
    private CounterType counterType;

    @Enumerated(EnumType.STRING)
    private Item.Rarity rarity;

    @Column(name = "icon_path")
    private String iconPath;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "title", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TitleEffect> effects;

    @OneToMany(mappedBy = "title", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TitleCondition> conditions;
}

