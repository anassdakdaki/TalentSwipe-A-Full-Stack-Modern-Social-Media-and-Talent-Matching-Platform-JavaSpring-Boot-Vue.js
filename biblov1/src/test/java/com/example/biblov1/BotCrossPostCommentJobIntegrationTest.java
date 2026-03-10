package com.example.biblov1;

import com.example.biblov1.model.Comment;
import com.example.biblov1.model.Community;
import com.example.biblov1.model.Post;
import com.example.biblov1.repository.CommentRepository;
import com.example.biblov1.repository.CommunityRepository;
import com.example.biblov1.service.BotIdentityService;
import com.example.biblov1.service.BotPopulationService;
import com.example.biblov1.service.BotSocialOrchestratorService;
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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "app.bots.social.cross-post.enabled=true",
        "app.bots.social.reactions.enabled=false",
        "app.bots.social.welcome.enabled=false",
        "app.bots.social.cross-post.max-comments-per-bot=1"
})
class BotCrossPostCommentJobIntegrationTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private TestDatabaseCleanup databaseCleanup;
    @Autowired private BotPopulationService botPopulationService;
    @Autowired private BotIdentityService botIdentityService;
    @Autowired private BotSocialOrchestratorService botSocialOrchestratorService;
    @Autowired private PostService postService;
    @Autowired private CommunityRepository communityRepository;
    @Autowired private CommentRepository commentRepository;

    @BeforeEach
    void resetDb() {
        databaseCleanup.reset();
    }

    @Test
    void should_comment_on_human_posts_with_max_one_comment_per_bot() throws Exception {
        botPopulationService.seedBotProfilesAndCommunities();
        List<Long> botIds = botIdentityService.getActiveBotUserIds();
        assertThat(botIds).isNotEmpty();

        TestAuthSupport.TestUser humanA = TestAuthSupport.registerAndLogin(mockMvc, objectMapper, "Human A", "humanA@example.com", "password123");
        TestAuthSupport.TestUser humanB = TestAuthSupport.registerAndLogin(mockMvc, objectMapper, "Human B", "humanB@example.com", "password123");

        Community community = communityRepository.findAll().stream().findFirst().orElse(null);
        assertThat(community).isNotNull();

        Post postA = postService.createPost(community.getId(), humanA.id(), "Human A planning thread", null, Set.of("plan"));
        Post postB = postService.createPost(community.getId(), humanB.id(), "Human B strategy thread", null, Set.of("strategy"));

        botSocialOrchestratorService.runCrossPostCommentPass();

        List<Comment> botCommentsOnTargets = commentRepository.findAll().stream()
                .filter(comment -> comment.getPost() != null)
                .filter(comment -> postA.getId().equals(comment.getPost().getId()) || postB.getId().equals(comment.getPost().getId()))
                .filter(comment -> comment.getAuthor() != null && botIds.contains(comment.getAuthor().getId()))
                .collect(Collectors.toList());

        assertThat(botCommentsOnTargets).isNotEmpty();

        Map<Long, Long> commentsPerBot = botCommentsOnTargets.stream()
                .collect(Collectors.groupingBy(comment -> comment.getAuthor().getId(), Collectors.counting()));

        for (Long count : commentsPerBot.values()) {
            assertThat(count).isLessThanOrEqualTo(1L);
        }
    }
}
