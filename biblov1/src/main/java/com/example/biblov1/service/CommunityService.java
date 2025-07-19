package com.example.biblov1.service;

import com.example.biblov1.model.Community;
import com.example.biblov1.model.CommunityMember;
import com.example.biblov1.model.User;
import com.example.biblov1.repository.CommunityMemberRepository;
import com.example.biblov1.repository.CommunityRepository;
import com.example.biblov1.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CommunityService {

    private final CommunityRepository communityRepository;
    private final CommunityMemberRepository communityMemberRepository;
    private final UserRepository userRepository;

    @Autowired
    public CommunityService(CommunityRepository communityRepository, CommunityMemberRepository communityMemberRepository, UserRepository userRepository) {
        this.communityRepository = communityRepository;
        this.communityMemberRepository = communityMemberRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Community createCommunity(String name, String description, Long ownerId, List<String> tags) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("Owner user not found"));

        Community community = new Community();
        community.setName(name);
        community.setDescription(description);
        community.setOwner(owner);
        community.setTags(new java.util.HashSet<>(tags));
        community = communityRepository.save(community);

        // Add owner as a member automatically
        joinCommunity(community.getId(), ownerId);
        return community;
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

} 