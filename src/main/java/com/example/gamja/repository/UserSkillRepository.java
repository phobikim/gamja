package com.example.gamja.repository;

import com.example.gamja.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserSkillRepository extends JpaRepository<UserSkill, UserSkillId> {
    Optional<UserSkill> findByUserIdAndSkillType(Long userId, SkillType type);
}
