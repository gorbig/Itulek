package com.supwork.search.client;

import com.supwork.search.model.TechnicianProfile;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class UserClientFallback implements UserClient {
    
    @Override
    public TechnicianProfile getUserProfile(Long userId) {
        // Return mock profile when user service is down
        return TechnicianProfile.builder()
                .userId(userId)
                .name("Unknown User")
                .email("unknown@example.com")
                .skills(Arrays.asList("General"))
                .location("0.0,0.0")
                .rating(3.0)
                .isAvailable(false)
                .build();
    }
}
