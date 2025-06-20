package com.phobi.gamja.repository.contents;

import com.phobi.gamja.entity.contents.BackgroundImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BackgroundImageRepository extends JpaRepository<BackgroundImage, Long> {
    List<BackgroundImage> findByEnabledTrue();
}