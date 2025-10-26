package com.supwork.gig.controller;

import com.supwork.gig.dto.CreateGigRequest;
import com.supwork.gig.dto.CreateRatingRequest;
import com.supwork.gig.dto.GigResponseDTO;
import com.supwork.gig.dto.RatingDTO;
import com.supwork.gig.service.GigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/gigs")
@RequiredArgsConstructor
@Tag(name = "Gig Management", description = "API для управления заказами (gigs)")
public class GigController {
    
    private final GigService gigService;
    
    @PostMapping
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Создание заказа", description = "Создание нового заказа клиентом")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Заказ успешно создан"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные запроса"),
            @ApiResponse(responseCode = "401", description = "Не авторизован"),
            @ApiResponse(responseCode = "403", description = "Нет доступа")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<GigResponseDTO> createGig(@Valid @RequestBody CreateGigRequest request, Authentication authentication) {
        log.info("Creating gig for authenticated user");
        
        // Extract client ID from authentication (assuming it's stored in JWT)
        Long clientId = Long.valueOf(authentication.getName());
        
        GigResponseDTO response = gigService.createGig(request, clientId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping
    @Operation(summary = "Получение списка открытых заказов", description = "Получение всех открытых заказов с пагинацией")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список заказов получен"),
            @ApiResponse(responseCode = "400", description = "Некорректные параметры пагинации")
    })
    public ResponseEntity<Page<GigResponseDTO>> getAllOpenGigs(Pageable pageable) {
        log.info("Fetching open gigs with pageable: {}", pageable);
        Page<GigResponseDTO> gigs = gigService.getAllOpenGigs(pageable);
        return ResponseEntity.ok(gigs);
    }
    
    @GetMapping("/my-gigs")
    @PreAuthorize("hasRole('CLIENT') or hasRole('TECHNICIAN')")
    @Operation(summary = "Получение своих заказов", description = "Получение всех заказов пользователя (созданные или назначенные)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список заказов получен"),
            @ApiResponse(responseCode = "401", description = "Не авторизован"),
            @ApiResponse(responseCode = "403", description = "Нет доступа")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Page<GigResponseDTO>> getMyGigs(Pageable pageable, Authentication authentication) {
        Long userId = Long.valueOf(authentication.getName());
        String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(authority -> authority.getAuthority().replace("ROLE_", ""))
                .orElse("USER");
        
        log.info("Fetching gigs for user ID: {}, role: {}", userId, role);
        Page<GigResponseDTO> gigs = gigService.getMyGigs(userId, role, pageable);
        return ResponseEntity.ok(gigs);
    }
    
    @PostMapping("/{id}/rate")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Оценка заказа", description = "Оценка выполненного заказа клиентом")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Оценка успешно создана"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные"),
            @ApiResponse(responseCode = "401", description = "Не авторизован"),
            @ApiResponse(responseCode = "403", description = "Нет доступа"),
            @ApiResponse(responseCode = "404", description = "Заказ не найден")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<RatingDTO> rateGig(@PathVariable Long id, @Valid @RequestBody CreateRatingRequest request, Authentication authentication) {
        Long clientId = Long.valueOf(authentication.getName());
        RatingDTO rating = gigService.createRating(id, clientId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(rating);
    }
    
    @GetMapping("/ratings/my")
    @PreAuthorize("hasRole('CLIENT') or hasRole('TECHNICIAN')")
    @Operation(summary = "Получение оценок пользователя", description = "Получение всех оценок пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список оценок получен"),
            @ApiResponse(responseCode = "401", description = "Не авторизован"),
            @ApiResponse(responseCode = "403", description = "Нет доступа")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<List<RatingDTO>> getMyRatings(Authentication authentication) {
        Long userId = Long.valueOf(authentication.getName());
        String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(authority -> authority.getAuthority().replace("ROLE_", ""))
                .orElse("USER");
        
        List<RatingDTO> ratings;
        if ("CLIENT".equalsIgnoreCase(role)) {
            ratings = gigService.getClientRatings(userId);
        } else if ("TECHNICIAN".equalsIgnoreCase(role)) {
            ratings = gigService.getTechnicianRatings(userId);
        } else {
            throw new IllegalArgumentException("Invalid role: " + role);
        }
        
        return ResponseEntity.ok(ratings);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Получение заказа по ID", description = "Получение информации о конкретном заказе")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Заказ найден"),
            @ApiResponse(responseCode = "404", description = "Заказ не найден")
    })
    public ResponseEntity<GigResponseDTO> getGigById(@PathVariable Long id) {
        log.info("Fetching gig with ID: {}", id);
        GigResponseDTO gig = gigService.getGigById(id);
        return ResponseEntity.ok(gig);
    }
    
    @PutMapping("/{id}/assign")
    @PreAuthorize("hasRole('TECHNICIAN')")
    @Operation(summary = "Назначение заказа мастеру", description = "Назначение заказа мастеру")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Заказ успешно назначен"),
            @ApiResponse(responseCode = "400", description = "Заказ недоступен для назначения"),
            @ApiResponse(responseCode = "401", description = "Не авторизован"),
            @ApiResponse(responseCode = "403", description = "Нет доступа"),
            @ApiResponse(responseCode = "404", description = "Заказ не найден")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<GigResponseDTO> assignGig(@PathVariable Long id, Authentication authentication) {
        log.info("Assigning gig ID: {} to technician");
        
        // Extract technician ID from authentication
        Long technicianId = Long.valueOf(authentication.getName());
        
        GigResponseDTO response = gigService.assignGig(id, technicianId);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Удаление заказа", description = "Удаление заказа владельцем")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Заказ успешно удален"),
            @ApiResponse(responseCode = "401", description = "Не авторизован"),
            @ApiResponse(responseCode = "403", description = "Нет доступа"),
            @ApiResponse(responseCode = "404", description = "Заказ не найден")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> deleteGig(@PathVariable Long id, Authentication authentication) {
        log.info("Deleting gig ID: {} by client");
        
        // Extract client ID from authentication
        Long clientId = Long.valueOf(authentication.getName());
        
        gigService.deleteGig(id, clientId);
        return ResponseEntity.noContent().build();
    }
}
