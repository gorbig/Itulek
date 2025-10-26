package com.supwork.search.client;

import com.supwork.search.model.GigSearchDTO;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
public class GigClientFallback implements GigClient {
    
    @Override
    public List<GigSearchDTO> getOpenGigs() {
        // Return empty list when gig service is down
        return Collections.emptyList();
    }
    
    @Override
    public GigSearchDTO getGigById(Long gigId) {
        // Return mock gig when gig service is down
        return GigSearchDTO.builder()
                .title("Service Unavailable")
                .description("Gig service is currently unavailable")
                .skills(Arrays.asList("General"))
                .location("0.0,0.0")
                .budget(0.0)
                .status("UNAVAILABLE")
                .build();
    }
}
