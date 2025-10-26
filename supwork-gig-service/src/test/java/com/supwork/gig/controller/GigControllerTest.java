package com.supwork.gig.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.supwork.gig.dto.CreateGigRequest;
import com.supwork.gig.dto.GigResponseDTO;
import com.supwork.gig.dto.UserProfileDTO;
import com.supwork.gig.entity.GigStatus;
import com.supwork.gig.service.GigService;
import com.supwork.gig.service.UserClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GigController.class)
class GigControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GigService gigService;

    @MockBean
    private UserClient userClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "CLIENT")
    void createGig_ShouldReturnCreatedGig() throws Exception {
        // Given
        CreateGigRequest request = CreateGigRequest.builder()
                .title("Test Gig")
                .description("Test Description")
                .budget(100.0)
                .location("Test Location")
                .build();

        GigResponseDTO response = GigResponseDTO.builder()
                .id(1L)
                .title("Test Gig")
                .description("Test Description")
                .budget(100.0)
                .location("Test Location")
                .status(GigStatus.OPEN)
                .clientId(1L)
                .createdAt(LocalDateTime.now())
                .build();

        when(gigService.createGig(any(CreateGigRequest.class), eq(1L)))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(post("/gigs")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Gig"))
                .andExpect(jsonPath("$.budget").value(100.0))
                .andExpect(jsonPath("$.status").value("OPEN"));
    }
}
