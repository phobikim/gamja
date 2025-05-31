package com.phobi.gamja.repository.user;

import com.phobi.gamja.entity.contents.SkillType;
import com.phobi.gamja.entity.user.UserSkill;
import com.phobi.gamja.entity.user.UserSkillId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserSkillRepository extends JpaRepository<UserSkill, UserSkillId> {
    Optional<UserSkill> findByUserIdAndSkillType(Long userId, SkillType type);
    List<UserSkill> findByUserId(Long userId);
}
