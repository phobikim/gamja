package com.example.gamja.repository;


import com.example.gamja.entity.UserDex;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserDexRepository extends JpaRepository<UserDex, Long> {
    List<UserDex> findByUserId(Long userId);

    boolean existsByUserIdAndDexId(Long userId, Long dexId);
}
