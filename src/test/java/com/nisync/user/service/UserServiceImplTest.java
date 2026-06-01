package com.nisync.user.service;

import com.nisync.common.response.PagedResponseDto;
import com.nisync.audit.service.AuditLogService;
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

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UserServiceImplTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private JwtService jwtService;
    private AuditLogService auditLogService;
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        jwtService = mock(JwtService.class);
        auditLogService = mock(AuditLogService.class);

        userService = new UserServiceImpl();

        ReflectionTestUtils.setField(userService, "userRepository", userRepository);
        ReflectionTestUtils.setField(userService, "passwordEncoder", passwordEncoder);
        ReflectionTestUtils.setField(userService, "jwtService", jwtService);
        ReflectionTestUtils.setField(userService, "auditLogService", auditLogService);
    }

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
    void shouldCreateUserByAdminSuccessfully() {
        RegisterRequestDto request = new RegisterRequestDto();
        request.setFullName("Analyst User");
        request.setEmail("analyst@test.com");
        request.setPassword("test@1234");
        request.setRole(RoleName.SECURITY_ANALYST);

        AppUser savedUser = buildUser(10L, "Analyst User", "analyst@test.com");
        savedUser.setRoles(Set.of(RoleName.SECURITY_ANALYST));

        when(userRepository.existsByEmail("analyst@test.com")).thenReturn(false);
        when(passwordEncoder.encode("test@1234")).thenReturn("encoded-password");
        when(userRepository.save(any(AppUser.class))).thenReturn(savedUser);

        UserResponseDto response = userService.createUser(request, "admin@nis2.com");

        assertEquals(10L, response.getId());
        assertEquals("analyst@test.com", response.getEmail());
        assertTrue(response.getRoles().contains(RoleName.SECURITY_ANALYST));
        verify(auditLogService).record(
                eq("USER_CREATED"),
                eq("USER"),
                eq(10L),
                eq("admin@nis2.com"),
                eq("User created: analyst@test.com")
        );
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

        when(userRepository.findAll(anyUserSpecification(), anyCreatedAtDescPageable()))
                .thenReturn(new PageImpl<>(List.of(firstUser, secondUser)));

        PagedResponseDto<UserResponseDto> response = userService.getAllUsers(
                null,
                null,
                null,
                null,
                null,
                0,
                10,
                "createdAt",
                "desc"
        );

        assertEquals(2, response.getContent().size());
        assertEquals("first@test.com", response.getContent().get(0).getEmail());
        assertEquals("second@test.com", response.getContent().get(1).getEmail());
    }

    @Test
    void shouldFilterUsersSuccessfully() {
        AppUser user = buildUser(1L, "Filtered User", "filtered@test.com");
        user.setRoles(Set.of(RoleName.AUDITOR));

        when(userRepository.findAll(anyUserSpecification(), anyEmailAscPageable()))
                .thenReturn(new PageImpl<>(List.of(user)));

        PagedResponseDto<UserResponseDto> response = userService.getAllUsers(
                UserStatus.ACTIVE,
                RoleName.AUDITOR,
                "filtered",
                LocalDateTime.of(2026, 1, 1, 0, 0),
                LocalDateTime.of(2026, 12, 31, 23, 59),
                0,
                10,
                "email",
                "asc"
        );

        assertEquals(1, response.getContent().size());
        assertEquals("filtered@test.com", response.getContent().get(0).getEmail());
        assertTrue(response.getContent().get(0).getRoles().contains(RoleName.AUDITOR));
    }

    @Test
    void shouldExportUsersCsvSuccessfully() {
        AppUser user = buildUser(1L, "Admin, \"Lead\"", "admin@test.com");
        user.setRoles(Set.of(RoleName.ADMIN, RoleName.AUDITOR));
        user.setCreatedAt(LocalDateTime.of(2026, 1, 15, 10, 30));
        user.setUpdatedAt(LocalDateTime.of(2026, 1, 16, 11, 45));

        when(userRepository.findAll(anyUserSpecification(), anyCreatedAtDescSort())).thenReturn(List.of(user));

        String csv = userService.exportUsersCsv(
                UserStatus.ACTIVE,
                RoleName.ADMIN,
                "admin",
                "admin@nis2.com"
        );

        assertTrue(csv.startsWith("ID,Full Name,Email,Status,Roles,Created At,Updated At"));
        assertTrue(csv.contains("1,\"Admin, \"\"Lead\"\"\",admin@test.com,ACTIVE,ADMIN;AUDITOR,2026-01-15T10:30,2026-01-16T11:45"));
        verify(auditLogService).record(
                eq("USERS_EXPORTED"),
                eq("USER"),
                eq(null),
                eq("admin@nis2.com"),
                eq("Users exported to CSV. Count: 1")
        );
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

        UserResponseDto response = userService.updateUserById(1L, updateRequest, "admin@nis2.com");

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

        assertThrows(
                DuplicateResourceException.class,
                () -> userService.updateUserById(1L, updateRequest, "admin@nis2.com")
        );
    }

    @Test
    void shouldDeleteUserByIdSuccessfully() {
        AppUser user = buildUser(1L, "Test User", "test@test.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponseDto response = userService.deleteUserById(1L, "admin@nis2.com");

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

    private Specification<AppUser> anyUserSpecification() {
        return any();
    }

    private Sort anyCreatedAtDescSort() {
        return argThat(sort -> sort.getOrderFor("createdAt") != null
                && Sort.Direction.DESC.equals(sort.getOrderFor("createdAt").getDirection()));
    }

    private Pageable anyCreatedAtDescPageable() {
        return argThat(pageable -> pageable.getSort().getOrderFor("createdAt") != null
                && Sort.Direction.DESC.equals(pageable.getSort().getOrderFor("createdAt").getDirection()));
    }

    private Pageable anyEmailAscPageable() {
        return argThat(pageable -> pageable.getSort().getOrderFor("email") != null
                && Sort.Direction.ASC.equals(pageable.getSort().getOrderFor("email").getDirection()));
    }
}
