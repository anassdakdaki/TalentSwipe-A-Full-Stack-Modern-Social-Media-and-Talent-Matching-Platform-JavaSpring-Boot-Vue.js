package com.example.biblov1;

import com.example.biblov1.model.StudyMatch;
import com.example.biblov1.repository.StudyMatchRepository;
import com.example.biblov1.repository.UserSwipeRepository;
import com.example.biblov1.testsupport.TestAuthSupport;
import com.example.biblov1.testsupport.TestDatabaseCleanup;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MatchingIntegrationTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private TestDatabaseCleanup databaseCleanup;
    @Autowired private StudyMatchRepository studyMatchRepository;
    @Autowired private UserSwipeRepository userSwipeRepository;

    @BeforeEach
    void resetDb() {
        databaseCleanup.reset();
    }

    @Test
    void should_create_match_only_on_mutual_like() throws Exception {
        TestAuthSupport.TestUser a = TestAuthSupport.registerAndLogin(mockMvc, objectMapper, "Alice", "a@example.com", "password123");
        TestAuthSupport.TestUser b = TestAuthSupport.registerAndLogin(mockMvc, objectMapper, "Bob", "b@example.com", "password123");

        mockMvc.perform(
                        post("/api/matches/swipe")
                                .contentType(APPLICATION_JSON)
                                .header("Authorization", "Bearer " + a.token())
                                .content(objectMapper.writeValueAsString(Map.of(
                                        "swipedUserId", b.id(),
                                        "swipeType", "LIKE"
                                )))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.match").value(false));

        mockMvc.perform(
                        post("/api/matches/swipe")
                                .contentType(APPLICATION_JSON)
                                .header("Authorization", "Bearer " + b.token())
                                .content(objectMapper.writeValueAsString(Map.of(
                                        "swipedUserId", a.id(),
                                        "swipeType", "LIKE"
                                )))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.match").value(true));

        List<StudyMatch> matches = studyMatchRepository.findAll();
        assertThat(matches).hasSize(1);

        StudyMatch match = matches.get(0);
        long minId = Math.min(a.id(), b.id());
        long maxId = Math.max(a.id(), b.id());
        assertThat(match.getUser1().getId()).isEqualTo(minId);
        assertThat(match.getUser2().getId()).isEqualTo(maxId);
        assertThat(match.getStatus()).isEqualTo(StudyMatch.MatchStatus.MATCHED);
    }

    @Test
    void should_not_create_duplicate_match_on_repeated_likes() throws Exception {
        TestAuthSupport.TestUser a = TestAuthSupport.registerAndLogin(mockMvc, objectMapper, "Alice", "a@example.com", "password123");
        TestAuthSupport.TestUser b = TestAuthSupport.registerAndLogin(mockMvc, objectMapper, "Bob", "b@example.com", "password123");

        for (int i = 0; i < 3; i++) {
            mockMvc.perform(
                            post("/api/matches/swipe")
                                    .contentType(APPLICATION_JSON)
                                    .header("Authorization", "Bearer " + a.token())
                                    .content(objectMapper.writeValueAsString(Map.of(
                                            "swipedUserId", b.id(),
                                            "swipeType", "LIKE"
                                    )))
                    )
                    .andExpect(status().isOk());

            mockMvc.perform(
                            post("/api/matches/swipe")
                                    .contentType(APPLICATION_JSON)
                                    .header("Authorization", "Bearer " + b.token())
                                    .content(objectMapper.writeValueAsString(Map.of(
                                            "swipedUserId", a.id(),
                                            "swipeType", "LIKE"
                                    )))
                    )
                    .andExpect(status().isOk());
        }

        assertThat(studyMatchRepository.count()).isEqualTo(1);
        assertThat(userSwipeRepository.count()).isEqualTo(2);
    }
}
