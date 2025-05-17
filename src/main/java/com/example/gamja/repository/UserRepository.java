package com.example.gamja.repository;

import com.example.gamja.entity.User;
import com.example.gamja.entity.UserDtl;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByUsernameAndPin(String username, String pin);
}
