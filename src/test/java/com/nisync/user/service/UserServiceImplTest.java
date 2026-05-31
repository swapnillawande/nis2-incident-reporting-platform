package com.nisync.user.service;

import com.nisync.auth.service.JwtService;
import com.nisync.common.exception.DuplicateResourceException;
import com.nisync.common.exception.ResourceNotFoundException;
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
import java.util.List;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
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

    @Test
    void shouldGetUserByIdSuccessfully() {
        AppUser user = buildUser(1L, "Test User", "test@test.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponseDto response = userService.getUserById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Test User", response.getFullName());
        assertEquals("test@test.com", response.getEmail());
    }

    @Test
    void shouldThrowWhenUserByIdDoesNotExist() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(99L));
    }

    @Test
    void shouldGetAllUsersSuccessfully() {
        AppUser firstUser = buildUser(1L, "First User", "first@test.com");
        AppUser secondUser = buildUser(2L, "Second User", "second@test.com");

        when(userRepository.findAll()).thenReturn(List.of(firstUser, secondUser));

        List<UserResponseDto> response = userService.getAllUsers();

        assertEquals(2, response.size());
        assertEquals("first@test.com", response.get(0).getEmail());
        assertEquals("second@test.com", response.get(1).getEmail());
    }

    @Test
    void shouldUpdateUserByIdSuccessfully() {
        AppUser user = buildUser(1L, "Old Name", "old@test.com");
        UserResponseDto updateRequest = new UserResponseDto();
        updateRequest.setFullName("New Name");
        updateRequest.setEmail("new@test.com");
        updateRequest.setStatus(UserStatus.ACTIVE);
        updateRequest.setRoles(Set.of(RoleName.SECURITY_ANALYST));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(userRepository.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponseDto response = userService.updateUserById(1L, updateRequest);

        assertEquals("New Name", response.getFullName());
        assertEquals("new@test.com", response.getEmail());
        assertTrue(response.getRoles().contains(RoleName.SECURITY_ANALYST));
    }

    @Test
    void shouldThrowWhenUpdateEmailAlreadyExists() {
        AppUser user = buildUser(1L, "Old Name", "old@test.com");
        UserResponseDto updateRequest = new UserResponseDto();
        updateRequest.setEmail("taken@test.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("taken@test.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> userService.updateUserById(1L, updateRequest));
    }

    @Test
    void shouldDeleteUserByIdSuccessfully() {
        AppUser user = buildUser(1L, "Test User", "test@test.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponseDto response = userService.deleteUserById(1L);

        assertEquals(1L, response.getId());
        verify(userRepository).delete(user);
    }

    private AppUser buildUser(Long id, String fullName, String email) {
        AppUser user = new AppUser();
        user.setId(id);
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPasswordHash("encoded-password");
        user.setStatus(UserStatus.ACTIVE);
        user.setRoles(Set.of(RoleName.ADMIN));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        return user;
    }
}



