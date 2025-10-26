package com.supwork.gig.service;

import com.supwork.gig.dto.UserProfileDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service")
public interface UserClient {
    
    @GetMapping("/users/{id}/profile")
    UserProfileDTO getProfile(@PathVariable("id") Long id);
}
