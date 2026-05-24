package com.nisync.user.service.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.nisync.common.exception.BadRequestException;
import com.nisync.common.exception.DuplicateResourceException;
import com.nisync.common.exception.ResourceNotFoundException;
import com.nisync.user.dto.AuthResponseDto;
import com.nisync.user.dto.LoginRequestDto;
import com.nisync.user.dto.RegisterRequestDto;
import com.nisync.user.dto.UserMapperDto;
import com.nisync.user.dto.UserResponseDto;
import com.nisync.user.entity.AppUser;
import com.nisync.user.enums.RoleName;
import com.nisync.user.enums.UserStatus;
import com.nisync.user.repository.UserRepository;
import com.nisync.user.service.UserService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UserServiceImpl implements UserService{

	private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
	
	@Autowired
    private UserRepository userRepository;
    
	@Autowired
    private PasswordEncoder passwordEncoder;
	
	@Override
	public UserResponseDto register(RegisterRequestDto request) {
		
        logger.info("Register request received for email: {}", request.getEmail());
		
        if (userRepository.existsByEmail(request.getEmail())) {
            logger.warn("Registration failed... Email already exists: {}", request.getEmail());
            throw new DuplicateResourceException("Email already exists: " + request.getEmail());
        }

        AppUser user = new AppUser();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setStatus(UserStatus.ACTIVE);

        Set<RoleName> roles = new HashSet<>();
        roles.add(request.getRole());
        user.setRoles(roles);

        AppUser savedUser = userRepository.save(user);
        
        logger.info("User registered successfully. userId: {}, email: {}", savedUser.getId(), savedUser.getEmail());

        return UserMapperDto.toResponse(savedUser);
        
        
	}
	
	@Override
	public AuthResponseDto login(LoginRequestDto request) {

	    logger.info("Login request received for email: {}", request.getEmail());

	    AppUser user = userRepository.findByEmail(request.getEmail())
	            .orElseThrow(() -> {
	                logger.warn("Login failed. User not found with email: {}", request.getEmail());
	                return new ResourceNotFoundException("User not found with email: " + request.getEmail());
	            });

	    boolean passwordMatches = passwordEncoder.matches(
	            request.getPassword(),
	            user.getPasswordHash()
	    );

	    if (!passwordMatches) {
	        logger.warn("Login failed. Invalid password for email: {}", request.getEmail());
	        throw new BadRequestException("Invalid email or password");
	    }

	    logger.info("Login successful. userId: {}, email: {}", user.getId(), user.getEmail());

	    return new AuthResponseDto(
	            null,
	            user.getId(),
	            user.getFullName(),
	            user.getEmail(),
	            user.getRoles()
	    );
	}
	

	@Override
	public UserResponseDto getUserByEmail(String email) {
		
        logger.info("Fetching user by email: {}", email);

        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.warn("User not found with email: {}", email);
                    throw new ResourceNotFoundException("User not found with email: " + email);
                });

        logger.info("User found. userId: {}, email: {}", user.getId(), user.getEmail());

        return UserMapperDto.toResponse(user);
        
	}

	@Override
	public UserResponseDto getUserById(Long userId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<UserResponseDto> getAllUsers() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UserResponseDto updateUserById(Long userId, UserResponseDto userResponseDto) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UserResponseDto deleteUserById(Long userId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteAllUsers() {
		// TODO Auto-generated method stub
		
	}

}
