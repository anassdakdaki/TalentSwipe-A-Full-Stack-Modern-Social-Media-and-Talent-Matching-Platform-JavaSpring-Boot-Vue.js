package com.example.biblov1;

import com.example.biblov1.testsupport.TestAuthSupport;
import com.example.biblov1.testsupport.TestDatabaseCleanup;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthIntegrationTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private TestDatabaseCleanup databaseCleanup;

    @BeforeEach
    void resetDb() {
        databaseCleanup.reset();
    }

    @Test
    void should_allow_public_register_and_login() throws Exception {
        TestAuthSupport.register(mockMvc, objectMapper, "Alice", "alice@example.com", "password123");

        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(java.util.Map.of(
                                        "email", "alice@example.com",
                                        "password", "password123"
                                )))
                )
                .andExpect(status().isOk());
    }

    @Test
    void should_reject_protected_endpoint_without_token() throws Exception {
        mockMvc.perform(get("/api/communities"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void should_allow_protected_endpoint_with_valid_token() throws Exception {
        TestAuthSupport.TestUser user = TestAuthSupport.registerAndLogin(
                mockMvc, objectMapper, "Alice", "alice@example.com", "password123"
        );

        mockMvc.perform(
                        get("/api/communities")
                                .header("Authorization", "Bearer " + user.token())
                )
                .andExpect(status().isOk());
    }

    @Test
    void should_reject_request_with_invalid_token() throws Exception {
        mockMvc.perform(
                        get("/api/communities")
                                .header("Authorization", "Bearer not-a-valid-jwt")
                )
                .andExpect(status().isUnauthorized());
    }
}

