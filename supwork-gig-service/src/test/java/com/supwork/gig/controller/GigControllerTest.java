package com.supwork.gig.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.supwork.gig.dto.CreateGigRequest;
import com.supwork.gig.entity.Gig;
import com.supwork.gig.entity.GigStatus;
import com.supwork.gig.repository.GigRepository;
import com.supwork.gig.repository.RatingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class GigControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private GigRepository gigRepository;

    @Autowired
    private RatingRepository ratingRepository;

    @BeforeEach
    void setUp() {
        gigRepository.deleteAll();
        ratingRepository.deleteAll();
    }

    @Test
    void testGetAllOpenGigs_Success() throws Exception {
        // Create test gigs
        Gig gig1 = new Gig();
        gig1.setTitle("Fix leaky faucet");
        gig1.setDescription("Water dripping from kitchen faucet");
        gig1.setBudget(150.0);
        gig1.setLocation("Queens, NY");
        gig1.setStatus(GigStatus.OPEN);
        gig1.setClientId(1L);
        gig1.setCreatedAt(LocalDateTime.now());
        gigRepository.save(gig1);

        Gig gig2 = new Gig();
        gig2.setTitle("Install ceiling fan");
        gig2.setDescription("Need help installing ceiling fan");
        gig2.setBudget(200.0);
        gig2.setLocation("Brooklyn, NY");
        gig2.setStatus(GigStatus.OPEN);
        gig2.setClientId(2L);
        gig2.setCreatedAt(LocalDateTime.now());
        gigRepository.save(gig2);

        mockMvc.perform(get("/gigs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].title").value("Fix leaky faucet"))
                .andExpect(jsonPath("$.content[1].title").value("Install ceiling fan"));
    }

    @Test
    @WithMockUser(username = "1", roles = "CLIENT")
    void testCreateGig_Success() throws Exception {
        CreateGigRequest request = new CreateGigRequest();
        request.setTitle("Fix broken door");
        request.setDescription("Door handle is broken");
        request.setBudget(100.0);
        request.setLocation("Manhattan, NY");

        mockMvc.perform(post("/gigs")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Fix broken door"))
                .andExpect(jsonPath("$.budget").value(100.0))
                .andExpect(jsonPath("$.status").value("OPEN"));
    }

    @Test
    @WithMockUser(username = "1", roles = "CLIENT")
    void testCreateGig_InvalidBudget() throws Exception {
        CreateGigRequest request = new CreateGigRequest();
        request.setTitle("Fix broken door");
        request.setDescription("Door handle is broken");
        request.setBudget(-50.0); // Invalid negative budget
        request.setLocation("Manhattan, NY");

        mockMvc.perform(post("/gigs")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetGigById_Success() throws Exception {
        Gig gig = new Gig();
        gig.setTitle("Test gig");
        gig.setDescription("Test description");
        gig.setBudget(100.0);
        gig.setLocation("Test location");
        gig.setStatus(GigStatus.OPEN);
        gig.setClientId(1L);
        gig.setCreatedAt(LocalDateTime.now());
        Gig savedGig = gigRepository.save(gig);

        mockMvc.perform(get("/gigs/{id}", savedGig.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedGig.getId()))
                .andExpect(jsonPath("$.title").value("Test gig"))
                .andExpect(jsonPath("$.status").value("OPEN"));
    }

    @Test
    void testGetGigById_NotFound() throws Exception {
        mockMvc.perform(get("/gigs/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "1", roles = "CLIENT")
    void testGetMyGigs_Success() throws Exception {
        // Create gig for user 1
        Gig gig = new Gig();
        gig.setTitle("My gig");
        gig.setDescription("My description");
        gig.setBudget(100.0);
        gig.setLocation("My location");
        gig.setStatus(GigStatus.OPEN);
        gig.setClientId(1L);
        gig.setCreatedAt(LocalDateTime.now());
        gigRepository.save(gig);

        mockMvc.perform(get("/gigs/my-gigs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].title").value("My gig"));
    }
}