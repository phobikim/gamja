package com.example.gamja.repository;

import com.example.gamja.entity.UserDtl;
import com.example.gamja.entity.UserInventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserInventoryRepository extends JpaRepository<UserInventory, Long> {

}