package com.supwork.gig.service;

import com.supwork.gig.dto.CreateGigRequest;
import com.supwork.gig.dto.GigDTO;
import com.supwork.gig.dto.GigResponseDTO;
import com.supwork.gig.dto.UserProfileDTO;
import com.supwork.gig.entity.Gig;
import com.supwork.gig.entity.GigStatus;
import com.supwork.gig.repository.GigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class GigService {
    
    private final GigRepository gigRepository;
    private final UserClient userClient;
    
    public GigResponseDTO createGig(CreateGigRequest request, Long clientId) {
        log.info("Creating gig for client ID: {}", clientId);
        
        // Validate budget and location
        if (request.getBudget() <= 0) {
            throw new IllegalArgumentException("Budget must be positive");
        }
        
        // Check if client exists and has CLIENT role
        UserProfileDTO clientProfile = userClient.getProfile(clientId);
        if (!"CLIENT".equals(clientProfile.getRole())) {
            throw new IllegalArgumentException("Only clients can create gigs");
        }
        
        Gig gig = Gig.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .budget(request.getBudget())
                .location(request.getLocation())
                .status(GigStatus.OPEN)
                .clientId(clientId)
                .createdAt(LocalDateTime.now())
                .build();
        
        Gig savedGig = gigRepository.save(gig);
        log.info("Created gig with ID: {}", savedGig.getId());
        
        return convertToResponseDTO(savedGig);
    }
    
    @Transactional(readOnly = true)
    public Page<GigResponseDTO> getAllOpenGigs(Pageable pageable) {
        log.info("Fetching open gigs with pageable: {}", pageable);
        Page<Gig> gigs = gigRepository.findByStatus(GigStatus.OPEN, pageable);
        return gigs.map(this::convertToResponseDTO);
    }
    
    @Transactional(readOnly = true)
    public GigResponseDTO getGigById(Long id) {
        log.info("Fetching gig with ID: {}", id);
        Gig gig = gigRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Gig not found with ID: " + id));
        return convertToResponseDTO(gig);
    }
    
    public GigResponseDTO assignGig(Long gigId, Long technicianId) {
        log.info("Assigning gig ID: {} to technician ID: {}", gigId, technicianId);
        
        // Check if technician exists and has TECHNICIAN role
        UserProfileDTO technicianProfile = userClient.getProfile(technicianId);
        if (!"TECHNICIAN".equals(technicianProfile.getRole())) {
            throw new IllegalArgumentException("Only technicians can be assigned to gigs");
        }
        
        Gig gig = gigRepository.findById(gigId)
                .orElseThrow(() -> new RuntimeException("Gig not found with ID: " + gigId));
        
        if (gig.getStatus() != GigStatus.OPEN) {
            throw new IllegalArgumentException("Gig is not available for assignment");
        }
        
        gig.setTechnicianId(technicianId);
        gig.setStatus(GigStatus.ASSIGNED);
        
        Gig savedGig = gigRepository.save(gig);
        log.info("Assigned gig ID: {} to technician ID: {}", gigId, technicianId);
        
        return convertToResponseDTO(savedGig);
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
    
    private GigResponseDTO convertToResponseDTO(Gig gig) {
        return GigResponseDTO.builder()
                .id(gig.getId())
                .title(gig.getTitle())
                .description(gig.getDescription())
                .budget(gig.getBudget())
                .location(gig.getLocation())
                .status(gig.getStatus())
                .clientId(gig.getClientId())
                .technicianId(gig.getTechnicianId())
                .createdAt(gig.getCreatedAt())
                .build();
    }
}
