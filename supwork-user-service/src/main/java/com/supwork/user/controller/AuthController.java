package com.supwork.user.controller;

import com.supwork.user.dto.JwtResponse;
import com.supwork.user.dto.LoginRequest;
import com.supwork.user.dto.RefreshTokenRequest;
import com.supwork.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        JwtResponse response = userService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtResponse> refresh(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        JwtResponse response = userService.refreshToken(refreshTokenRequest);
        return ResponseEntity.ok(response);
    }

}

