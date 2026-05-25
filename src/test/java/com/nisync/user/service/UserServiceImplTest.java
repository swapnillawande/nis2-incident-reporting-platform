package com.nisync.user.service;

import com.nisync.auth.service.JwtService;
import com.nisync.user.dto.RegisterRequestDto;
import com.nisync.user.dto.UserResponseDto;
import com.nisync.user.entity.AppUser;
import com.nisync.user.enums.RoleName;
import com.nisync.user.enums.UserStatus;
import com.nisync.user.repository.UserRepository;
import com.nisync.user.service.impl.UserServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UserServiceImplTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private JwtService jwtService;
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        jwtService = mock(JwtService.class);

        userService = new UserServiceImpl();

        ReflectionTestUtils.setField(userService, "userRepository", userRepository);
        ReflectionTestUtils.setField(userService, "passwordEncoder", passwordEncoder);
        ReflectionTestUtils.setField(userService, "jwtService", jwtService);    }

    @Test
    void shouldRegisterUserSuccessfully() {

        RegisterRequestDto registerRequestDto = new RegisterRequestDto();
        registerRequestDto.setFullName("Test User");
        registerRequestDto.setEmail("test@test.com");
        registerRequestDto.setPassword("test@1234");
        registerRequestDto.setRole(RoleName.ADMIN);

        when(userRepository.existsByEmail("test@test.com")).thenReturn(false);
        when(passwordEncoder.encode("test@1234")).thenReturn("encoded-password");

        AppUser savedUser = new AppUser();
        savedUser.setId(1L);
        savedUser.setFullName("Test User");
        savedUser.setEmail("test@test.com");
        savedUser.setPasswordHash("encoded-password");
        savedUser.setStatus(UserStatus.ACTIVE);

        Set<RoleName> roles = new HashSet<>();
        roles.add(RoleName.ADMIN);
        savedUser.setRoles(roles);

        savedUser.setCreatedAt(LocalDateTime.now());
        savedUser.setUpdatedAt(LocalDateTime.now());

        when(userRepository.save(any(AppUser.class))).thenReturn(savedUser);

        UserResponseDto response = userService.register(registerRequestDto);

        assertNotNull(response);
        assertEquals("Test User", response.getFullName());
        assertEquals("test@test.com", response.getEmail());
        assertEquals(UserStatus.ACTIVE, response.getStatus());
        assertTrue(response.getRoles().contains(RoleName.ADMIN));
        
        
    }
}




