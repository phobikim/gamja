package com.phobi.gamja.repository.battle;

import com.phobi.gamja.entity.battle.Monster;
import com.phobi.gamja.entity.battle.MonsterMap;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MonsterRepository extends JpaRepository<Monster, Long> {
    List<Monster> findByMapAndEnabledIsTrue(MonsterMap map);
    List<Monster> findByMapId(Long mapId);
    Optional<Monster> findFirstByMapId(Long mapId);
}