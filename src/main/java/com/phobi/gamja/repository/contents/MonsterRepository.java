package com.phobi.gamja.repository.contents;

import com.phobi.gamja.entity.contents.Monster;
import com.phobi.gamja.entity.contents.MonsterMap;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MonsterRepository extends JpaRepository<Monster, Long> {
    List<Monster> findByMapAndEnabledIsTrue(MonsterMap map);
}