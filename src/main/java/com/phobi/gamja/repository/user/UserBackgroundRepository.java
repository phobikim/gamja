package com.phobi.gamja.repository.user;

import com.phobi.gamja.entity.user.UserBackground;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserBackgroundRepository extends JpaRepository<UserBackground, Long> {
    List<UserBackground> findByUserId(Long userId);
}