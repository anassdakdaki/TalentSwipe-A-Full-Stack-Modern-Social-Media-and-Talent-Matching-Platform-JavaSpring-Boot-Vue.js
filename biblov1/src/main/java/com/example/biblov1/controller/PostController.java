package com.example.biblov1.controller;

import com.example.biblov1.model.Post;
import com.example.biblov1.service.PostService;
import com.example.biblov1.payload.request.CreatePostRequest;
import com.example.biblov1.payload.request.UpdatePostRequest;
import com.example.biblov1.payload.response.PostResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Arrays;
import java.util.HashSet;

@RestController
@RequestMapping("/api/posts")
@CrossOrigin(origins = "http://localhost:5173")
public class PostController {

    private final PostService postService;

    @Autowired
    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping
    public ResponseEntity<?> createPost(
            @RequestAttribute("userId") Long authorId,
            @RequestParam("communityId") Long communityId,
            @RequestParam("content") String content,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            @RequestParam("hashtags") String hashtagsJson
    ) {
        try {
            // Check file size before processing
            if (imageFile != null && !imageFile.isEmpty()) {
                long fileSizeInMB = imageFile.getSize() / (1024 * 1024);
                if (fileSizeInMB > 50) {
                    return ResponseEntity.badRequest().body(Map.of("error", "File size too large. Maximum allowed size is 50MB."));
                }
            }

            Set<String> hashtags = new HashSet<>(Arrays.asList(new ObjectMapper().readValue(hashtagsJson, String[].class)));

            Post post = postService.createPost(communityId, authorId, content, imageFile, hashtags);
            return ResponseEntity.status(HttpStatus.CREATED).body(post);
        } catch (MaxUploadSizeExceededException e) {
            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                    .body(Map.of("error", "File size too large. Maximum allowed size is 50MB."));
        } catch (RuntimeException | JsonProcessingException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create post: " + e.getMessage()));
        }
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostResponse> getPostById(@PathVariable Long postId, @RequestAttribute("userId") Long userId) {
        return ResponseEntity.ok(postService.getPostById(postId, userId));
    }

    @GetMapping("/community/{communityId}")
    public ResponseEntity<List<PostResponse>> getPostsByCommunity(@PathVariable Long communityId, @RequestAttribute("userId") Long userId) {
        List<PostResponse> posts = postService.getPostsByCommunity(communityId, userId);
        return ResponseEntity.ok(posts);
    }

    @PutMapping("/{postId}")
    public ResponseEntity<?> updatePost(@PathVariable Long postId, @RequestBody UpdatePostRequest request) {
        try {
            Post updatedPost = postService.updatePost(postId, request.getContent(), request.getImageUrl(), request.getHashtags());
            return ResponseEntity.ok(updatedPost);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<?> deletePost(@PathVariable Long postId) {
        try {
            postService.deletePost(postId);
            return ResponseEntity.ok(Map.of("message", "Post deleted successfully."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Global exception handler for upload size exceeded
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, String>> handleMaxSizeException(MaxUploadSizeExceededException exc) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(Map.of("error", "File size too large. Maximum allowed size is 50MB."));
    }
} 