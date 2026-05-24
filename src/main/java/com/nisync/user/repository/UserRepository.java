package com.nisync.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nisync.user.entity.AppUser;

public interface UserRepository extends JpaRepository<AppUser, Long>{

}
