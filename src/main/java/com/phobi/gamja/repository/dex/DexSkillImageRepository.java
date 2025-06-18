package com.phobi.gamja.repository.dex;

import com.phobi.gamja.entity.dex.DexSkillImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DexSkillImageRepository extends JpaRepository<DexSkillImage, Long> {
    List<DexSkillImage> findBySkillId(Long skillId);
}