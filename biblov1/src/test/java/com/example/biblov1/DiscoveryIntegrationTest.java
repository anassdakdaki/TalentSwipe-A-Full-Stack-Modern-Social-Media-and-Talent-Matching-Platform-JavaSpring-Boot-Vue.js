package com.example.biblov1;

import com.example.biblov1.model.UserProfile;
import com.example.biblov1.repository.UserProfileRepository;
import com.example.biblov1.testsupport.TestAuthSupport;
import com.example.biblov1.testsupport.TestDatabaseCleanup;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DiscoveryIntegrationTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private TestDatabaseCleanup databaseCleanup;
    @Autowired private UserProfileRepository userProfileRepository;

    @BeforeEach
    void resetDb() {
        databaseCleanup.reset();
    }

    @Test
    void should_only_return_profiles_with_non_empty_name_and_major() throws Exception {
        TestAuthSupport.TestUser viewer = TestAuthSupport.registerAndLogin(
                mockMvc, objectMapper, "Viewer", "viewer-discovery@example.com", "password123"
        );
        TestAuthSupport.TestUser complete = TestAuthSupport.registerAndLogin(
                mockMvc, objectMapper, "Complete", "complete-discovery@example.com", "password123"
        );
        TestAuthSupport.TestUser incomplete = TestAuthSupport.registerAndLogin(
                mockMvc, objectMapper, "Incomplete", "incomplete-discovery@example.com", "password123"
        );

        UserProfile completeProfile = userProfileRepository.findByUserId(complete.id()).orElseThrow();
        completeProfile.setMajor("Computer Science");
        completeProfile.setBio("I enjoy building community projects and pairing for study sessions.");
        completeProfile.setInterests(new ArrayList<>(List.of("AI", "Coding")));
        userProfileRepository.save(completeProfile);

        UserProfile incompleteProfile = userProfileRepository.findByUserId(incomplete.id()).orElseThrow();
        incompleteProfile.setMajor("");
        incompleteProfile.setBio("");
        incompleteProfile.setInterests(new ArrayList<>(List.of()));
        userProfileRepository.save(incompleteProfile);

        MvcResult result = mockMvc.perform(
                        get("/api/profiles/discover")
                                .header("Authorization", "Bearer " + viewer.token())
                )
                .andExpect(status().isOk())
                .andReturn();

        JsonNode payload = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(payload.isArray()).isTrue();
        assertThat(payload).hasSize(1);

        Long returnedUserId = extractUserId(payload.get(0));
        assertThat(returnedUserId).isEqualTo(complete.id());
    }

    private Long extractUserId(JsonNode profileNode) {
        if (profileNode == null || profileNode.isNull()) {
            return null;
        }
        JsonNode nestedUser = profileNode.get("user");
        if (nestedUser != null && nestedUser.hasNonNull("id")) {
            return nestedUser.get("id").asLong();
        }
        if (profileNode.hasNonNull("userId")) {
            return profileNode.get("userId").asLong();
        }
        if (profileNode.hasNonNull("id")) {
            return profileNode.get("id").asLong();
        }
        return null;
    }
}
