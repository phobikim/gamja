package com.phobi.gamja.repository.title;

import com.phobi.gamja.entity.title.Title;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TitleRepository extends JpaRepository<Title, Long> {
    List<Title> findAllByUseFlagTrue();  // 사용 가능한 칭호만 조회
}