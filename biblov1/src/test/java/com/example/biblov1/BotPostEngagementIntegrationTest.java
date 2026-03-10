package com.example.biblov1;

import com.example.biblov1.model.Community;
import com.example.biblov1.model.Post;
import com.example.biblov1.repository.CommentRepository;
import com.example.biblov1.repository.CommunityRepository;
import com.example.biblov1.repository.LikeRepository;
import com.example.biblov1.service.BotIdentityService;
import com.example.biblov1.service.BotPopulationService;
import com.example.biblov1.service.PostService;
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

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BotPostEngagementIntegrationTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private TestDatabaseCleanup databaseCleanup;
    @Autowired private BotPopulationService botPopulationService;
    @Autowired private BotIdentityService botIdentityService;
    @Autowired private PostService postService;
    @Autowired private CommunityRepository communityRepository;
    @Autowired private LikeRepository likeRepository;
    @Autowired private CommentRepository commentRepository;

    @BeforeEach
    void resetDb() {
        databaseCleanup.reset();
    }

    @Test
    void should_add_moderate_bot_reactions_on_human_post() throws Exception {
        botPopulationService.seedBotProfilesAndCommunities();
        List<Long> botIds = botIdentityService.getActiveBotUserIds();
        assertThat(botIds).isNotEmpty();
        Long botId = botIds.get(0);

        TestAuthSupport.TestUser human = TestAuthSupport.registerAndLogin(
                mockMvc,
                objectMapper,
                "Human Poster",
                "human-poster@example.com",
                "password123"
        );

        // Ensure a direct bot-human match exists before posting.
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

        Community targetCommunity = communityRepository.findAll().stream().findFirst().orElse(null);
        assertThat(targetCommunity).isNotNull();

        Post createdPost = postService.createPost(
                targetCommunity.getId(),
                human.id(),
                "Need feedback on this workflow for my next project sprint.",
                null,
                Set.of("workflow", "project", "feedback")
        );

        BotReactionSnapshot snapshot = waitForBotReactions(createdPost.getId(), botIds, Duration.ofSeconds(5));
        assertThat(snapshot.botLikes()).isBetween(1L, 2L);
        assertThat(snapshot.botComments()).isBetween(0L, 1L);
    }

    private BotReactionSnapshot waitForBotReactions(Long postId, List<Long> botIds, Duration timeout) throws InterruptedException {
        Instant deadline = Instant.now().plus(timeout);
        BotReactionSnapshot lastSnapshot = new BotReactionSnapshot(0, 0);
        while (Instant.now().isBefore(deadline)) {
            long likes = likeRepository.findAll().stream()
                    .filter(like -> like.getPost() != null && postId.equals(like.getPost().getId()))
                    .filter(like -> like.getUser() != null && botIds.contains(like.getUser().getId()))
                    .count();
            long comments = commentRepository.findAll().stream()
                    .filter(comment -> comment.getPost() != null && postId.equals(comment.getPost().getId()))
                    .filter(comment -> comment.getAuthor() != null && botIds.contains(comment.getAuthor().getId()))
                    .count();

            lastSnapshot = new BotReactionSnapshot(likes, comments);
            if (likes >= 1) {
                return lastSnapshot;
            }
            Thread.sleep(100);
        }
        return lastSnapshot;
    }

    private record BotReactionSnapshot(long botLikes, long botComments) {}
}
