package com.example.biblov1;

import com.example.biblov1.model.StudyMatch;
import com.example.biblov1.model.UserSwipe;
import com.example.biblov1.repository.StudyMatchRepository;
import com.example.biblov1.repository.UserSwipeRepository;
import com.example.biblov1.service.BotIdentityService;
import com.example.biblov1.service.BotPopulationService;
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
class BotAutoMatchIntegrationTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private TestDatabaseCleanup databaseCleanup;
    @Autowired private BotPopulationService botPopulationService;
    @Autowired private BotIdentityService botIdentityService;
    @Autowired private UserSwipeRepository userSwipeRepository;
    @Autowired private StudyMatchRepository studyMatchRepository;

    @BeforeEach
    void resetDb() {
        databaseCleanup.reset();
    }

    @Test
    void should_auto_reciprocate_when_human_likes_bot_and_create_match() throws Exception {
        botPopulationService.seedBotProfilesAndCommunities();
        List<Long> botIds = botIdentityService.getActiveBotUserIds();
        assertThat(botIds).isNotEmpty();
        Long botId = botIds.get(0);

        TestAuthSupport.TestUser human = TestAuthSupport.registerAndLogin(
                mockMvc,
                objectMapper,
                "Human User",
                "human@example.com",
                "password123"
        );

        mockMvc.perform(
                        post("/api/matches/swipe")
                                .contentType(APPLICATION_JSON)
                                .header("Authorization", "Bearer " + human.token())
                                .content(objectMapper.writeValueAsString(Map.of(
                                        "swipedUserId", botId,
                                        "swipeType", "LIKE"
                                )))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.match").value(true));

        assertThat(userSwipeRepository.existsBySwiper_IdAndSwiped_IdAndSwipeType(botId, human.id(), UserSwipe.SwipeType.LIKE))
                .isTrue();

        List<StudyMatch> matches = studyMatchRepository.findByParticipantIdAndStatus(human.id(), StudyMatch.MatchStatus.MATCHED);
        boolean matchedWithBot = matches.stream().anyMatch(match -> {
            Long user1Id = match.getUser1() != null ? match.getUser1().getId() : null;
            Long user2Id = match.getUser2() != null ? match.getUser2().getId() : null;
            return botId.equals(user1Id) || botId.equals(user2Id);
        });
        assertThat(matchedWithBot).isTrue();
    }
}
