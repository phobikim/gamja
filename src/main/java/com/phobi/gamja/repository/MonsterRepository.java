package com.phobi.gamja.repository;

import com.phobi.gamja.entity.Monster;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MonsterRepository extends JpaRepository<Monster, Long> {
    // ex) 등급으로 몬스터 필터링
    List<Monster> findByRank(int rank);
}