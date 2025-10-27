package com.supwork.user.controller;

import com.supwork.user.dto.UserDTO;
import com.supwork.user.dto.UserProfileDTO;
import com.supwork.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "API for user management")
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    @Operation(summary = "User Registration", description = "Create a new user in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User successfully created"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "409", description = "User with this email already exists")
    })
    public ResponseEntity<UserProfileDTO> register(@Valid @RequestBody UserDTO userDTO) {
        UserProfileDTO profile = userService.register(userDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(profile);
    }

    @GetMapping("/{id}/profile")
    @PreAuthorize("hasRole('CLIENT') or hasRole('TECHNICIAN')")
    @Operation(summary = "Get User Profile", description = "Get user profile by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User profile retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<UserProfileDTO> getProfile(@PathVariable Long id) {
        UserProfileDTO profile = userService.getProfile(id);
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/{id}/profile")
    @PreAuthorize("hasRole('CLIENT') or hasRole('TECHNICIAN')")
    @Operation(summary = "Update User Profile", description = "Update user profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile successfully updated"),
            @ApiResponse(responseCode = "400", description = "Invalid data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<UserProfileDTO> updateProfile(@PathVariable Long id, @Valid @RequestBody UserProfileDTO updateRequest) {
        UserProfileDTO updatedProfile = userService.updateProfile(id, updateRequest);
        return ResponseEntity.ok(updatedProfile);
    }

}

