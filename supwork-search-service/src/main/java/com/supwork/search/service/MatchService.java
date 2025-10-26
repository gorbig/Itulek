package com.supwork.search.service;

import com.supwork.search.client.GigClient;
import com.supwork.search.model.GigSearchDTO;
import com.supwork.search.model.MatchResult;
import com.supwork.search.model.TechnicianProfile;
import com.supwork.search.repository.TechnicianProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchService {
    
    private final TechnicianProfileRepository technicianProfileRepository;
    private final GigClient gigClient;
    
    @Cacheable(key = "#gigId", cacheNames = "matches")
    public List<MatchResult> findMatchesForGig(Long gigId) {
        log.info("Finding matches for gig: {}", gigId);
        
        // Get gig details
        GigSearchDTO gig = gigClient.getGigById(gigId);
        if (gig == null || gig.getSkills() == null || gig.getSkills().isEmpty()) {
            return List.of();
        }
        
        // Get all available technicians
        List<TechnicianProfile> technicians = technicianProfileRepository.findByIsAvailableTrue();
        
        // Calculate matches using Java streams
        return technicians.stream()
                .filter(tech -> tech.getRating() != null && tech.getRating() >= 4.0)
                .map(tech -> calculateMatch(gig, tech))
                .filter(match -> match.getDistanceKm() <= 10.0) // Within 10km
                .sorted((m1, m2) -> Double.compare(m2.getSimilarityScore(), m1.getSimilarityScore()))
                .limit(5)
                .collect(Collectors.toList());
    }
    
    public List<MatchResult> findMatchesForTechnician(Long technicianId) {
        log.info("Finding matches for technician: {}", technicianId);
        
        // Get technician profile
        TechnicianProfile technician = technicianProfileRepository.findByUserId(technicianId);
        if (technician == null || technician.getSkills() == null || technician.getSkills().isEmpty()) {
            return List.of();
        }
        
        // Get open gigs
        List<GigSearchDTO> gigs = gigClient.getOpenGigs();
        
        // Calculate matches
        return gigs.stream()
                .map(gig -> calculateMatchForTechnician(gig, technician))
                .filter(match -> match.getDistanceKm() <= 10.0)
                .sorted((m1, m2) -> Double.compare(m2.getSimilarityScore(), m1.getSimilarityScore()))
                .limit(5)
                .collect(Collectors.toList());
    }
    
    private MatchResult calculateMatch(GigSearchDTO gig, TechnicianProfile technician) {
        // Calculate cosine similarity for skills
        double similarityScore = calculateCosineSimilarity(gig.getSkills(), technician.getSkills());
        
        // Calculate distance using Haversine formula
        double distance = calculateDistance(
            gig.getLatitude(), gig.getLongitude(),
            technician.getLatitude(), technician.getLongitude()
        );
        
        // Count matching skills
        int skillsMatchCount = (int) technician.getSkills().stream()
                .filter(skill -> gig.getSkills().contains(skill))
                .count();
        
        return MatchResult.builder()
                .technician(technician)
                .similarityScore(similarityScore)
                .distanceKm(distance)
                .rating(technician.getRating())
                .skillsMatchCount(skillsMatchCount)
                .build();
    }
    
    private MatchResult calculateMatchForTechnician(GigSearchDTO gig, TechnicianProfile technician) {
        // Similar calculation but from technician perspective
        double similarityScore = calculateCosineSimilarity(technician.getSkills(), gig.getSkills());
        
        double distance = calculateDistance(
            gig.getLatitude(), gig.getLongitude(),
            technician.getLatitude(), technician.getLongitude()
        );
        
        int skillsMatchCount = (int) gig.getSkills().stream()
                .filter(skill -> technician.getSkills().contains(skill))
                .count();
        
        return MatchResult.builder()
                .technician(technician)
                .similarityScore(similarityScore)
                .distanceKm(distance)
                .rating(technician.getRating())
                .skillsMatchCount(skillsMatchCount)
                .build();
    }
    
    private double calculateCosineSimilarity(List<String> skills1, List<String> skills2) {
        if (skills1 == null || skills2 == null || skills1.isEmpty() || skills2.isEmpty()) {
            return 0.0;
        }
        
        // Create vectors for skills
        List<String> allSkills = skills1.stream()
                .distinct()
                .collect(Collectors.toList());
        allSkills.addAll(skills2.stream().distinct().collect(Collectors.toList()));
        allSkills = allSkills.stream().distinct().collect(Collectors.toList());
        
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;
        
        for (String skill : allSkills) {
            int count1 = (int) skills1.stream().filter(s -> s.equals(skill)).count();
            int count2 = (int) skills2.stream().filter(s -> s.equals(skill)).count();
            
            dotProduct += count1 * count2;
            norm1 += count1 * count1;
            norm2 += count2 * count2;
        }
        
        if (norm1 == 0.0 || norm2 == 0.0) {
            return 0.0;
        }
        
        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
    
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        if (lat1 == 0.0 && lon1 == 0.0) return Double.MAX_VALUE;
        if (lat2 == 0.0 && lon2 == 0.0) return Double.MAX_VALUE;
        
        final int R = 6371; // Radius of the earth in km
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c; // Distance in km
    }
}
