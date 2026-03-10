package com.example.biblov1.service;

import com.example.biblov1.model.ChatRoom;
import com.example.biblov1.model.Comment;
import com.example.biblov1.model.Like;
import com.example.biblov1.model.Post;
import com.example.biblov1.model.StudyMatch;
import com.example.biblov1.model.User;
import com.example.biblov1.repository.ChatRoomRepository;
import com.example.biblov1.repository.CommentRepository;
import com.example.biblov1.repository.CommunityMemberRepository;
import com.example.biblov1.repository.LikeRepository;
import com.example.biblov1.repository.MessageRepository;
import com.example.biblov1.repository.PostRepository;
import com.example.biblov1.repository.StudyMatchRepository;
import com.example.biblov1.repository.UserRepository;
import com.example.biblov1.service.events.PostCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class BotSocialOrchestratorService {
    private static final Logger logger = LoggerFactory.getLogger(BotSocialOrchestratorService.class);

    private final BotIdentityService botIdentityService;
    private final StudyMatchRepository studyMatchRepository;
    private final PostRepository postRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final MessageRepository messageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final CommunityMemberRepository communityMemberRepository;
    private final UserRepository userRepository;
    private final ChatService chatService;
    private final TaskScheduler taskScheduler;
    private final Set<String> pendingWelcomeKeys = ConcurrentHashMap.newKeySet();

    @Value("${app.bots.social.enabled:true}")
    private boolean socialEnabled;

    @Value("${app.bots.social.welcome.enabled:true}")
    private boolean welcomeEnabled;

    @Value("${app.bots.social.welcome.delay-min-seconds:3}")
    private int welcomeDelayMinSeconds;

    @Value("${app.bots.social.welcome.delay-max-seconds:15}")
    private int welcomeDelayMaxSeconds;

    @Value("${app.bots.social.reactions.enabled:true}")
    private boolean reactionsEnabled;

    @Value("${app.bots.social.reactions.likes-min:1}")
    private int reactionsLikesMin;

    @Value("${app.bots.social.reactions.likes-max:2}")
    private int reactionsLikesMax;

    @Value("${app.bots.social.reactions.comments-max:1}")
    private int reactionsCommentsMax;

    @Value("${app.bots.social.reactions.delay-min-seconds:2}")
    private int reactionsDelayMinSeconds;

    @Value("${app.bots.social.reactions.delay-max-seconds:8}")
    private int reactionsDelayMaxSeconds;

    @Value("${app.bots.social.cross-post.enabled:true}")
    private boolean crossPostEnabled;

    @Value("${app.bots.social.cross-post.max-comments-per-bot:1}")
    private int crossPostMaxCommentsPerBot;

    public BotSocialOrchestratorService(
            BotIdentityService botIdentityService,
            StudyMatchRepository studyMatchRepository,
            PostRepository postRepository,
            LikeRepository likeRepository,
            CommentRepository commentRepository,
            MessageRepository messageRepository,
            ChatRoomRepository chatRoomRepository,
            CommunityMemberRepository communityMemberRepository,
            UserRepository userRepository,
            ChatService chatService,
            @Qualifier("botSocialTaskScheduler") TaskScheduler taskScheduler
    ) {
        this.botIdentityService = botIdentityService;
        this.studyMatchRepository = studyMatchRepository;
        this.postRepository = postRepository;
        this.likeRepository = likeRepository;
        this.commentRepository = commentRepository;
        this.messageRepository = messageRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.communityMemberRepository = communityMemberRepository;
        this.userRepository = userRepository;
        this.chatService = chatService;
        this.taskScheduler = taskScheduler;
    }

    public void onUsersMatched(StudyMatch match) {
        if (!socialEnabled || !welcomeEnabled || match == null || match.getId() == null) {
            return;
        }
        if (match.getStatus() != StudyMatch.MatchStatus.MATCHED) {
            return;
        }

        Long user1Id = match.getUser1() != null ? match.getUser1().getId() : null;
        Long user2Id = match.getUser2() != null ? match.getUser2().getId() : null;
        if (user1Id == null || user2Id == null) {
            return;
        }

        boolean user1IsBot = botIdentityService.isBotUserId(user1Id);
        boolean user2IsBot = botIdentityService.isBotUserId(user2Id);
        if (user1IsBot == user2IsBot) {
            return;
        }

        Long botUserId = user1IsBot ? user1Id : user2Id;
        Long humanUserId = user1IsBot ? user2Id : user1Id;
        ChatRoom chatRoom = chatRoomRepository.findByStudyMatch(match).orElse(null);
        if (chatRoom == null || chatRoom.getId() == null) {
            logger.debug("Skipping welcome scheduling because chat room is missing for match {}", match.getId());
            return;
        }

        scheduleWelcomeMessage(match.getId(), chatRoom.getId(), botUserId, humanUserId);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPostCreated(PostCreatedEvent event) {
        if (event == null || event.postId() == null || event.authorId() == null || event.communityId() == null) {
            return;
        }
        schedulePostReactions(event.postId(), event.authorId(), event.communityId());
    }

    @Scheduled(fixedDelayString = "#{${app.bots.social.cross-post.interval-seconds:3600} * 1000}")
    public void runCrossPostCommentPass() {
        if (!socialEnabled || !crossPostEnabled) {
            return;
        }

        List<Long> botIds = botIdentityService.getActiveBotUserIds();
        if (botIds.isEmpty()) {
            return;
        }

        Pageable pageable = PageRequest.of(0, 120);
        List<Post> recentHumanPosts = postRepository.findRecentPostsByAuthorIdsNotIn(botIds, pageable);
        if (recentHumanPosts.isEmpty()) {
            return;
        }

        Map<Long, User> botUsersById = userRepository.findAllById(botIds).stream()
                .collect(Collectors.toMap(User::getId, user -> user));
        if (botUsersById.isEmpty()) {
            return;
        }

        for (Long botId : botIds) {
            User botUser = botUsersById.get(botId);
            if (botUser == null) {
                continue;
            }

            int commentsCreated = 0;
            for (Post targetPost : recentHumanPosts) {
                if (commentsCreated >= Math.max(0, crossPostMaxCommentsPerBot)) {
                    break;
                }
                if (targetPost.getAuthor() == null || targetPost.getAuthor().getId() == null) {
                    continue;
                }
                if (Objects.equals(targetPost.getAuthor().getId(), botId)) {
                    continue;
                }
                if (commentRepository.existsByPostAndAuthor(targetPost, botUser)) {
                    continue;
                }

                String commentContent = buildCrossPostComment(botId, targetPost);
                if (commentContent == null || commentContent.isBlank()) {
                    continue;
                }
                createCommentIfMissing(targetPost, botUser, commentContent.trim());
                commentsCreated++;
            }
        }
    }

    private void scheduleWelcomeMessage(Long matchId, Long chatRoomId, Long botUserId, Long humanUserId) {
        if (messageRepository.existsByChatRoom_IdAndSender_Id(chatRoomId, botUserId)) {
            return;
        }

        String pendingKey = chatRoomId + ":" + botUserId;
        if (!pendingWelcomeKeys.add(pendingKey)) {
            return;
        }

        int delaySeconds = randomInRange(welcomeDelayMinSeconds, welcomeDelayMaxSeconds);
        Instant runAt = Instant.now().plusSeconds(Math.max(0, delaySeconds));
        taskScheduler.schedule(() -> {
            try {
                sendWelcomeMessageIfStillEligible(matchId, chatRoomId, botUserId, humanUserId);
            } catch (Exception ex) {
                logger.warn(
                        "Failed to send delayed bot welcome: matchId={}, chatRoomId={}, botUserId={}, reason={}",
                        matchId,
                        chatRoomId,
                        botUserId,
                        ex.getMessage()
                );
            } finally {
                pendingWelcomeKeys.remove(pendingKey);
            }
        }, runAt);
    }

    private void sendWelcomeMessageIfStillEligible(Long matchId, Long chatRoomId, Long botUserId, Long humanUserId) {
        StudyMatch latestMatch = studyMatchRepository.findById(matchId).orElse(null);
        if (latestMatch == null || latestMatch.getStatus() != StudyMatch.MatchStatus.MATCHED) {
            return;
        }

        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId).orElse(null);
        if (chatRoom == null || chatRoom.getStudyMatch() == null || !Objects.equals(chatRoom.getStudyMatch().getId(), matchId)) {
            return;
        }

        if (messageRepository.existsByChatRoom_IdAndSender_Id(chatRoomId, botUserId)) {
            return;
        }

        String welcomeMessage = botIdentityService.pickWelcomeMessage(botUserId, humanUserId);
        if (welcomeMessage == null || welcomeMessage.isBlank()) {
            return;
        }
        chatService.saveMessage(chatRoomId, botUserId, welcomeMessage);
    }

    private void schedulePostReactions(Long postId, Long postAuthorId, Long communityId) {
        if (!socialEnabled || !reactionsEnabled || botIdentityService.isBotUserId(postAuthorId)) {
            return;
        }

        Post post = postRepository.findById(postId).orElse(null);
        if (post == null) {
            return;
        }

        List<Long> candidateBotIds = resolveCandidateBots(postAuthorId, communityId);
        if (candidateBotIds.isEmpty()) {
            return;
        }

        int likesToSchedule = Math.min(candidateBotIds.size(), Math.max(0, randomInRange(reactionsLikesMin, reactionsLikesMax)));
        int commentsToSchedule = Math.min(candidateBotIds.size(), Math.max(0, reactionsCommentsMax));

        List<Long> shuffledCandidates = new ArrayList<>(candidateBotIds);
        Collections.shuffle(shuffledCandidates);

        for (int i = 0; i < likesToSchedule; i++) {
            Long botId = shuffledCandidates.get(i);
            scheduleWithDelay(() -> createLikeIfMissing(postId, botId), reactionsDelayMinSeconds, reactionsDelayMaxSeconds);
        }

        for (int i = 0; i < commentsToSchedule; i++) {
            Long botId = shuffledCandidates.get(i);
            String content = buildMatchedPostComment(botId, post);
            if (content == null || content.isBlank()) {
                continue;
            }
            int minDelay = Math.max(0, reactionsDelayMinSeconds + 1);
            int maxDelay = Math.max(minDelay, reactionsDelayMaxSeconds + 2);
            scheduleWithDelay(() -> createCommentIfMissing(postId, botId, content.trim()), minDelay, maxDelay);
        }
    }

    private List<Long> resolveCandidateBots(Long humanUserId, Long communityId) {
        LinkedHashSet<Long> candidates = new LinkedHashSet<>();

        List<StudyMatch> matches = studyMatchRepository.findByParticipantIdAndStatus(humanUserId, StudyMatch.MatchStatus.MATCHED);
        for (StudyMatch match : matches) {
            Long user1Id = match.getUser1() != null ? match.getUser1().getId() : null;
            Long user2Id = match.getUser2() != null ? match.getUser2().getId() : null;
            Long otherUserId = Objects.equals(user1Id, humanUserId) ? user2Id : user1Id;
            if (otherUserId != null && botIdentityService.isBotUserId(otherUserId)) {
                candidates.add(otherUserId);
            }
        }

        List<Long> communityMemberIds = communityMemberRepository.findUserIdsByCommunityId(communityId);
        for (Long memberId : communityMemberIds) {
            if (botIdentityService.isBotUserId(memberId)) {
                candidates.add(memberId);
            }
        }

        return new ArrayList<>(candidates);
    }

    private void scheduleWithDelay(Runnable runnable, int minSeconds, int maxSeconds) {
        int delaySeconds = randomInRange(minSeconds, maxSeconds);
        Instant runAt = Instant.now().plusSeconds(Math.max(0, delaySeconds));
        taskScheduler.schedule(() -> {
            try {
                runnable.run();
            } catch (Exception ex) {
                logger.warn("Bot social scheduled task failed: {}", ex.getMessage());
            }
        }, runAt);
    }

    @Transactional
    protected void createLikeIfMissing(Long postId, Long botUserId) {
        Post post = postRepository.findById(postId).orElse(null);
        User botUser = userRepository.findById(botUserId).orElse(null);
        if (post == null || botUser == null) {
            return;
        }
        if (likeRepository.findByPostAndUser(post, botUser).isPresent()) {
            return;
        }
        Like like = new Like();
        like.setPost(post);
        like.setUser(botUser);
        likeRepository.save(like);
    }

    @Transactional
    protected void createCommentIfMissing(Long postId, Long botUserId, String content) {
        Post post = postRepository.findById(postId).orElse(null);
        User botUser = userRepository.findById(botUserId).orElse(null);
        if (post == null || botUser == null || content == null || content.isBlank()) {
            return;
        }
        createCommentIfMissing(post, botUser, content);
    }

    private void createCommentIfMissing(Post post, User botUser, String content) {
        if (commentRepository.existsByPostAndAuthorAndContent(post, botUser, content)) {
            return;
        }
        Comment comment = new Comment();
        comment.setPost(post);
        comment.setAuthor(botUser);
        comment.setContent(content);
        commentRepository.save(comment);
    }

    private String buildMatchedPostComment(Long botUserId, Post post) {
        String snippet = normalizeSnippet(post.getContent());
        BotIdentityService.BotPersona persona = botIdentityService.findBotPersonaByUserId(botUserId).orElse(null);
        String botName = persona != null ? persona.name() : "I";
        String interest = "this topic";
        if (persona != null && persona.interests() != null && !persona.interests().isEmpty()) {
            interest = persona.interests().get(Math.floorMod(Objects.hash(post.getId(), botUserId), persona.interests().size()));
        }

        int variant = Math.floorMod(Objects.hash(post.getId(), botUserId), 4);
        if (variant == 0) {
            return botName + " here. Strong post around \"" + snippet + "\". I can share a lightweight next-step checklist.";
        }
        if (variant == 1) {
            return "Nice direction on \"" + snippet + "\". If helpful, I can post one practical framework for " + interest + ".";
        }
        if (variant == 2) {
            return "Good momentum. I had a similar flow in " + interest + " and it made progress easier week to week.";
        }
        return "Following this thread. Happy to compare notes and add a concise action plan if useful.";
    }

    private String buildCrossPostComment(Long botUserId, Post post) {
        String snippet = normalizeSnippet(post.getContent());
        BotIdentityService.BotPersona persona = botIdentityService.findBotPersonaByUserId(botUserId).orElse(null);
        String interest = "community projects";
        if (persona != null && persona.interests() != null && !persona.interests().isEmpty()) {
            interest = persona.interests().get(Math.floorMod(Objects.hash(botUserId, post.getId()), persona.interests().size()));
        }
        return "Interesting point on \"" + snippet + "\". I can share a short playbook from " + interest + " if useful.";
    }

    private String normalizeSnippet(String content) {
        if (content == null || content.isBlank()) {
            return "this idea";
        }
        String normalized = content.replaceAll("\\s+", " ").replace("\"", "").trim();
        if (normalized.length() <= 72) {
            return normalized;
        }
        return normalized.substring(0, 72).trim() + "...";
    }

    private int randomInRange(int min, int max) {
        int low = Math.min(min, max);
        int high = Math.max(min, max);
        if (low == high) {
            return Math.max(0, low);
        }
        return Math.max(0, low) + (int) Math.floor(Math.random() * (high - low + 1));
    }
}
