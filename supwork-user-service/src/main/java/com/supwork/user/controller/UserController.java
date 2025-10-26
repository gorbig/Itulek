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
@Tag(name = "User Management", description = "API для управления пользователями")
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    @Operation(summary = "Регистрация пользователя", description = "Создание нового пользователя в системе")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Пользователь успешно создан"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные запроса"),
            @ApiResponse(responseCode = "409", description = "Пользователь с таким email уже существует")
    })
    public ResponseEntity<UserProfileDTO> register(@Valid @RequestBody UserDTO userDTO) {
        UserProfileDTO profile = userService.register(userDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(profile);
    }

    @GetMapping("/{id}/profile")
    @PreAuthorize("hasRole('CLIENT') or hasRole('TECHNICIAN')")
    @Operation(summary = "Получение профиля пользователя", description = "Получение профиля пользователя по ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Профиль пользователя получен"),
            @ApiResponse(responseCode = "401", description = "Не авторизован"),
            @ApiResponse(responseCode = "403", description = "Нет доступа"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<UserProfileDTO> getProfile(@PathVariable Long id) {
        UserProfileDTO profile = userService.getProfile(id);
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/{id}/profile")
    @PreAuthorize("hasRole('CLIENT') or hasRole('TECHNICIAN')")
    @Operation(summary = "Обновление профиля пользователя", description = "Обновление профиля пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Профиль успешно обновлен"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные"),
            @ApiResponse(responseCode = "401", description = "Не авторизован"),
            @ApiResponse(responseCode = "403", description = "Нет доступа"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<UserProfileDTO> updateProfile(@PathVariable Long id, @Valid @RequestBody UserProfileDTO updateRequest) {
        UserProfileDTO updatedProfile = userService.updateProfile(id, updateRequest);
        return ResponseEntity.ok(updatedProfile);
    }

}

