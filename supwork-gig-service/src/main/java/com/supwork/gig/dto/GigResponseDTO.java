package com.supwork.gig.dto;

import com.supwork.gig.entity.GigStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GigResponseDTO {
    
    private Long id;
    private String title;
    private String description;
    private Double budget;
    private String location;
    private GigStatus status;
    private Long clientId;
    private Long technicianId;
    private LocalDateTime createdAt;
}
