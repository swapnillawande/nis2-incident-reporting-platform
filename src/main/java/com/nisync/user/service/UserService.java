package com.nisync.user.service;

import java.util.List;

import com.nisync.user.dto.RegisterRequestDto;
import com.nisync.user.dto.UserResponseDto;

public interface UserService {

    UserResponseDto register(RegisterRequestDto request);

    UserResponseDto getUserByEmail(String email);
    
    UserResponseDto getUserById(Long userId);
    
    List<UserResponseDto> getAllUsers();
    
    UserResponseDto updateUserById(Long userId, UserResponseDto userResponseDto);
    
    UserResponseDto deleteUserById(Long userId);
    
    void deleteAllUsers();
    
    

}
