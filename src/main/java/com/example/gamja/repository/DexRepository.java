package com.example.gamja.repository;

import com.example.gamja.entity.Dex;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DexRepository extends JpaRepository<Dex, Long> {
    @Query("SELECT d FROM Dex d WHERE d.userFlag = true")
    List<Dex> findAllEnabledForUser();
}
