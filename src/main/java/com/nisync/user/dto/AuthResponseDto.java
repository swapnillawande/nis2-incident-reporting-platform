package com.nisync.user.dto;

import com.nisync.user.enums.RoleName;

import java.util.Set;


public class AuthResponseDto {

    private String token;

    private Long userId;

    private String fullName;

    private String email;

    private Set<RoleName> roles;

	public AuthResponseDto() {

	}

	public AuthResponseDto(String token, Long userId, String fullName, String email, Set<RoleName> roles) {
		super();
		this.token = token;
		this.userId = userId;
		this.fullName = fullName;
		this.email = email;
		this.roles = roles;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
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

	public Set<RoleName> getRoles() {
		return roles;
	}

	public void setRoles(Set<RoleName> roles) {
		this.roles = roles;
	}
    
    
    
    
}