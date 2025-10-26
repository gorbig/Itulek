package com.supwork.gig.service;

import com.supwork.gig.dto.UserProfileDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service")
public interface UserClient {
    
    @GetMapping("/users/{id}/profile")
    @CircuitBreaker(name = "user-service", fallbackMethod = "fallbackGetProfile")
    UserProfileDTO getProfile(@PathVariable("id") Long id);
    
    default UserProfileDTO fallbackGetProfile(Long id, Exception ex) {
        return UserProfileDTO.builder()
                .id(id)
                .email("unavailable@example.com")
                .role("USER")
                .skills(java.util.Arrays.asList("General"))
                .build();
    }
}
