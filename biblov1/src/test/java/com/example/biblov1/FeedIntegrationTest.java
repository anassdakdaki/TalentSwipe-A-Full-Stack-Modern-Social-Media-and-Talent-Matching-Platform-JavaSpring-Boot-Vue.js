package com.example.biblov1;

import com.example.biblov1.model.Comment;
import com.example.biblov1.model.Community;
import com.example.biblov1.model.CommunityMember;
import com.example.biblov1.model.Like;
import com.example.biblov1.model.Post;
import com.example.biblov1.model.User;
import com.example.biblov1.model.UserProfile;
import com.example.biblov1.repository.CommentRepository;
import com.example.biblov1.repository.CommunityMemberRepository;
import com.example.biblov1.repository.CommunityRepository;
import com.example.biblov1.repository.LikeRepository;
import com.example.biblov1.repository.PostRepository;
import com.example.biblov1.repository.UserProfileRepository;
import com.example.biblov1.repository.UserRepository;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FeedIntegrationTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private TestDatabaseCleanup databaseCleanup;
    @Autowired private UserRepository userRepository;
    @Autowired private UserProfileRepository userProfileRepository;
    @Autowired private CommunityRepository communityRepository;
    @Autowired private CommunityMemberRepository communityMemberRepository;
    @Autowired private PostRepository postRepository;
    @Autowired private LikeRepository likeRepository;
    @Autowired private CommentRepository commentRepository;

    @BeforeEach
    void resetDb() {
        databaseCleanup.reset();
    }

    @Test
    void should_require_authentication_for_feed() throws Exception {
        mockMvc.perform(get("/api/posts/feed"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void should_return_only_joined_communities_and_mix_users_and_communities() throws Exception {
        SeedData seed = seedFeedData();

        MvcResult result = mockMvc.perform(
                        get("/api/posts/feed")
                                .header("Authorization", "Bearer " + seed.viewerToken())
                                .param("limit", "10")
                )
                .andExpect(status().isOk())
                .andReturn();

        JsonNode items = objectMapper.readTree(result.getResponse().getContentAsString()).get("items");
        Set<Long> allowedCommunityIds = Set.of(seed.joinedCommunityA().getId(), seed.joinedCommunityB().getId());

        Set<Long> returnedCommunityIds = StreamSupport.stream(items.spliterator(), false)
                .map(node -> node.get("communityId").asLong())
                .collect(java.util.stream.Collectors.toSet());
        Set<Long> returnedAuthorIds = StreamSupport.stream(items.spliterator(), false)
                .map(node -> node.get("authorId").asLong())
                .collect(java.util.stream.Collectors.toSet());

        assertThat(returnedCommunityIds).isSubsetOf(allowedCommunityIds);
        assertThat(returnedCommunityIds).contains(seed.joinedCommunityA().getId(), seed.joinedCommunityB().getId());
        assertThat(returnedAuthorIds).contains(seed.authorA().getId(), seed.authorB().getId());
    }

    @Test
    void should_sort_newest_first_with_id_tie_breaker() throws Exception {
        SeedData seed = seedFeedData();

        MvcResult result = mockMvc.perform(
                        get("/api/posts/feed")
                                .header("Authorization", "Bearer " + seed.viewerToken())
                                .param("limit", "10")
                )
                .andExpect(status().isOk())
                .andReturn();

        JsonNode items = objectMapper.readTree(result.getResponse().getContentAsString()).get("items");
        List<Long> ids = StreamSupport.stream(items.spliterator(), false)
                .map(node -> node.get("id").asLong())
                .toList();

        assertThat(ids).containsExactly(
                seed.postNewest().getId(),
                seed.postTieHigherId().getId(),
                seed.postTieLowerId().getId(),
                seed.postOlder().getId()
        );
    }

    @Test
    void should_support_cursor_pagination_without_overlap() throws Exception {
        SeedData seed = seedFeedData();

        MvcResult firstPage = mockMvc.perform(
                        get("/api/posts/feed")
                                .header("Authorization", "Bearer " + seed.viewerToken())
                                .param("limit", "2")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.hasMore").value(true))
                .andReturn();

        JsonNode firstPayload = objectMapper.readTree(firstPage.getResponse().getContentAsString());
        JsonNode firstItems = firstPayload.get("items");
        List<Long> firstIds = StreamSupport.stream(firstItems.spliterator(), false)
                .map(node -> node.get("id").asLong())
                .toList();

        String nextCursorCreatedAt = firstPayload.get("nextCursorCreatedAt").asText();
        String nextCursorPostId = firstPayload.get("nextCursorPostId").asText();

        MvcResult secondPage = mockMvc.perform(
                        get("/api/posts/feed")
                                .header("Authorization", "Bearer " + seed.viewerToken())
                                .param("limit", "2")
                                .param("cursorCreatedAt", nextCursorCreatedAt)
                                .param("cursorPostId", nextCursorPostId)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.hasMore").value(false))
                .andReturn();

        JsonNode secondItems = objectMapper.readTree(secondPage.getResponse().getContentAsString()).get("items");
        List<Long> secondIds = StreamSupport.stream(secondItems.spliterator(), false)
                .map(node -> node.get("id").asLong())
                .toList();

        assertThat(firstIds).doesNotContainAnyElementsOf(secondIds);
    }

    @Test
    void should_return_empty_feed_when_user_has_no_joined_communities() throws Exception {
        TestAuthSupport.TestUser user = TestAuthSupport.registerAndLogin(
                mockMvc, objectMapper, "Lonely", "lonely@example.com", "password123"
        );

        MvcResult result = mockMvc.perform(
                        get("/api/posts/feed")
                                .header("Authorization", "Bearer " + user.token())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(0))
                .andExpect(jsonPath("$.hasMore").value(false))
                .andReturn();

        JsonNode payload = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(payload.get("nextCursorCreatedAt").isNull()).isTrue();
        assertThat(payload.get("nextCursorPostId").isNull()).isTrue();
    }

    @Test
    void should_return_like_and_comment_metrics_correctly() throws Exception {
        SeedData seed = seedFeedData();

        MvcResult result = mockMvc.perform(
                        get("/api/posts/feed")
                                .header("Authorization", "Bearer " + seed.viewerToken())
                                .param("limit", "10")
                )
                .andExpect(status().isOk())
                .andReturn();

        JsonNode items = objectMapper.readTree(result.getResponse().getContentAsString()).get("items");
        JsonNode target = StreamSupport.stream(items.spliterator(), false)
                .filter(node -> node.get("id").asLong() == seed.postNewest().getId())
                .findFirst()
                .orElseThrow();

        assertThat(target.get("isLiked").asBoolean()).isTrue();
        assertThat(target.get("likesCount").asLong()).isEqualTo(2L);
        assertThat(target.get("commentsCount").asLong()).isEqualTo(2L);
        assertThat(target.get("authorProfilePictureUrl").asText()).isEqualTo("/uploads/author-a.png");
    }

    @Test
    void should_reject_half_cursor_with_bad_request() throws Exception {
        TestAuthSupport.TestUser user = TestAuthSupport.registerAndLogin(
                mockMvc, objectMapper, "Cursor", "cursor@example.com", "password123"
        );

        mockMvc.perform(
                        get("/api/posts/feed")
                                .header("Authorization", "Bearer " + user.token())
                                .param("cursorCreatedAt", "2026-01-10T10:00:00")
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("cursorCreatedAt and cursorPostId must be provided together."));
    }

    private SeedData seedFeedData() throws Exception {
        TestAuthSupport.TestUser viewer = TestAuthSupport.registerAndLogin(
                mockMvc, objectMapper, "Viewer", "viewer@example.com", "password123"
        );
        TestAuthSupport.TestUser authorAAuth = TestAuthSupport.registerAndLogin(
                mockMvc, objectMapper, "Author A", "authora@example.com", "password123"
        );
        TestAuthSupport.TestUser authorBAuth = TestAuthSupport.registerAndLogin(
                mockMvc, objectMapper, "Author B", "authorb@example.com", "password123"
        );
        TestAuthSupport.TestUser outsiderAuth = TestAuthSupport.registerAndLogin(
                mockMvc, objectMapper, "Outsider", "outsider@example.com", "password123"
        );

        User viewerUser = userRepository.findById(viewer.id()).orElseThrow();
        User authorAUser = userRepository.findById(authorAAuth.id()).orElseThrow();
        User authorBUser = userRepository.findById(authorBAuth.id()).orElseThrow();
        User outsiderUser = userRepository.findById(outsiderAuth.id()).orElseThrow();

        UserProfile authorAProfile = userProfileRepository.findByUserId(authorAUser.getId()).orElseThrow();
        authorAProfile.setProfilePictureUrl("/uploads/author-a.png");
        userProfileRepository.save(authorAProfile);

        Community joinedA = createCommunity("Joined Community A", authorAUser);
        Community joinedB = createCommunity("Joined Community B", authorBUser);
        Community hiddenCommunity = createCommunity("Hidden Community", outsiderUser);

        joinCommunity(viewerUser, joinedA);
        joinCommunity(viewerUser, joinedB);
        joinCommunity(authorAUser, joinedA);
        joinCommunity(authorBUser, joinedB);
        joinCommunity(outsiderUser, hiddenCommunity);

        LocalDateTime base = LocalDateTime.of(2026, 1, 10, 10, 0, 0);
        Post postNewest = createPost(joinedA, authorAUser, "Newest joined post", base.plusMinutes(4));
        Post postTieLowerId = createPost(joinedB, authorBUser, "Tie lower id", base.plusMinutes(3));
        Post postTieHigherId = createPost(joinedB, authorAUser, "Tie higher id", base.plusMinutes(3));
        Post postOlder = createPost(joinedA, authorBUser, "Older joined post", base.plusMinutes(2));
        createPost(hiddenCommunity, outsiderUser, "Hidden post", base.plusMinutes(5));

        likePost(postNewest, viewerUser);
        likePost(postNewest, authorBUser);
        likePost(postTieLowerId, viewerUser);

        commentOnPost(postNewest, viewerUser, "Comment 1");
        commentOnPost(postNewest, authorBUser, "Comment 2");
        commentOnPost(postTieHigherId, authorAUser, "Comment 3");

        return new SeedData(
                viewer.token(),
                authorAUser,
                authorBUser,
                joinedA,
                joinedB,
                postNewest,
                postTieHigherId,
                postTieLowerId,
                postOlder
        );
    }

    private Community createCommunity(String name, User owner) {
        Community community = new Community();
        community.setName(name);
        community.setDescription(name + " description");
        community.setOwner(owner);
        return communityRepository.save(community);
    }

    private void joinCommunity(User user, Community community) {
        CommunityMember member = new CommunityMember();
        member.setUser(user);
        member.setCommunity(community);
        communityMemberRepository.save(member);
    }

    private Post createPost(Community community, User author, String content, LocalDateTime createdAt) {
        Post post = new Post();
        post.setCommunity(community);
        post.setAuthor(author);
        post.setContent(content);
        post.setHashtags(Set.of("feed"));
        post = postRepository.save(post);
        post.setCreatedAt(createdAt);
        return postRepository.save(post);
    }

    private void likePost(Post post, User user) {
        Like like = new Like();
        like.setPost(post);
        like.setUser(user);
        likeRepository.save(like);
    }

    private void commentOnPost(Post post, User user, String content) {
        Comment comment = new Comment();
        comment.setPost(post);
        comment.setAuthor(user);
        comment.setContent(content);
        commentRepository.save(comment);
    }

    private record SeedData(
            String viewerToken,
            User authorA,
            User authorB,
            Community joinedCommunityA,
            Community joinedCommunityB,
            Post postNewest,
            Post postTieHigherId,
            Post postTieLowerId,
            Post postOlder
    ) {}
}
