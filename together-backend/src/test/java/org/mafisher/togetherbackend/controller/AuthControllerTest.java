package org.mafisher.togetherbackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mafisher.togetherbackend.config.BeansConfig;
import org.mafisher.togetherbackend.config.JwtFilter;
import org.mafisher.togetherbackend.dto.UserDto;
import org.mafisher.togetherbackend.dto.request.LoginRequest;
import org.mafisher.togetherbackend.dto.request.RegisterRequest;
import org.mafisher.togetherbackend.entity.Role;
import org.mafisher.togetherbackend.service.AuthService;
import org.mafisher.togetherbackend.service.CookieService;
import org.mafisher.togetherbackend.service.JwtService;
import org.mafisher.togetherbackend.service.impl.UserDetailsService;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Collections;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
@Import({JwtFilter.class, BeansConfig.class})
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private CookieService cookieService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private final ObjectMapper objectMapper = new ObjectMapper();


    @Test
    void register_ShouldReturnCreated() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "John", "Doe", "johndoe", "john@example.com", "password123"
        );

        UserDto userDto = UserDto.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .nickName("johndoe")
                .email("john@example.com")
                .roles(Collections.emptySet())
                .build();

        when(authService.createUser(any())).thenReturn(userDto);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.nickName").value("johndoe"))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(status().isCreated());
    }

    @Test
    void login_ShouldSetCookieAndReturnUser() throws Exception {
        LoginRequest request = new LoginRequest("john@example.com", "password123");

        UserDto userDto = UserDto.builder()
                .email("john@example.com")
                .roles(Set.of(new Role(1L, "USER")))
                .build();

        when(authService.verify(any())).thenReturn("jwt-token");
        when(authService.findUserByEmail(any())).thenReturn(userDto);
        when(cookieService.getNewCookie(any(), any())).thenReturn(new Cookie("jwt", "jwt-token"));

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(cookie().exists("jwt"))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    void logout_ShouldRemoveCookie() throws Exception {
        when(cookieService.deleteCookie(any())).thenReturn(new Cookie("jwt", ""));

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(cookie().value("jwt", ""));
    }

    @Test
    void activateAccount_ShouldRedirect() throws Exception {
        doNothing().when(authService).activateUser(any(), any());

        mockMvc.perform(MockMvcRequestBuilders.get("/auth/active/1/activation-token"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost:5173"));
    }

    @Test
    void checkAuth_ShouldReturnUser() throws Exception {
        UserDto userDto = UserDto.builder()
                .email("john@example.com")
                .roles(Set.of(new Role(1L, "USER")))
                .build();

        when(cookieService.getJwtCookie(any())).thenReturn("valid-jwt");
        when(authService.verifyToken(any())).thenReturn(userDto);

        mockMvc.perform(MockMvcRequestBuilders.get("/auth/check"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }
}


