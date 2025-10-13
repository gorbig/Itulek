package com.supwork.user.controller;

import com.supwork.user.dto.UserDTO;
import com.supwork.user.dto.UserProfileDTO;
import com.supwork.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserProfileDTO> register(@Valid @RequestBody UserDTO userDTO) {
        UserProfileDTO profile = userService.register(userDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(profile);
    }

    @GetMapping("/{id}/profile")
    @PreAuthorize("hasRole('CLIENT') or hasRole('TECHNICIAN')")
    public ResponseEntity<UserProfileDTO> getProfile(@PathVariable Long id) {
        UserProfileDTO profile = userService.getProfile(id);
        return ResponseEntity.ok(profile);
    }

}

