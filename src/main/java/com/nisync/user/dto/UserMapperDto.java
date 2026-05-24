package com.nisync.user.dto;

import com.nisync.user.entity.AppUser;

public class UserMapperDto {

    private UserMapperDto() {
    }

    public static UserResponseDto toResponse(AppUser user) {
        UserResponseDto response = new UserResponseDto();

        response.setId(user.getId());
        response.setFullName(user.getFullName());
        response.setEmail(user.getEmail());
        response.setStatus(user.getStatus());
        response.setRoles(user.getRoles());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());

        return response;
    }
}