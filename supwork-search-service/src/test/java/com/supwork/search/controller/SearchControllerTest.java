package com.supwork.search.controller;

import com.supwork.search.model.TechnicianProfile;
import com.supwork.search.service.SearchService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SearchController.class)
class SearchControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private SearchService searchService;
    
    @Test
    void searchTechnicians_ShouldReturnPageOfTechnicians() throws Exception {
        // Given
        TechnicianProfile technician1 = TechnicianProfile.builder()
                .userId(1L)
                .name("John Doe")
                .skills(Arrays.asList("Java", "Spring"))
                .rating(4.5)
                .isAvailable(true)
                .build();
        
        TechnicianProfile technician2 = TechnicianProfile.builder()
                .userId(2L)
                .name("Jane Smith")
                .skills(Arrays.asList("Python", "Django"))
                .rating(4.2)
                .isAvailable(true)
                .build();
        
        List<TechnicianProfile> technicians = Arrays.asList(technician1, technician2);
        Page<TechnicianProfile> page = new PageImpl<>(technicians, PageRequest.of(0, 10), 2);
        
        when(searchService.searchTechnicians(any(), any(), any(), any(), any()))
                .thenReturn(page);
        when(searchService.createPageable(anyInt(), anyInt(), any(), any()))
                .thenReturn(PageRequest.of(0, 10));
        
        // When & Then
        mockMvc.perform(get("/search/technicians")
                .param("skills", "Java")
                .param("minRating", "4.0")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2));
    }
    
    @Test
    void searchTechnicians_WithNoFilters_ShouldReturnAllTechnicians() throws Exception {
        // Given
        when(searchService.searchTechnicians(any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(Arrays.asList()));
        when(searchService.createPageable(anyInt(), anyInt(), any(), any()))
                .thenReturn(PageRequest.of(0, 10));
        
        // When & Then
        mockMvc.perform(get("/search/technicians"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }
}
