package com.supwork.gig.repository;

import com.supwork.gig.entity.Gig;
import com.supwork.gig.entity.GigStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GigRepository extends JpaRepository<Gig, Long> {
    
    List<Gig> findByClientId(Long clientId);
    
    Page<Gig> findByStatus(GigStatus status, Pageable pageable);
    
    List<Gig> findByClientIdAndStatus(Long clientId, GigStatus status);
    
    Page<Gig> findByTechnicianId(Long technicianId, Pageable pageable);
    
    Page<Gig> findByClientId(Long clientId, Pageable pageable);
}
