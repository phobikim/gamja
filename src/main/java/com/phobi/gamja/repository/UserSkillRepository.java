package com.phobi.gamja.repository;

import com.phobi.gamja.entity.SkillType;
import com.phobi.gamja.entity.UserSkill;
import com.phobi.gamja.entity.UserSkillId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserSkillRepository extends JpaRepository<UserSkill, UserSkillId> {
    Optional<UserSkill> findByUserIdAndSkillType(Long userId, SkillType type);
}
