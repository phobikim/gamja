package com.phobi.gamja.repository;

import com.phobi.gamja.entity.SkillShop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SkillShopRepository extends JpaRepository<SkillShop, Long> {
    Optional<SkillShop> findBySkillType(String skillType);
}
