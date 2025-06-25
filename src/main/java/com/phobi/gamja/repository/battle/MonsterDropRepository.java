package com.phobi.gamja.repository.battle;

import com.phobi.gamja.entity.battle.Monster;
import com.phobi.gamja.entity.battle.MonsterDrop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MonsterDropRepository extends JpaRepository<MonsterDrop, Long> {

    // 특정 몬스터의 드랍 정보 조회
    List<MonsterDrop> findByMonster(Monster monster);

    // 몬스터 ID 기준 조회 (연관객체 없이)
    List<MonsterDrop> findByMonsterId(Long monsterId);
}
