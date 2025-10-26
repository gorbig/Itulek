package com.supwork.search.controller;

import com.supwork.search.model.GigSearchDTO;
import com.supwork.search.model.TechnicianProfile;
import com.supwork.search.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
@Tag(name = "Search", description = "Search operations for technicians and gigs")
public class SearchController {
    
    private final SearchService searchService;
    
    @GetMapping("/technicians")
    @Operation(summary = "Search technicians", description = "Search for technicians with filters")
    public ResponseEntity<Page<TechnicianProfile>> searchTechnicians(
            @Parameter(description = "Skills to filter by") @RequestParam(required = false) List<String> skills,
            @Parameter(description = "Location to search near") @RequestParam(required = false) String location,
            @Parameter(description = "Minimum rating") @RequestParam(required = false) Double minRating,
            @Parameter(description = "Maximum distance in km") @RequestParam(required = false) Double maxDistance,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort by field") @RequestParam(required = false) String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {
        
        Pageable pageable = searchService.createPageable(page, size, sortBy, sortDir);
        Page<TechnicianProfile> result = searchService.searchTechnicians(skills, location, minRating, maxDistance, pageable);
        
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/gigs")
    @Operation(summary = "Search gigs", description = "Search for gigs with filters")
    public ResponseEntity<Page<GigSearchDTO>> searchGigs(
            @Parameter(description = "Title to search for") @RequestParam(required = false) String title,
            @Parameter(description = "Skills to filter by") @RequestParam(required = false) List<String> skills,
            @Parameter(description = "Location to search near") @RequestParam(required = false) String location,
            @Parameter(description = "Minimum budget") @RequestParam(required = false) Double minBudget,
            @Parameter(description = "Maximum budget") @RequestParam(required = false) Double maxBudget,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort by field") @RequestParam(required = false) String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {
        
        Pageable pageable = searchService.createPageable(page, size, sortBy, sortDir);
        Page<GigSearchDTO> result = searchService.searchGigs(title, skills, location, minBudget, maxBudget, pageable);
        
        return ResponseEntity.ok(result);
    }
}
