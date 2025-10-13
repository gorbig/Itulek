package com.supwork.user.dto;

import com.supwork.user.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileDTO {
    
    private Long id;
    private String email;
    private Role role;
    private List<String> skills;
    
}

