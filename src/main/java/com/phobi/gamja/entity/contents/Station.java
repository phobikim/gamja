package com.phobi.gamja.entity.contents;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Data
@Table(name="station")
public class Station {
    @Id
    @Column(name = "id", nullable = false)
    private Long id;
    @Column(name = "name", nullable = false)
    private String name;
    @Column(name = "category", nullable = false)
    private String category;
    private String description;
    private String imagePath;
    @Column(name = "order_num", nullable = false)
    private Integer orderNum;

    @Column(name = "use_flag", nullable = false)
    private Boolean useFlag; // true: 사용, false: 미사용

}
