package com.nisync.user.service.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.nisync.audit.service.AuditLogService;
import com.nisync.auth.service.JwtService;
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
	
	@Autowired
	private JwtService jwtService;

	@Autowired
	private AuditLogService auditLogService;
	
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

        auditLogService.record(
        		"USER_REGISTERED",
        		"USER",
        		savedUser.getId(),
        		savedUser.getEmail(),
        		"User registered: " + savedUser.getEmail()
        );
        
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

	    auditLogService.record(
	    		"USER_LOGIN",
	    		"USER",
	    		user.getId(),
	    		user.getEmail(),
	    		"User login: " + user.getEmail()
	    );

	    String token = jwtService.generateToken(
	            user.getId(),
	            user.getEmail(),
	            user.getRoles()
	    );
	    
	    
	    return new AuthResponseDto(
	    		token,
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
		logger.info("Fetching user by id: {}", userId);

		AppUser user = userRepository.findById(userId)
				.orElseThrow(() -> {
					logger.warn("User not found with id: {}", userId);
					return new ResourceNotFoundException("User not found with id: " + userId);
				});

		return UserMapperDto.toResponse(user);
	}

	@Override
	public List<UserResponseDto> getAllUsers(UserStatus status, RoleName role, String query) {
		logger.info("Fetching users. status: {}, role: {}, query: {}", status, role, query);

		return userRepository.findAll(
				buildUserSpecification(status, role, query),
				Sort.by(Sort.Direction.DESC, "createdAt")
		)
				.stream()
				.map(UserMapperDto::toResponse)
				.toList();
	}

	@Override
	public UserResponseDto updateUserById(Long userId, UserResponseDto userResponseDto, String actorEmail) {
		logger.info("Updating user by id: {}", userId);

		AppUser user = userRepository.findById(userId)
				.orElseThrow(() -> {
					logger.warn("User not found for update. id: {}", userId);
					return new ResourceNotFoundException("User not found with id: " + userId);
				});

		if (userResponseDto.getFullName() != null) {
			user.setFullName(userResponseDto.getFullName());
		}

		if (userResponseDto.getEmail() != null && !userResponseDto.getEmail().equals(user.getEmail())) {
			if (userRepository.existsByEmail(userResponseDto.getEmail())) {
				logger.warn("User update failed. Email already exists: {}", userResponseDto.getEmail());
				throw new DuplicateResourceException("Email already exists: " + userResponseDto.getEmail());
			}

			user.setEmail(userResponseDto.getEmail());
		}

		if (userResponseDto.getStatus() != null) {
			user.setStatus(userResponseDto.getStatus());
		}

		if (userResponseDto.getRoles() != null && !userResponseDto.getRoles().isEmpty()) {
			user.setRoles(userResponseDto.getRoles());
		}

		AppUser savedUser = userRepository.save(user);

		auditLogService.record(
				"USER_UPDATED",
				"USER",
				savedUser.getId(),
				actorEmail,
				"User updated: " + savedUser.getEmail()
		);

		logger.info("User updated successfully. userId: {}, email: {}", savedUser.getId(), savedUser.getEmail());

		return UserMapperDto.toResponse(savedUser);
	}

	@Override
	public UserResponseDto deleteUserById(Long userId, String actorEmail) {
		logger.info("Deleting user by id: {}", userId);

		AppUser user = userRepository.findById(userId)
				.orElseThrow(() -> {
					logger.warn("User not found for delete. id: {}", userId);
					return new ResourceNotFoundException("User not found with id: " + userId);
				});

		UserResponseDto response = UserMapperDto.toResponse(user);
		userRepository.delete(user);

		auditLogService.record(
				"USER_DELETED",
				"USER",
				userId,
				actorEmail,
				"User deleted: " + user.getEmail()
		);

		logger.info("User deleted successfully. userId: {}, email: {}", user.getId(), user.getEmail());

		return response;
	}

	@Override
	public void deleteAllUsers() {
		logger.warn("Deleting all users");
		userRepository.deleteAll();
	}

	private Specification<AppUser> buildUserSpecification(UserStatus status, RoleName role, String query) {
		return (root, criteriaQuery, criteriaBuilder) -> {
			var predicate = criteriaBuilder.conjunction();

			if (status != null) {
				predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("status"), status));
			}

			if (role != null) {
				predicate = criteriaBuilder.and(predicate, criteriaBuilder.isMember(role, root.get("roles")));
			}

			if (query != null && !query.isBlank()) {
				String searchTerm = "%" + query.trim().toLowerCase() + "%";
				var fullNamePredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("fullName")), searchTerm);
				var emailPredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), searchTerm);
				predicate = criteriaBuilder.and(predicate, criteriaBuilder.or(fullNamePredicate, emailPredicate));
			}

			return predicate;
		};
	}

}
