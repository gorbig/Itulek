package com.supwork.search.repository;

import com.supwork.search.model.TechnicianProfile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TechnicianProfileRepository extends MongoRepository<TechnicianProfile, String> {
    
    @Query("{ 'skills': { $in: ?0 }, 'isAvailable': true }")
    List<TechnicianProfile> findBySkillsInAndAvailable(List<String> skills);
    
    @Query("{ 'rating': { $gte: ?0 }, 'isAvailable': true }")
    List<TechnicianProfile> findByRatingGreaterThanEqualAndAvailable(Double minRating);
    
    @Query("{ 'skills': { $in: ?0 }, 'rating': { $gte: ?1 }, 'isAvailable': true }")
    List<TechnicianProfile> findBySkillsInAndRatingGreaterThanEqualAndAvailable(List<String> skills, Double minRating);
    
    List<TechnicianProfile> findByIsAvailableTrue();
    
    TechnicianProfile findByUserId(Long userId);
}
