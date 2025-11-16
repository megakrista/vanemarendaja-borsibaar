package com.borsibaar.controller;

import com.borsibaar.dto.OrganizationRequestDto;
import com.borsibaar.dto.OrganizationResponseDto;
import com.borsibaar.service.OrganizationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class OrganizationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrganizationService organizationService;

    @Test
    void create_ReturnsCreated() throws Exception {
        OrganizationRequestDto req = new OrganizationRequestDto("Org");
        OrganizationResponseDto resp = new OrganizationResponseDto(1L, "Org", OffsetDateTime.now(), OffsetDateTime.now());
        when(organizationService.create(any(OrganizationRequestDto.class))).thenReturn(resp);

        mockMvc.perform(post("/api/organizations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Org"));

        verify(organizationService).create(any(OrganizationRequestDto.class));
    }

    @Test
    void get_ReturnsDto() throws Exception {
        OrganizationResponseDto resp = new OrganizationResponseDto(2L, "Org2", OffsetDateTime.now(), OffsetDateTime.now());
        when(organizationService.getById(2L)).thenReturn(resp);

        mockMvc.perform(get("/api/organizations/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.name").value("Org2"));

        verify(organizationService).getById(2L);
    }

    @Test
    void getAll_ReturnsList() throws Exception {
        OrganizationResponseDto resp1 = new OrganizationResponseDto(1L, "A", OffsetDateTime.now(), OffsetDateTime.now());
        OrganizationResponseDto resp2 = new OrganizationResponseDto(2L, "B", OffsetDateTime.now(), OffsetDateTime.now());
        when(organizationService.getAll()).thenReturn(List.of(resp1, resp2));

        mockMvc.perform(get("/api/organizations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        verify(organizationService).getAll();
    }
}
