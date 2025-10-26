package com.supwork.search.service;

import com.supwork.search.model.GigSearchDTO;
import com.supwork.search.model.TechnicianProfile;
import com.supwork.search.repository.TechnicianProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchService {
    
    private final TechnicianProfileRepository technicianProfileRepository;
    private final MongoTemplate mongoTemplate;
    
    public Page<TechnicianProfile> searchTechnicians(List<String> skills, String location, 
                                                   Double minRating, Double maxDistance, Pageable pageable) {
        
        Query query = new Query();
        
        // Skills filter
        if (skills != null && !skills.isEmpty()) {
            query.addCriteria(Criteria.where("skills").in(skills));
        }
        
        // Rating filter
        if (minRating != null) {
            query.addCriteria(Criteria.where("rating").gte(minRating));
        }
        
        // Location filter (if maxDistance is specified)
        if (location != null && maxDistance != null) {
            String[] coords = location.split(",");
            if (coords.length == 2) {
                try {
                    double lat = Double.parseDouble(coords[0]);
                    double lng = Double.parseDouble(coords[1]);
                    
                    // Add location-based criteria (simplified - in real app would use geo queries)
                    query.addCriteria(Criteria.where("location").exists(true));
                } catch (NumberFormatException e) {
                    log.warn("Invalid location format: {}", location);
                }
            }
        }
        
        // Availability filter
        query.addCriteria(Criteria.where("isAvailable").is(true));
        
        long total = mongoTemplate.count(query, TechnicianProfile.class);
        
        query.with(pageable);
        List<TechnicianProfile> results = mongoTemplate.find(query, TechnicianProfile.class);
        
        return new org.springframework.data.domain.PageImpl<>(results, pageable, total);
    }
    
    public Page<GigSearchDTO> searchGigs(String title, List<String> skills, String location, 
                                        Double minBudget, Double maxBudget, Pageable pageable) {
        
        // This would typically query the gig service via Feign client
        // For now, return empty page as gigs are stored in gig-service
        return new org.springframework.data.domain.PageImpl<>(List.of(), pageable, 0);
    }
    
    public Pageable createPageable(int page, int size, String sortBy, String sortDir) {
        Sort sort = Sort.unsorted();
        
        if (sortBy != null && !sortBy.isEmpty()) {
            Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ? 
                Sort.Direction.DESC : Sort.Direction.ASC;
            sort = Sort.by(direction, sortBy);
        }
        
        return PageRequest.of(page, size, sort);
    }
}
