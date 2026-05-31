package com.nisync.user.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nisync.user.dto.AuthResponseDto;
import com.nisync.user.dto.LoginRequestDto;
import com.nisync.user.dto.RegisterRequestDto;
import com.nisync.user.dto.UserResponseDto;
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
    public List<UserResponseDto> getAllUsers() {
        logger.info("GET /users called");

        return userService.getAllUsers();
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
            @RequestBody UserResponseDto userResponseDto) {

        logger.info("PUT /users/{} called", userId);

        return userService.updateUserById(userId, userResponseDto);
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponseDto deleteUserById(@PathVariable("userId") Long userId) {
        logger.info("DELETE /users/{} called", userId);

        return userService.deleteUserById(userId);
    }
    
    
}
