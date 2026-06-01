package com.nisync.user.controller;

import java.util.List;

import com.nisync.common.response.PagedResponseDto;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nisync.user.dto.AuthResponseDto;
import com.nisync.user.dto.LoginRequestDto;
import com.nisync.user.dto.RegisterRequestDto;
import com.nisync.user.dto.UserResponseDto;
import com.nisync.user.enums.RoleName;
import com.nisync.user.enums.UserStatus;
import com.nisync.user.service.UserService;

import jakarta.validation.Valid;

import org.springframework.security.core.Authentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController()
@RequestMapping("users")
public class UserController {

	private static final Logger logger = LoggerFactory.getLogger(UserController.class);
	
	@Autowired
    private UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public UserResponseDto register(@Valid @RequestBody RegisterRequestDto request) {
    	
        logger.info("POST /api/users/register called for email: {}", request.getEmail());

        UserResponseDto response = userService.register(request);

        logger.info("User registration completed successfully. userId: {}, email: {}", response.getId(), response.getEmail());

        return response;
        
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponseDto createUser(
            @Valid @RequestBody RegisterRequestDto request,
            Authentication authentication) {

        logger.info("POST /users called by {} for email: {}", authentication.getName(), request.getEmail());

        UserResponseDto response = userService.createUser(request, authentication.getName());

        logger.info("User created successfully by admin. userId: {}, email: {}", response.getId(), response.getEmail());

        return response;
    }
    
    @PostMapping("/login")
    public AuthResponseDto login(@Valid @RequestBody LoginRequestDto request) {

        logger.info("POST /users/login called for email: {}", request.getEmail());

        AuthResponseDto response = userService.login(request);

        logger.info("User login completed successfully. userId: {}, email: {}",
                response.getUserId(), response.getEmail());

        return response;
    }

    @GetMapping("/email/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponseDto getUserByEmail(@PathVariable("email") String email) {
        logger.info("GET /api/users/email/{} called", email);

        UserResponseDto response = userService.getUserByEmail(email);

        logger.info("User fetched successfully. userId: {}, email: {}", response.getId(), response.getEmail());

        return response;
        
    }
    
    
    @GetMapping("/me")
    public UserResponseDto getCurrentUser(Authentication authentication) {

        String email = authentication.getName();

        logger.info("GET /users/me called for authenticated user: {}", email);

        return userService.getUserByEmail(email);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public PagedResponseDto<UserResponseDto> getAllUsers(
            @RequestParam(name = "status", required = false) UserStatus status,
            @RequestParam(name = "role", required = false) RoleName role,
            @RequestParam(name = "q", required = false) String query,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(name = "sortDir", defaultValue = "desc") String sortDir) {
        logger.info(
                "GET /users called. status: {}, role: {}, query: {}, page: {}, size: {}, sortBy: {}, sortDir: {}",
                status,
                role,
                query,
                page,
                size,
                sortBy,
                sortDir
        );

        return userService.getAllUsers(status, role, query, page, size, sortBy, sortDir);
    }

    @GetMapping("/export")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> exportUsers(
            @RequestParam(name = "status", required = false) UserStatus status,
            @RequestParam(name = "role", required = false) RoleName role,
            @RequestParam(name = "q", required = false) String query,
            Authentication authentication) {
        logger.info(
                "GET /users/export called by {}. status: {}, role: {}, query: {}",
                authentication.getName(),
                status,
                role,
                query
        );

        String csv = userService.exportUsersCsv(status, role, query, authentication.getName());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=users-export.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponseDto getUserById(@PathVariable("userId") Long userId) {
        logger.info("GET /users/{} called", userId);

        return userService.getUserById(userId);
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponseDto updateUserById(
            @PathVariable("userId") Long userId,
            @RequestBody UserResponseDto userResponseDto,
            Authentication authentication) {

        logger.info("PUT /users/{} called by {}", userId, authentication.getName());

        return userService.updateUserById(userId, userResponseDto, authentication.getName());
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponseDto deleteUserById(
            @PathVariable("userId") Long userId,
            Authentication authentication) {
        logger.info("DELETE /users/{} called by {}", userId, authentication.getName());

        return userService.deleteUserById(userId, authentication.getName());
    }
    
    
}
