package com.borsibaar.controller;

import com.borsibaar.dto.SaleItemRequestDto;
import com.borsibaar.dto.SaleItemResponseDto;
import com.borsibaar.dto.SaleRequestDto;
import com.borsibaar.dto.SaleResponseDto;
import com.borsibaar.entity.Role;
import com.borsibaar.entity.User;
import com.borsibaar.service.SalesService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class SalesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SalesService salesService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void processSale_ReturnsCreatedResponse() throws Exception {
        User user = userWithOrg(1L, "USER");
        setAuth(user);

        SaleRequestDto req = new SaleRequestDto(List.of(new SaleItemRequestDto(10L, new BigDecimal("2"))), "note", 5L);
        SaleItemResponseDto itemResp = new SaleItemResponseDto(10L, "Cola", new BigDecimal("2"), new BigDecimal("3.00"), new BigDecimal("6.00"));
        SaleResponseDto resp = new SaleResponseDto("SALE-1", List.of(itemResp), new BigDecimal("6.00"), "note", OffsetDateTime.now());
        when(salesService.processSale(any(SaleRequestDto.class), any(UUID.class), anyLong())).thenReturn(resp);

        mockMvc.perform(post("/api/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.saleId").value("SALE-1"))
                .andExpect(jsonPath("$.items[0].productName").value("Cola"))
                .andExpect(jsonPath("$.totalAmount").value(6.00));

        verify(salesService).processSale(any(SaleRequestDto.class), any(UUID.class), anyLong());
    }

    private static User userWithOrg(Long orgId, String roleName) {
        Role role = new Role();
        role.setId(1L);
        role.setName(roleName);
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("user@test.com");
        user.setName("Test User");
        user.setOrganizationId(orgId);
        user.setRole(role);
        return user;
    }

    private static void setAuth(User user) {
        Authentication auth = new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
