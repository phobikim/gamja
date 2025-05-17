package com.example.gamja.repository;

import com.example.gamja.entity.UserDtl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserDtlRepository extends JpaRepository<UserDtl, Long> {

}
