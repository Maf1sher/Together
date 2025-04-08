package org.mafisher.togetherbackend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.mafisher.togetherbackend.controller.AuthController;
import org.mafisher.togetherbackend.dto.UserDto;
import org.mafisher.togetherbackend.dto.request.LoginRequest;
import org.mafisher.togetherbackend.dto.request.RegisterRequest;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @Mock
    private CookieService cookieService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void register_ShouldReturnCreated() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "John", "Doe", "johndoe", "john@example.com", "password123"
        );

        UserDto userDto = UserDto.builder()
                .id(1L)
                .email("john@example.com")
                .build();

        when(authService.createUser(any(RegisterRequest.class))).thenReturn(userDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    void login_ShouldSetCookieAndReturnUser() throws Exception {
        LoginRequest request = new LoginRequest("john@example.com", "password123");
        UserDto userDto = UserDto.builder().email("john@example.com").build();

        when(authService.verify(any(LoginRequest.class))).thenReturn("jwt-token");
        when(authService.findUserByEmail("john@example.com")).thenReturn(userDto);
        when(cookieService.getNewCookie("jwt", "jwt-token")).thenReturn(new Cookie("jwt", "jwt-token"));

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("jwt"))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    void logout_ShouldDeleteCookie() throws Exception {
        when(cookieService.deleteCookie("jwt")).thenReturn(new Cookie("jwt", ""));

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(cookie().value("jwt", ""));
    }

    @Test
    void check_ShouldReturnUserForValidToken() throws Exception {
        UserDto userDto = UserDto.builder().email("john@example.com").build();

        when(cookieService.getJwtCookie(any(HttpServletRequest.class))).thenReturn("valid-token");
        when(authService.verifyToken("valid-token")).thenReturn(userDto);

        mockMvc.perform(MockMvcRequestBuilders.get("/auth/check"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }
}
