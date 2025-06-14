package com.phobi.gamja.repository.title;

import com.phobi.gamja.entity.title.TitleEffect;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TitleEffectRepository extends JpaRepository<TitleEffect, Long> {
    List<TitleEffect> findByTitleId(Long titleId);
}
