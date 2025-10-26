package com.supwork.gig.service;

import com.supwork.gig.dto.*;
import com.supwork.gig.entity.Gig;
import com.supwork.gig.entity.GigStatus;
import com.supwork.gig.entity.Rating;
import com.supwork.gig.repository.GigRepository;
import com.supwork.gig.repository.RatingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service class for managing gig operations including creation, assignment,
 * rating, and retrieval of gigs in the SupWork platform.
 * 
 * This service provides comprehensive business logic for gig management,
 * ensuring proper validation, authorization, and data integrity.
 * 
 * @author SupWork Team
 * @version 1.0
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class GigService {
    
    // Dependencies
    private final GigRepository gigRepository;
    private final RatingRepository ratingRepository;
    private final UserClient userClient;
    
    /**
     * Creates a new gig in the system.
     * 
     * This method validates the request data, ensures the user is authorized
     * to create gigs (CLIENT role), and creates a new gig with OPEN status.
     * 
     * @param request the gig creation request
     * @param clientId the ID of the client creating the gig
     * @return GigResponseDTO containing the created gig information
     * @throws IllegalArgumentException if budget is invalid or user is not a client
     * @throws RuntimeException if gig creation fails
     */
    public GigResponseDTO createGig(CreateGigRequest request, Long clientId) {
        log.info("Creating gig for client ID: {}", clientId);
        
        // Validate request data
        validateGigRequest(request);
        
        // Verify client authorization
        validateClientAuthorization(clientId);
        
        try {
            // Create and save gig
            Gig gig = buildGigFromRequest(request, clientId);
            Gig savedGig = gigRepository.save(gig);
            
            log.info("Successfully created gig with ID: {}", savedGig.getId());
            return convertToResponseDTO(savedGig);
            
        } catch (Exception e) {
            log.error("Failed to create gig for client ID {}: {}", clientId, e.getMessage(), e);
            throw new RuntimeException("Gig creation failed", e);
        }
    }
    
    /**
     * Retrieves all open gigs with pagination.
     * 
     * @param pageable pagination parameters
     * @return Page of GigResponseDTO containing open gigs
     */
    @Transactional(readOnly = true)
    public Page<GigResponseDTO> getAllOpenGigs(Pageable pageable) {
        log.info("Fetching open gigs with pageable: {}", pageable);
        Page<Gig> gigs = gigRepository.findByStatus(GigStatus.OPEN, pageable);
        return gigs.map(this::convertToResponseDTO);
    }
    
    /**
     * Retrieves a specific gig by its ID.
     * 
     * @param id the gig ID
     * @return GigResponseDTO containing gig information
     * @throws RuntimeException if gig not found
     */
    @Transactional(readOnly = true)
    public GigResponseDTO getGigById(Long id) {
        log.info("Fetching gig with ID: {}", id);
        Gig gig = findGigById(id);
        return convertToResponseDTO(gig);
    }
    
    /**
     * Assigns a gig to a technician.
     * 
     * This method validates that the technician is authorized to be assigned
     * to gigs and that the gig is available for assignment.
     * 
     * @param gigId the ID of the gig to assign
     * @param technicianId the ID of the technician
     * @return GigResponseDTO with updated gig information
     * @throws IllegalArgumentException if technician is not authorized or gig is not available
     * @throws RuntimeException if gig not found
     */
    public GigResponseDTO assignGig(Long gigId, Long technicianId) {
        log.info("Assigning gig ID: {} to technician ID: {}", gigId, technicianId);
        
        // Validate technician authorization
        validateTechnicianAuthorization(technicianId);
        
        // Find and validate gig
        Gig gig = findGigById(gigId);
        validateGigForAssignment(gig);
        
        try {
            // Update gig assignment
            gig.setTechnicianId(technicianId);
            gig.setStatus(GigStatus.ASSIGNED);
            
            Gig savedGig = gigRepository.save(gig);
            log.info("Successfully assigned gig ID: {} to technician ID: {}", gigId, technicianId);
            
            return convertToResponseDTO(savedGig);
            
        } catch (Exception e) {
            log.error("Failed to assign gig ID {} to technician ID {}: {}", gigId, technicianId, e.getMessage(), e);
            throw new RuntimeException("Gig assignment failed", e);
        }
    }
    
    public void deleteGig(Long gigId, Long clientId) {
        log.info("Deleting gig ID: {} by client ID: {}", gigId, clientId);
        
        Gig gig = gigRepository.findById(gigId)
                .orElseThrow(() -> new RuntimeException("Gig not found with ID: " + gigId));
        
        if (!gig.getClientId().equals(clientId)) {
            throw new IllegalArgumentException("Only gig owner can delete the gig");
        }
        
        gigRepository.delete(gig);
        log.info("Deleted gig with ID: {}", gigId);
    }
    
    @Transactional(readOnly = true)
    public Page<GigResponseDTO> getMyGigs(Long userId, String role, Pageable pageable) {
        log.info("Fetching gigs for user ID: {}, role: {}", userId, role);
        
        Page<Gig> gigs;
        if ("CLIENT".equalsIgnoreCase(role)) {
            gigs = gigRepository.findByClientId(userId, pageable);
        } else if ("TECHNICIAN".equalsIgnoreCase(role)) {
            gigs = gigRepository.findByTechnicianId(userId, pageable);
        } else {
            throw new IllegalArgumentException("Invalid role: " + role);
        }
        
        return gigs.map(this::convertToResponseDTO);
    }
    
    @Transactional
    public RatingDTO createRating(Long gigId, Long clientId, CreateRatingRequest request) {
        log.info("Creating rating for gig ID: {} by client ID: {}", gigId, clientId);
        
        // Check if gig exists and belongs to client
        Gig gig = gigRepository.findById(gigId)
                .orElseThrow(() -> new RuntimeException("Gig not found with ID: " + gigId));
        
        if (!gig.getClientId().equals(clientId)) {
            throw new IllegalArgumentException("Only gig owner can rate the gig");
        }
        
        if (gig.getTechnicianId() == null) {
            throw new IllegalArgumentException("Cannot rate gig that is not assigned to a technician");
        }
        
        // Check if rating already exists
        if (ratingRepository.existsByGigIdAndClientId(gigId, clientId)) {
            throw new IllegalArgumentException("Rating already exists for this gig");
        }
        
        Rating rating = Rating.builder()
                .gigId(gigId)
                .clientId(clientId)
                .technicianId(gig.getTechnicianId())
                .rating(request.getRating())
                .comment(request.getComment())
                .createdAt(LocalDateTime.now())
                .build();
        
        Rating savedRating = ratingRepository.save(rating);
        log.info("Created rating with ID: {}", savedRating.getId());
        
        return convertToRatingDTO(savedRating);
    }
    
    @Transactional(readOnly = true)
    public List<RatingDTO> getClientRatings(Long clientId) {
        log.info("Fetching ratings for client ID: {}", clientId);
        List<Rating> ratings = ratingRepository.findByClientId(clientId);
        return ratings.stream().map(this::convertToRatingDTO).toList();
    }
    
    @Transactional(readOnly = true)
    public List<RatingDTO> getTechnicianRatings(Long technicianId) {
        log.info("Fetching ratings for technician ID: {}", technicianId);
        List<Rating> ratings = ratingRepository.findByTechnicianId(technicianId);
        return ratings.stream().map(this::convertToRatingDTO).toList();
    }
    
    private RatingDTO convertToRatingDTO(Rating rating) {
        return RatingDTO.builder()
                .id(rating.getId())
                .gigId(rating.getGigId())
                .clientId(rating.getClientId())
                .technicianId(rating.getTechnicianId())
                .rating(rating.getRating())
                .comment(rating.getComment())
                .createdAt(rating.getCreatedAt())
                .build();
    }
    
    // ==================== PRIVATE HELPER METHODS ====================

    /**
     * Validates gig creation request data.
     * 
     * @param request the gig creation request
     * @throws IllegalArgumentException if validation fails
     */
    private void validateGigRequest(CreateGigRequest request) {
        if (request.getBudget() <= 0) {
            throw new IllegalArgumentException("Budget must be positive");
        }
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Title is required");
        }
        if (request.getDescription() == null || request.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("Description is required");
        }
        if (request.getLocation() == null || request.getLocation().trim().isEmpty()) {
            throw new IllegalArgumentException("Location is required");
        }
    }

    /**
     * Validates that the user is authorized to create gigs (CLIENT role).
     * 
     * @param clientId the user ID to validate
     * @throws IllegalArgumentException if user is not a client
     */
    private void validateClientAuthorization(Long clientId) {
        try {
            UserProfileDTO clientProfile = userClient.getProfile(clientId);
            String userRole = clientProfile.getRole();
            
            if (userRole == null || !"CLIENT".equalsIgnoreCase(userRole)) {
                log.warn("User {} has role '{}', expected 'CLIENT'", clientId, userRole);
                throw new IllegalArgumentException("Only clients can create gigs");
            }
        } catch (Exception e) {
            log.error("Failed to validate client authorization for user {}: {}", clientId, e.getMessage());
            throw new IllegalArgumentException("Unable to verify user authorization", e);
        }
    }

    /**
     * Validates that the user is authorized to be assigned to gigs (TECHNICIAN role).
     * 
     * @param technicianId the user ID to validate
     * @throws IllegalArgumentException if user is not a technician
     */
    private void validateTechnicianAuthorization(Long technicianId) {
        try {
            UserProfileDTO technicianProfile = userClient.getProfile(technicianId);
            String techRole = technicianProfile.getRole();
            
            if (techRole == null || !"TECHNICIAN".equalsIgnoreCase(techRole)) {
                log.warn("User {} has role '{}', expected 'TECHNICIAN'", technicianId, techRole);
                throw new IllegalArgumentException("Only technicians can be assigned to gigs");
            }
        } catch (Exception e) {
            log.error("Failed to validate technician authorization for user {}: {}", technicianId, e.getMessage());
            throw new IllegalArgumentException("Unable to verify technician authorization", e);
        }
    }

    /**
     * Validates that a gig is available for assignment.
     * 
     * @param gig the gig to validate
     * @throws IllegalArgumentException if gig is not available
     */
    private void validateGigForAssignment(Gig gig) {
        if (gig.getStatus() != GigStatus.OPEN) {
            throw new IllegalArgumentException("Gig is not available for assignment");
        }
        if (gig.getTechnicianId() != null) {
            throw new IllegalArgumentException("Gig is already assigned to a technician");
        }
    }

    /**
     * Finds a gig by ID.
     * 
     * @param gigId the gig ID
     * @return Gig entity
     * @throws RuntimeException if gig not found
     */
    private Gig findGigById(Long gigId) {
        return gigRepository.findById(gigId)
                .orElseThrow(() -> new RuntimeException("Gig not found with ID: " + gigId));
    }

    /**
     * Builds a Gig entity from CreateGigRequest.
     * 
     * @param request the gig creation request
     * @param clientId the client ID
     * @return Gig entity
     */
    private Gig buildGigFromRequest(CreateGigRequest request, Long clientId) {
        return Gig.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .budget(request.getBudget())
                .location(request.getLocation())
                .status(GigStatus.OPEN)
                .clientId(clientId)
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * Converts Gig entity to GigResponseDTO.
     * 
     * @param gig the gig entity
     * @return GigResponseDTO with all relevant information
     */
    private GigResponseDTO convertToResponseDTO(Gig gig) {
        String technicianEmail = getTechnicianEmail(gig.getTechnicianId());
        RatingInfo ratingInfo = getRatingInfo(gig);
        
        return GigResponseDTO.builder()
                .id(gig.getId())
                .title(gig.getTitle())
                .description(gig.getDescription())
                .budget(gig.getBudget())
                .location(gig.getLocation())
                .status(gig.getStatus())
                .clientId(gig.getClientId())
                .technicianId(gig.getTechnicianId())
                .technicianEmail(technicianEmail)
                .createdAt(gig.getCreatedAt())
                .canRate(ratingInfo.canRate)
                .isRated(ratingInfo.isRated)
                .build();
    }

    /**
     * Gets technician email if technician is assigned.
     * 
     * @param technicianId the technician ID
     * @return technician email or null if not found
     */
    private String getTechnicianEmail(Long technicianId) {
        if (technicianId == null) {
            return null;
        }
        
        try {
            UserProfileDTO technicianProfile = userClient.getProfile(technicianId);
            return technicianProfile.getEmail();
        } catch (Exception e) {
            log.warn("Failed to get technician profile for ID: {}", technicianId);
            return null;
        }
    }

    /**
     * Gets rating information for a gig.
     * 
     * @param gig the gig entity
     * @return RatingInfo containing rating status
     */
    private RatingInfo getRatingInfo(Gig gig) {
        boolean isRated = false;
        boolean canRate = false;
        
        if (gig.getTechnicianId() != null) {
            isRated = ratingRepository.existsByGigIdAndClientId(gig.getId(), gig.getClientId());
            canRate = !isRated; // Can rate if not already rated
        }
        
        return new RatingInfo(canRate, isRated);
    }

    /**
     * Converts Rating entity to RatingDTO.
     * 
     * @param rating the rating entity
     * @return RatingDTO with rating information
     */
    private RatingDTO convertToRatingDTO(Rating rating) {
        return RatingDTO.builder()
                .id(rating.getId())
                .gigId(rating.getGigId())
                .clientId(rating.getClientId())
                .technicianId(rating.getTechnicianId())
                .rating(rating.getRating())
                .comment(rating.getComment())
                .createdAt(rating.getCreatedAt())
                .build();
    }

    /**
     * Inner class to hold rating information.
     */
    private static class RatingInfo {
        final boolean canRate;
        final boolean isRated;
        
        RatingInfo(boolean canRate, boolean isRated) {
            this.canRate = canRate;
            this.isRated = isRated;
        }
    }
}
