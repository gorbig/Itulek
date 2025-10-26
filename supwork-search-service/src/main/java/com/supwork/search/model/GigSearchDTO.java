package com.supwork.search.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GigSearchDTO {
    
    private String title;
    private List<String> skills;
    private String location; // Format: "lat,lng"
    private Double budget;
    private String description;
    private String status;
    private Long clientId;
    
    // Helper methods for location
    public Double getLatitude() {
        if (location != null && location.contains(",")) {
            return Double.parseDouble(location.split(",")[0]);
        }
        return null;
    }
    
    public Double getLongitude() {
        if (location != null && location.contains(",")) {
            return Double.parseDouble(location.split(",")[1]);
        }
        return null;
    }
    
    public void setLocation(Double latitude, Double longitude) {
        this.location = latitude + "," + longitude;
    }
}
