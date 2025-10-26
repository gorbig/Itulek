package com.supwork.user.service;

import com.supwork.user.dto.*;
import com.supwork.user.entity.User;
import com.supwork.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class for managing user operations including registration, authentication,
 * profile management, and JWT token handling.
 * 
 * This service provides the core business logic for user-related operations
 * in the SupWork platform, ensuring secure authentication and proper data management.
 * 
 * @author SupWork Team
 * @version 1.0
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    // Dependencies
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * Registers a new user in the system.
     * 
     * This method validates the user data, checks for email uniqueness,
     * encrypts the password, and creates a new user account.
     * 
     * @param userDTO the user registration data
     * @return UserProfileDTO containing the created user's profile information
     * @throws IllegalArgumentException if email already exists
     * @throws IllegalStateException if user creation fails
     */
    @Transactional
    public UserProfileDTO register(UserDTO userDTO) {
        log.info("Starting user registration for email: {}", userDTO.getEmail());
        
        // Validate email uniqueness
        if (userRepository.findByEmail(userDTO.getEmail()).isPresent()) {
            log.warn("Registration failed: Email already exists - {}", userDTO.getEmail());
            throw new IllegalArgumentException("Email already exists");
        }

        try {
            // Create new user entity
            User user = buildUserFromDTO(userDTO);
            User savedUser = userRepository.save(user);
            
            log.info("User registered successfully with ID: {}", savedUser.getId());
            return mapToProfileDTO(savedUser);
            
        } catch (Exception e) {
            log.error("Failed to register user: {}", e.getMessage(), e);
            throw new IllegalStateException("User registration failed", e);
        }
    }

    /**
     * Authenticates a user and generates JWT tokens.
     * 
     * This method validates user credentials and returns JWT tokens
     * for authenticated access to the system.
     * 
     * @param loginRequest the login credentials
     * @return JwtResponse containing access and refresh tokens
     * @throws UsernameNotFoundException if user doesn't exist
     * @throws BadCredentialsException if credentials are invalid
     */
    public JwtResponse login(LoginRequest loginRequest) {
        log.info("Attempting login for email: {}", loginRequest.getEmail());
        
        // Find user by email
        User user = findUserByEmail(loginRequest.getEmail());
        
        // Validate password
        if (!isPasswordValid(loginRequest.getPassword(), user.getPassword())) {
            log.warn("Login failed: Invalid credentials for email: {}", loginRequest.getEmail());
            throw new BadCredentialsException("Invalid credentials");
        }

        // Generate JWT tokens
        JwtResponse jwtResponse = generateJwtResponse(user);
        
        log.info("Login successful for user ID: {}", user.getId());
        return jwtResponse;
    }

    /**
     * Refreshes JWT tokens using a valid refresh token.
     * 
     * This method validates the refresh token and generates new access
     * and refresh tokens for continued authentication.
     * 
     * @param refreshTokenRequest containing the refresh token
     * @return JwtResponse with new tokens
     * @throws BadCredentialsException if refresh token is invalid
     * @throws UsernameNotFoundException if user no longer exists
     */
    public JwtResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        String token = refreshTokenRequest.getRefreshToken();
        log.info("Attempting token refresh");
        
        // Validate refresh token
        if (!jwtUtil.validateToken(token)) {
            log.warn("Token refresh failed: Invalid refresh token");
            throw new BadCredentialsException("Invalid refresh token");
        }

        try {
            // Extract user ID and find user
            String userId = jwtUtil.extractUserId(token);
            User user = findUserById(Long.parseLong(userId));
            
            // Generate new tokens
            JwtResponse jwtResponse = generateJwtResponse(user);
            
            log.info("Token refresh successful for user ID: {}", user.getId());
            return jwtResponse;
            
        } catch (NumberFormatException e) {
            log.error("Invalid user ID in refresh token: {}", e.getMessage());
            throw new BadCredentialsException("Invalid refresh token");
        }
    }

    /**
     * Loads a user by email for Spring Security authentication.
     * 
     * @param email the user's email address
     * @return User entity
     * @throws UsernameNotFoundException if user not found
     */
    public User loadByEmail(String email) {
        log.debug("Loading user by email: {}", email);
        return findUserByEmail(email);
    }

    /**
     * Retrieves a user's profile information.
     * 
     * @param userId the user's ID
     * @return UserProfileDTO containing user profile data
     * @throws UsernameNotFoundException if user not found
     */
    public UserProfileDTO getProfile(Long userId) {
        log.info("Retrieving profile for user ID: {}", userId);
        User user = findUserById(userId);
        return mapToProfileDTO(user);
    }

    /**
     * Updates a user's profile information.
     * 
     * Only provided fields are updated, null or empty fields are ignored.
     * 
     * @param userId the user's ID
     * @param updateRequest the profile update data
     * @return UserProfileDTO with updated profile information
     * @throws UsernameNotFoundException if user not found
     * @throws IllegalArgumentException if email is already taken by another user
     */
    @Transactional
    public UserProfileDTO updateProfile(Long userId, UserProfileDTO updateRequest) {
        log.info("Updating profile for user ID: {}", userId);
        
        User user = findUserById(userId);
        
        // Update email if provided and different
        if (updateRequest.getEmail() != null && !updateRequest.getEmail().isEmpty()) {
            updateUserEmail(user, updateRequest.getEmail());
        }
        
        // Update skills if provided
        if (updateRequest.getSkills() != null) {
            user.setSkills(updateRequest.getSkills());
            log.debug("Updated skills for user ID: {}", userId);
        }
        
        User updatedUser = userRepository.save(user);
        log.info("Profile updated successfully for user ID: {}", userId);
        
        return mapToProfileDTO(updatedUser);
    }

    // ==================== PRIVATE HELPER METHODS ====================

    /**
     * Finds a user by email address.
     * 
     * @param email the user's email
     * @return User entity
     * @throws UsernameNotFoundException if user not found
     */
    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }

    /**
     * Finds a user by ID.
     * 
     * @param userId the user's ID
     * @return User entity
     * @throws UsernameNotFoundException if user not found
     */
    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + userId));
    }

    /**
     * Validates a password against the stored hash.
     * 
     * @param rawPassword the raw password to validate
     * @param encodedPassword the encoded password from database
     * @return true if password matches, false otherwise
     */
    private boolean isPasswordValid(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    /**
     * Builds a User entity from UserDTO.
     * 
     * @param userDTO the user data transfer object
     * @return User entity with encrypted password
     */
    private User buildUserFromDTO(UserDTO userDTO) {
        return User.builder()
                .email(userDTO.getEmail())
                .password(passwordEncoder.encode(userDTO.getPassword()))
                .role(userDTO.getRole())
                .skills(userDTO.getSkills())
                .build();
    }

    /**
     * Generates JWT response with access and refresh tokens.
     * 
     * @param user the user entity
     * @return JwtResponse with tokens and user information
     */
    private JwtResponse generateJwtResponse(User user) {
        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        return JwtResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }

    /**
     * Updates user email with uniqueness validation.
     * 
     * @param user the user entity to update
     * @param newEmail the new email address
     * @throws IllegalArgumentException if email is already taken
     */
    private void updateUserEmail(User user, String newEmail) {
        if (!user.getEmail().equals(newEmail)) {
            // Check if new email is already taken by another user
            if (userRepository.findByEmail(newEmail).isPresent()) {
                throw new IllegalArgumentException("Email already exists: " + newEmail);
            }
            user.setEmail(newEmail);
            log.debug("Updated email for user ID: {}", user.getId());
        }
    }

    /**
     * Maps User entity to UserProfileDTO.
     * 
     * @param user the user entity
     * @return UserProfileDTO with user profile information
     */
    private UserProfileDTO mapToProfileDTO(User user) {
        return UserProfileDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .skills(user.getSkills())
                .build();
    }

}

