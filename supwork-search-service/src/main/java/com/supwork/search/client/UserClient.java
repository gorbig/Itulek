package com.supwork.search.client;

import com.supwork.search.model.TechnicianProfile;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", fallback = UserClientFallback.class)
public interface UserClient {
    
    @GetMapping("/users/{id}/profile")
    @CircuitBreaker(name = "user-service", fallbackMethod = "fallbackGetUserProfile")
    TechnicianProfile getUserProfile(@PathVariable("id") Long userId);
    
    default TechnicianProfile fallbackGetUserProfile(Long userId, Exception ex) {
        return TechnicianProfile.builder()
                .userId(userId)
                .name("Service Unavailable")
                .email("unavailable@example.com")
                .skills(java.util.Arrays.asList("General"))
                .location("0.0,0.0")
                .rating(3.0)
                .isAvailable(false)
                .build();
    }
}
