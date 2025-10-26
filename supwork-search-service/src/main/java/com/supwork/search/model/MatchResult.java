package com.supwork.search.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchResult {
    
    private TechnicianProfile technician;
    private Double similarityScore;
    private Double distanceKm;
    private Double rating;
    private Integer skillsMatchCount;
    
}
