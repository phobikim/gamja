package com.phobi.gamja.repository.skin;

import com.phobi.gamja.entity.skin.BackgroundImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BackgroundImageRepository extends JpaRepository<BackgroundImage, Long> {
    List<BackgroundImage> findByEnabledTrue();
}