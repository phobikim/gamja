package com.phobi.gamja.entity.dex;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "dex_attribute")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DexAttribute {

    @Id
    @Column(length = 20)
    private String name; // ex: 생감자, 튀김 등

    @Column(nullable = false, length = 100)
    private String description;

    @Column(name = "icon_path", length = 50)
    private String iconPath;
}