package com.borsibaar.controller;

import com.borsibaar.dto.UserDTO;
import com.borsibaar.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {"app.frontend.url=http://localhost:3000"})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Test
    void loginSuccess_SetsCookie_AndRedirectsToOnboarding() throws Exception {
        // Arrange
        UserDTO dto = new UserDTO("user@test.com", "User", "token-123", null);
        AuthService.AuthResult result = new AuthService.AuthResult(dto, true);
        when(authService.processOAuthLogin(any(OAuth2AuthenticationToken.class))).thenReturn(result);

        DefaultOAuth2User oAuth2User = new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                Map.of("email", "user@test.com", "name", "User"),
                "email");
        OAuth2AuthenticationToken authToken = new OAuth2AuthenticationToken(oAuth2User, oAuth2User.getAuthorities(), "google");

        // Act & Assert
        mockMvc.perform(get("/auth/login/success").principal(authToken))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost:3000/onboarding"))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.containsString("jwt=")))
                .andExpect(cookie().exists("jwt"));
    }

    @Test
    void logout_ClearsCookie_AndReturnsOk() throws Exception {
        mockMvc.perform(post("/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(cookie().maxAge("jwt", 0));
    }
}
