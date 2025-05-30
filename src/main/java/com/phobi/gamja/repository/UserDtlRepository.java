package com.phobi.gamja.repository;

import com.phobi.gamja.entity.User;
import com.phobi.gamja.entity.UserDtl;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserDtlRepository extends JpaRepository<UserDtl, Long> {
    Optional<UserDtl> findByUser(User user);
}
