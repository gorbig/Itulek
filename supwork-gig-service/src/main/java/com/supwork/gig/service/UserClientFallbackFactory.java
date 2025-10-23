package com.supwork.gig.service;

import com.supwork.gig.dto.UserProfileDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserClientFallbackFactory {
    
    public UserClient createFallback(Throwable cause) {
        log.error("User service is down: {}", cause.getMessage());
        
        return new UserClient() {
            @Override
            public UserProfileDTO getProfile(Long id) {
                log.warn("User service fallback triggered for user ID: {}", id);
                // Return mock user profile for fallback
                return UserProfileDTO.builder()
                        .id(id)
                        .email("unknown@example.com")
                        .role("CLIENT") // Default role
                        .build();
            }
        };
    }
}
