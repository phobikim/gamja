package com.phobi.gamja.repository.skin;

import com.phobi.gamja.entity.skin.SkinBorder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SkinBorderRepository extends JpaRepository<SkinBorder, Long> {
    List<SkinBorder> findByEnabledTrue();
}