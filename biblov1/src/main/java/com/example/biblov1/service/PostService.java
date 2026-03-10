package com.example.biblov1.service;

import com.example.biblov1.model.Community;
import com.example.biblov1.model.Post;
import com.example.biblov1.model.User;
import com.example.biblov1.model.UserProfile;
import com.example.biblov1.payload.response.FeedPostResponse;
import com.example.biblov1.payload.response.FeedPostsPageResponse;
import com.example.biblov1.repository.CommunityRepository;
import com.example.biblov1.repository.CommunityMemberRepository;
import com.example.biblov1.repository.CommentRepository;
import com.example.biblov1.repository.UserProfileRepository;
import com.example.biblov1.repository.PostRepository;
import com.example.biblov1.repository.UserRepository;
import com.example.biblov1.repository.LikeRepository;
import com.example.biblov1.payload.response.PostResponse;
import com.example.biblov1.payload.response.CommentResponse;
import com.example.biblov1.service.events.PostCreatedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final CommunityRepository communityRepository;
    private final CommunityMemberRepository communityMemberRepository;
    private final CommentRepository commentRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final CommentService commentService;
    private final FileStorageService fileStorageService;
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public PostService(
            PostRepository postRepository,
            CommunityRepository communityRepository,
            CommunityMemberRepository communityMemberRepository,
            CommentRepository commentRepository,
            UserProfileRepository userProfileRepository,
            UserRepository userRepository,
            LikeRepository likeRepository,
            CommentService commentService,
            FileStorageService fileStorageService,
            ApplicationEventPublisher eventPublisher
    ) {
        this.postRepository = postRepository;
        this.communityRepository = communityRepository;
        this.communityMemberRepository = communityMemberRepository;
        this.commentRepository = commentRepository;
        this.userProfileRepository = userProfileRepository;
        this.userRepository = userRepository;
        this.likeRepository = likeRepository;
        this.commentService = commentService;
        this.fileStorageService = fileStorageService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public Post createPost(Long communityId, Long authorId, String content, MultipartFile imageFile, Set<String> hashtags) {
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new RuntimeException("Community not found"));
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new RuntimeException("Author user not found"));

        String imageUrl = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            imageUrl = fileStorageService.storeFile(imageFile);
        }

        Post post = new Post();
        post.setCommunity(community);
        post.setAuthor(author);
        post.setContent(content);
        post.setImageUrl(imageUrl);
        post.setHashtags(hashtags);
        Post createdPost = postRepository.save(post);
        eventPublisher.publishEvent(new PostCreatedEvent(createdPost.getId(), authorId, communityId));
        return createdPost;
    }

    @Transactional(readOnly = true)
    public PostResponse getPostById(Long postId, Long currentUserId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        return convertToPostResponse(post, currentUserId);
    }

    @Transactional(readOnly = true)
    public List<PostResponse> getPostsByCommunity(Long communityId, Long currentUserId) {
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new RuntimeException("Community not found"));
        return postRepository.findByCommunityOrderByCreatedAtDesc(community).stream()
                .map(post -> convertToPostResponse(post, currentUserId))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FeedPostsPageResponse getFeedPosts(Long userId, Integer limit, LocalDateTime cursorCreatedAt, Long cursorPostId) {
        boolean hasCreatedAt = cursorCreatedAt != null;
        boolean hasCursorPostId = cursorPostId != null;
        if (hasCreatedAt != hasCursorPostId) {
            throw new IllegalArgumentException("cursorCreatedAt and cursorPostId must be provided together.");
        }

        int safeLimit = limit == null ? 10 : limit;
        if (safeLimit < 1 || safeLimit > 30) {
            throw new IllegalArgumentException("limit must be between 1 and 30.");
        }

        List<Long> joinedCommunityIds = communityMemberRepository.findCommunityIdsByUserId(userId);
        if (joinedCommunityIds.isEmpty()) {
            return new FeedPostsPageResponse(Collections.emptyList(), null, null, false);
        }

        Pageable pageable = PageRequest.of(0, safeLimit + 1);
        List<Post> queriedPosts = hasCreatedAt
                ? postRepository.findFeedPostsByCommunityIdsBeforeCursor(joinedCommunityIds, cursorCreatedAt, cursorPostId, pageable)
                : postRepository.findFeedPostsByCommunityIds(joinedCommunityIds, pageable);

        boolean hasMore = queriedPosts.size() > safeLimit;
        List<Post> pagePosts = hasMore ? queriedPosts.subList(0, safeLimit) : queriedPosts;

        if (pagePosts.isEmpty()) {
            return new FeedPostsPageResponse(Collections.emptyList(), null, null, false);
        }

        List<Long> postIds = pagePosts.stream().map(Post::getId).toList();
        List<Long> authorIds = pagePosts.stream().map(post -> post.getAuthor().getId()).distinct().toList();

        Map<Long, Long> likesCountByPostId = likeRepository.countByPostIds(postIds).stream()
                .collect(Collectors.toMap(LikeRepository.PostLikeCountProjection::getPostId, LikeRepository.PostLikeCountProjection::getCount));
        Set<Long> likedPostIdsByCurrentUser = likeRepository.findLikedPostIdsByUserIdAndPostIds(userId, postIds).stream()
                .collect(Collectors.toSet());
        Map<Long, Long> commentsCountByPostId = commentRepository.countByPostIds(postIds).stream()
                .collect(Collectors.toMap(CommentRepository.PostCommentCountProjection::getPostId, CommentRepository.PostCommentCountProjection::getCount));
        Map<Long, String> authorProfilePicturesByUserId = userProfileRepository.findByUserIdIn(authorIds).stream()
                .collect(HashMap::new, (map, profile) -> map.put(profile.getUser().getId(), profile.getProfilePictureUrl()), Map::putAll);

        List<FeedPostResponse> items = pagePosts.stream()
                .map(post -> new FeedPostResponse(
                        post.getId(),
                        post.getContent(),
                        post.getImageUrl(),
                        post.getCreatedAt(),
                        post.getUpdatedAt(),
                        post.getAuthor().getId(),
                        post.getAuthor().getName(),
                        authorProfilePicturesByUserId.get(post.getAuthor().getId()),
                        post.getCommunity().getId(),
                        post.getCommunity().getName(),
                        post.getCommunity().getImageUrl(),
                        post.getHashtags(),
                        likesCountByPostId.getOrDefault(post.getId(), 0L),
                        likedPostIdsByCurrentUser.contains(post.getId()),
                        commentsCountByPostId.getOrDefault(post.getId(), 0L)
                ))
                .toList();

        LocalDateTime nextCursorCreatedAt = null;
        Long nextCursorPostId = null;
        if (hasMore) {
            FeedPostResponse last = items.get(items.size() - 1);
            nextCursorCreatedAt = last.getCreatedAt();
            nextCursorPostId = last.getId();
        }

        return new FeedPostsPageResponse(items, nextCursorCreatedAt, nextCursorPostId, hasMore);
    }

    @Transactional
    public Post updatePost(Long postId, String content, String imageUrl, Set<String> hashtags) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        post.setContent(content);
        post.setImageUrl(imageUrl);
        post.setHashtags(hashtags);
        return postRepository.save(post);
    }

    @Transactional
    public void deletePost(Long postId) {
        postRepository.deleteById(postId);
    }

    private PostResponse convertToPostResponse(Post post, Long currentUserId) {
        long likesCount = likeRepository.countByPost(post);
        boolean isLiked = false;
        String authorProfilePictureUrl = userProfileRepository.findByUserId(post.getAuthor().getId())
                .map(UserProfile::getProfilePictureUrl)
                .orElse(null);
        if (currentUserId != null) {
            User currentUser = userRepository.findById(currentUserId).orElse(null);
            if (currentUser != null) {
                isLiked = likeRepository.findByPostAndUser(post, currentUser).isPresent();
            }
        }
        List<CommentResponse> comments = commentService.getCommentsByPost(post.getId());

        return new PostResponse(
                post.getId(),
                post.getContent(),
                post.getImageUrl(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                post.getAuthor().getName(),
                post.getAuthor().getId(),
                authorProfilePictureUrl,
                post.getCommunity().getId(),
                post.getHashtags(),
                likesCount,
                isLiked,
                comments
        );
    }
} 
