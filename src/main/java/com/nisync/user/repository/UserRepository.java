package com.nisync.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.nisync.user.entity.AppUser;
import com.nisync.user.enums.RoleName;
import com.nisync.user.enums.UserStatus;

public interface UserRepository extends JpaRepository<AppUser, Long>, JpaSpecificationExecutor<AppUser> {
    Optional<AppUser> findByEmail(String email);

    boolean existsByEmail(String email);

    long countByStatus(UserStatus status);

    @Query("select count(user) from AppUser user join user.roles role where role = :role")
    long countByRole(@Param("role") RoleName role);
}
