package com.supwork.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.supwork.user.dto.JwtResponse;
import com.supwork.user.dto.LoginRequest;
import com.supwork.user.dto.RefreshTokenRequest;
import com.supwork.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private JwtResponse jwtResponse;

    @BeforeEach
    void setUp() {
        jwtResponse = JwtResponse.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .tokenType("Bearer")
                .userId(1L)
                .email("test@example.com")
                .role("CLIENT")
                .build();
    }

    @Test
    @WithMockUser
    void testLogin_Success() throws Exception {
        LoginRequest loginRequest = new LoginRequest("test@example.com", "password123");
        
        when(userService.login(any(LoginRequest.class))).thenReturn(jwtResponse);

        mockMvc.perform(post("/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.role").value("CLIENT"));
    }

    @Test
    @WithMockUser
    void testRefresh_Success() throws Exception {
        RefreshTokenRequest refreshTokenRequest = new RefreshTokenRequest("refresh-token");
        
        when(userService.refreshToken(any(RefreshTokenRequest.class))).thenReturn(jwtResponse);

        mockMvc.perform(post("/auth/refresh")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshTokenRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.userId").value(1));
    }

}

