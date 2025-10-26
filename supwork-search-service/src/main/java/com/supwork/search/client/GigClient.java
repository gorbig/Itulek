package com.supwork.search.client;

import com.supwork.search.model.GigSearchDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Collections;
import java.util.List;

@FeignClient(name = "gig-service", fallback = GigClientFallback.class)
public interface GigClient {
    
    @GetMapping("/gigs/open")
    @CircuitBreaker(name = "gig-service", fallbackMethod = "fallbackGetOpenGigs")
    List<GigSearchDTO> getOpenGigs();
    
    @GetMapping("/gigs/{id}")
    @CircuitBreaker(name = "gig-service", fallbackMethod = "fallbackGetGigById")
    GigSearchDTO getGigById(@PathVariable("id") Long gigId);
    
    default List<GigSearchDTO> fallbackGetOpenGigs(Exception ex) {
        return Collections.emptyList();
    }
    
    default GigSearchDTO fallbackGetGigById(Long gigId, Exception ex) {
        return GigSearchDTO.builder()
                .title("Service Unavailable")
                .description("Gig service is currently unavailable")
                .skills(java.util.Arrays.asList("General"))
                .location("0.0,0.0")
                .budget(0.0)
                .status("UNAVAILABLE")
                .build();
    }
}
