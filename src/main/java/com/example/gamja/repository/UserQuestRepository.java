package com.example.gamja.repository;

import com.example.gamja.entity.UserQuest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface UserQuestRepository extends JpaRepository<UserQuest, Integer> {
    Optional<UserQuest> findByUserIdAndQuestIdAndQuestDate(Integer userId, Integer questId, LocalDate questDate);
}