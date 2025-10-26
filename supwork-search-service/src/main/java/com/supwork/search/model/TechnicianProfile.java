package com.supwork.search.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "technician_profiles")
public class TechnicianProfile {
    
    @Id
    private String id;
    
    @Indexed
    private Long userId;
    
    @Indexed
    private List<String> skills;
    
    private String location; // Format: "lat,lng"
    
    @Indexed
    private Double rating;
    
    private String name;
    private String email;
    private String phone;
    private String bio;
    private Boolean isAvailable;
    
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
