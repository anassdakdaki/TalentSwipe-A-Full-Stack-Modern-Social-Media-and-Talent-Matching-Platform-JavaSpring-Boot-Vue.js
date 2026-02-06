package com.example.biblov1.testsupport;

import com.example.biblov1.payload.response.JwtResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public final class TestAuthSupport {
    private TestAuthSupport() {}

    public record TestUser(Long id, String email, String token) {}

    public static void register(MockMvc mockMvc, ObjectMapper objectMapper, String name, String email, String password) throws Exception {
        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(Map.of(
                                        "name", name,
                                        "email", email,
                                        "password", password
                                )))
                )
                .andExpect(status().isOk());
    }

    public static TestUser registerAndLogin(MockMvc mockMvc, ObjectMapper objectMapper, String name, String email, String password) throws Exception {
        register(mockMvc, objectMapper, name, email, password);
        return login(mockMvc, objectMapper, email, password);
    }

    public static TestUser login(MockMvc mockMvc, ObjectMapper objectMapper, String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(Map.of(
                                        "email", email,
                                        "password", password
                                )))
                )
                .andExpect(status().isOk())
                .andReturn();

        JwtResponse jwtResponse = objectMapper.readValue(result.getResponse().getContentAsString(), JwtResponse.class);
        return new TestUser(jwtResponse.getId(), jwtResponse.getEmail(), jwtResponse.getToken());
    }
}

