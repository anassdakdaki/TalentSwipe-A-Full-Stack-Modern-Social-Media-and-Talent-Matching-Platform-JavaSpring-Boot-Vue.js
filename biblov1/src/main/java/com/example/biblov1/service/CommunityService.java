package com.example.biblov1.service;

import com.example.biblov1.model.Community;
import com.example.biblov1.model.CommunityMember;
import com.example.biblov1.model.Post;
import com.example.biblov1.model.User;
import com.example.biblov1.repository.CommentRepository;
import com.example.biblov1.repository.CommunityMemberRepository;
import com.example.biblov1.repository.CommunityRepository;
import com.example.biblov1.repository.LikeRepository;
import com.example.biblov1.repository.PostRepository;
import com.example.biblov1.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CommunityService {

    private final CommunityRepository communityRepository;
    private final CommunityMemberRepository communityMemberRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;
    private final UserRepository userRepository;

    @Autowired
    public CommunityService(
            CommunityRepository communityRepository,
            CommunityMemberRepository communityMemberRepository,
            PostRepository postRepository,
            CommentRepository commentRepository,
            LikeRepository likeRepository,
            UserRepository userRepository
    ) {
        this.communityRepository = communityRepository;
        this.communityMemberRepository = communityMemberRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.likeRepository = likeRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Community createCommunity(String name, String description, Long ownerId, List<String> tags) {
        return createCommunity(name, description, ownerId, tags, null);
    }

    @Transactional
    public Community createCommunity(String name, String description, Long ownerId, List<String> tags, String imageUrl) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("Owner user not found"));

        String normalizedName = normalizeName(name);
        String normalizedDescription = normalizeDescription(description);
        Set<String> normalizedTags = normalizeTags(tags);
        ensureUniqueName(normalizedName, null);

        Community community = new Community();
        community.setName(normalizedName);
        community.setDescription(normalizedDescription);
        community.setImageUrl(imageUrl);
        community.setOwner(owner);
        community.setTags(normalizedTags);
        try {
            community = communityRepository.save(community);
        } catch (DataIntegrityViolationException ex) {
            throw new RuntimeException("Community name already exists.");
        }

        // Add owner as a member automatically
        joinCommunity(community.getId(), ownerId);
        return community;
    }

    @Transactional
    public Community updateCommunity(
            Long communityId,
            Long userId,
            String name,
            String description,
            List<String> tags,
            String imageUrl,
            boolean removeImage
    ) {
        Community community = requireOwnedCommunity(communityId, userId);
        String normalizedName = normalizeName(name);
        String normalizedDescription = normalizeDescription(description);
        Set<String> normalizedTags = normalizeTags(tags);
        ensureUniqueName(normalizedName, communityId);

        community.setName(normalizedName);
        community.setDescription(normalizedDescription);
        community.setTags(normalizedTags);
        if (imageUrl != null && !imageUrl.isBlank()) {
            community.setImageUrl(imageUrl);
        } else if (removeImage) {
            community.setImageUrl(null);
        }

        try {
            return communityRepository.save(community);
        } catch (DataIntegrityViolationException ex) {
            throw new RuntimeException("Community name already exists.");
        }
    }

    @Transactional
    public void deleteCommunity(Long communityId, Long userId) {
        Community community = requireOwnedCommunity(communityId, userId);
        List<Post> communityPosts = postRepository.findByCommunity(community);

        if (!communityPosts.isEmpty()) {
            likeRepository.deleteByPostIn(communityPosts);
            commentRepository.deleteByPostIn(communityPosts);
            postRepository.deleteAll(communityPosts);
        }

        communityMemberRepository.deleteByCommunity(community);
        communityRepository.delete(community);
    }

    @Transactional(readOnly = true)
    public List<Community> getAllCommunities() {
        return communityRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Community> getCommunityById(Long communityId) {
        return communityRepository.findById(communityId);
    }

    @Transactional
    public CommunityMember joinCommunity(Long communityId, Long userId) {
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new RuntimeException("Community not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (communityMemberRepository.findByCommunityAndUser(community, user).isPresent()) {
            throw new IllegalStateException("User is already a member of this community.");
        }

        CommunityMember communityMember = new CommunityMember();
        communityMember.setCommunity(community);
        communityMember.setUser(user);
        return communityMemberRepository.save(communityMember);
    }

    @Transactional
    public void leaveCommunity(Long communityId, Long userId) {
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new RuntimeException("Community not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        CommunityMember member = communityMemberRepository.findByCommunityAndUser(community, user)
                .orElseThrow(() -> new RuntimeException("User is not a member of this community."));

        communityMemberRepository.delete(member);
    }

    @Transactional(readOnly = true)
    public List<Community> getCommunitiesByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return communityMemberRepository.findByUser(user).stream()
                .map(CommunityMember::getCommunity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long getCommunityMemberCount(Long communityId) {
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new RuntimeException("Community not found"));
        return communityMemberRepository.countByCommunity(community);
    }
    
    @Transactional(readOnly = true)
    public boolean isUserMemberOfCommunity(Long userId, Long communityId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new RuntimeException("Community not found"));
        return communityMemberRepository.findByCommunityAndUser(community, user).isPresent();
    }

    private Community requireOwnedCommunity(Long communityId, Long userId) {
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new RuntimeException("Community not found"));
        if (!community.getOwner().getId().equals(userId)) {
            throw new IllegalStateException("Only the community owner can modify or delete this community.");
        }
        return community;
    }

    private void ensureUniqueName(String name, Long currentCommunityId) {
        communityRepository.findByName(name).ifPresent(existing -> {
            if (currentCommunityId == null || !existing.getId().equals(currentCommunityId)) {
                throw new RuntimeException("Community name already exists.");
            }
        });
    }

    private String normalizeName(String value) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isEmpty()) {
            throw new RuntimeException("Community name is required.");
        }
        if (normalized.length() > 120) {
            throw new RuntimeException("Community name must be 120 characters or less.");
        }
        return normalized;
    }

    private String normalizeDescription(String value) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isEmpty()) {
            throw new RuntimeException("Community description is required.");
        }
        if (normalized.length() > 1200) {
            throw new RuntimeException("Community description must be 1200 characters or less.");
        }
        return normalized;
    }

    private Set<String> normalizeTags(List<String> tags) {
        if (tags == null) {
            return new LinkedHashSet<>();
        }
        return tags.stream()
                .filter(tag -> tag != null && !tag.trim().isEmpty())
                .map(String::trim)
                .limit(20)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

} 
