package com.supwork.search.controller;

import com.supwork.search.model.MatchResult;
import com.supwork.search.service.MatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/match")
@RequiredArgsConstructor
@Tag(name = "Matching", description = "Matching operations for gigs and technicians")
public class MatchController {
    
    private final MatchService matchService;
    
    @GetMapping("/{gigId}")
    @Operation(summary = "Find matches for a gig", description = "Find top 5 technicians that match a specific gig")
    public ResponseEntity<List<MatchResult>> findMatchesForGig(
            @Parameter(description = "Gig ID to find matches for") @PathVariable Long gigId) {
        
        List<MatchResult> matches = matchService.findMatchesForGig(gigId);
        return ResponseEntity.ok(matches);
    }
    
    @GetMapping("/technician/{technicianId}")
    @Operation(summary = "Find matches for a technician", description = "Find top 5 gigs that match a specific technician")
    public ResponseEntity<List<MatchResult>> findMatchesForTechnician(
            @Parameter(description = "Technician ID to find matches for") @PathVariable Long technicianId) {
        
        List<MatchResult> matches = matchService.findMatchesForTechnician(technicianId);
        return ResponseEntity.ok(matches);
    }
}
