package com.example.biblov1.service;

import com.example.biblov1.model.Community;
import com.example.biblov1.model.Post;
import com.example.biblov1.model.User;
import com.example.biblov1.repository.CommunityRepository;
import com.example.biblov1.repository.PostRepository;
import com.example.biblov1.repository.UserRepository;
import com.example.biblov1.repository.LikeRepository;
import com.example.biblov1.payload.response.PostResponse;
import com.example.biblov1.payload.response.CommentResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final CommunityRepository communityRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final CommentService commentService;
    private final FileStorageService fileStorageService;

    @Autowired
    public PostService(PostRepository postRepository, CommunityRepository communityRepository, UserRepository userRepository, LikeRepository likeRepository, CommentService commentService, FileStorageService fileStorageService) {
        this.postRepository = postRepository;
        this.communityRepository = communityRepository;
        this.userRepository = userRepository;
        this.likeRepository = likeRepository;
        this.commentService = commentService;
        this.fileStorageService = fileStorageService;
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
        return postRepository.save(post);
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
                post.getCommunity().getId(),
                post.getHashtags(),
                likesCount,
                isLiked,
                comments
        );
    }
} 