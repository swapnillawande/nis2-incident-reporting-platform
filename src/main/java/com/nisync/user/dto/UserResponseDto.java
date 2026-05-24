package com.nisync.user.dto;

import com.nisync.user.enums.UserStatus;
import com.nisync.user.enums.RoleName;


import java.time.LocalDateTime;
import java.util.Set;


public class UserResponseDto {

    private Long id;

    private String fullName;

    private String email;

    private UserStatus status;

    private Set<RoleName> roles;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

	public UserResponseDto() {

	}

	public UserResponseDto(Long id, String fullName, String email, UserStatus status, Set<RoleName> roles,
			LocalDateTime createdAt, LocalDateTime updatedAt) {
		super();
		this.id = id;
		this.fullName = fullName;
		this.email = email;
		this.status = status;
		this.roles = roles;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public UserStatus getStatus() {
		return status;
	}

	public void setStatus(UserStatus status) {
		this.status = status;
	}

	public Set<RoleName> getRoles() {
		return roles;
	}

	public void setRoles(Set<RoleName> roles) {
		this.roles = roles;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}
    
    
    
    
    
    
}