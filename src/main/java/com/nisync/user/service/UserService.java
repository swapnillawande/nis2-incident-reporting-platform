package com.nisync.user.service;

import java.util.List;

import com.nisync.user.dto.AuthResponseDto;
import com.nisync.user.dto.LoginRequestDto;
import com.nisync.user.dto.RegisterRequestDto;
import com.nisync.user.dto.UserResponseDto;
import com.nisync.user.enums.RoleName;
import com.nisync.user.enums.UserStatus;

public interface UserService {

    UserResponseDto register(RegisterRequestDto request);

    UserResponseDto createUser(RegisterRequestDto request, String actorEmail);
    
    AuthResponseDto login(LoginRequestDto request);

    UserResponseDto getUserByEmail(String email);
    
    UserResponseDto getUserById(Long userId);
    
    List<UserResponseDto> getAllUsers(UserStatus status, RoleName role, String query);

    String exportUsersCsv(UserStatus status, RoleName role, String query, String actorEmail);
    
    UserResponseDto updateUserById(Long userId, UserResponseDto userResponseDto, String actorEmail);
    
    UserResponseDto deleteUserById(Long userId, String actorEmail);
    
    void deleteAllUsers();
    
    

}
