package com.supwork.user.controller;

import com.supwork.user.dto.JwtResponse;
import com.supwork.user.dto.LoginRequest;
import com.supwork.user.dto.RefreshTokenRequest;
import com.supwork.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "API for user authentication")
public class AuthController {

    private final UserService userService;

    @PostMapping("/login")
    @Operation(summary = "User Login", description = "Authenticate user by email and password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful authentication"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        JwtResponse response = userService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh Token", description = "Refresh JWT token using refresh token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token successfully refreshed"),
            @ApiResponse(responseCode = "401", description = "Invalid refresh token"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<JwtResponse> refresh(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        JwtResponse response = userService.refreshToken(refreshTokenRequest);
        return ResponseEntity.ok(response);
    }

}

