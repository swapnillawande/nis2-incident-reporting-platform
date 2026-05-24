package com.nisync.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nisync.user.entity.AppUser;

public interface UserRepository extends JpaRepository<AppUser, Long>{
    Optional<AppUser> findByEmail(String email);

    boolean existsByEmail(String email);
}
