package com.borsibaar.controller;

import com.borsibaar.dto.CategoryRequestDto;
import com.borsibaar.dto.CategoryResponseDto;
import com.borsibaar.entity.Role;
import com.borsibaar.entity.User;
import com.borsibaar.service.CategoryService;
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

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CategoryService categoryService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createCategory_ReturnsCreated() throws Exception {
        User user = userWithOrg(1L, "ADMIN");
        setAuth(user);

        CategoryRequestDto req = new CategoryRequestDto("Beers", true);
        CategoryResponseDto resp = new CategoryResponseDto(1L, "Beers", true);
        when(categoryService.create(any(CategoryRequestDto.class), anyLong())).thenReturn(resp);

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Beers"));

        verify(categoryService).create(any(CategoryRequestDto.class), anyLong());
    }

    @Test
    void getAll_UsesUserOrg_WhenNoQueryParam() throws Exception {
        User user = userWithOrg(5L, "USER");
        setAuth(user);

        when(categoryService.getAllByOrg(5L)).thenReturn(List.of());
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk());

        verify(categoryService).getAllByOrg(5L);
    }

    @Test
    void getById_ReturnsDto() throws Exception {
        User user = userWithOrg(2L, "USER");
        setAuth(user);

        CategoryResponseDto resp = new CategoryResponseDto(10L, "Wine", true);
        when(categoryService.getByIdAndOrg(10L, 2L)).thenReturn(resp);

        mockMvc.perform(get("/api/categories/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("Wine"));

        verify(categoryService).getByIdAndOrg(10L, 2L);
    }

    @Test
    void delete_ReturnsNoContent() throws Exception {
        User user = userWithOrg(3L, "USER");
        setAuth(user);

        mockMvc.perform(delete("/api/categories/7"))
                .andExpect(status().isNoContent());

        verify(categoryService).deleteReturningDto(7L, 3L);
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
