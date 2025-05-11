package com.example.gamja.entity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import javax.persistence.*;
@Entity
@Data
@Table(name = "user_inventory")
public class UserInventory {
    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id")
    @JsonIgnore // ✅ 여기!
    private User user;

    private int money;
    private int fish;
    private int wood;
    private int stone;
    private int food;
}
