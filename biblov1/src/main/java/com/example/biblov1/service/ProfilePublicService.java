package com.example.biblov1.service;

import com.example.biblov1.model.Comment;
import com.example.biblov1.model.Community;
import com.example.biblov1.model.CommunityMember;
import com.example.biblov1.model.Post;
import com.example.biblov1.model.ProfileSectionVisibility;
import com.example.biblov1.model.StudyMatch;
import com.example.biblov1.model.User;
import com.example.biblov1.model.UserProfile;
import com.example.biblov1.payload.response.profile.ProfilePublicResponse;
import com.example.biblov1.payload.response.profile.ProfileStatsResponse;
import com.example.biblov1.payload.response.profile.ProfileSummaryResponse;
import com.example.biblov1.payload.response.profile.RecentActivityItem;
import com.example.biblov1.repository.CommentRepository;
import com.example.biblov1.repository.CommunityMemberRepository;
import com.example.biblov1.repository.CommunityRepository;
import com.example.biblov1.repository.PostRepository;
import com.example.biblov1.repository.StudyMatchRepository;
import com.example.biblov1.repository.UserProfileRepository;
import com.example.biblov1.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProfilePublicService {
    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final CommunityRepository communityRepository;
    private final CommunityMemberRepository communityMemberRepository;
    private final StudyMatchRepository studyMatchRepository;

    public ProfilePublicService(UserProfileRepository userProfileRepository,
                                UserRepository userRepository,
                                PostRepository postRepository,
                                CommentRepository commentRepository,
                                CommunityRepository communityRepository,
                                CommunityMemberRepository communityMemberRepository,
                                StudyMatchRepository studyMatchRepository) {
        this.userProfileRepository = userProfileRepository;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.communityRepository = communityRepository;
        this.communityMemberRepository = communityMemberRepository;
        this.studyMatchRepository = studyMatchRepository;
    }

    @Transactional(readOnly = true)
    public ProfilePublicResponse getPublicProfile(Long targetUserId, Long viewerUserId) {
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("User not found: " + targetUserId));
        UserProfile profile = getOrFallbackProfile(targetUser);

        boolean isOwner = viewerUserId != null && viewerUserId.equals(targetUserId);
        boolean isConnected = !isOwner && viewerUserId != null && areUsersMatched(viewerUserId, targetUserId);
        boolean canMessage = !isOwner && isConnected;

        UserProfile.VisibilitySettings visibility = profile.getVisibilitySettings() == null
                ? new UserProfile.VisibilitySettings()
                : profile.getVisibilitySettings();

        ProfilePublicResponse response = new ProfilePublicResponse();
        response.setUserId(targetUserId);
        response.setName(profile.getName());
        response.setProfilePictureUrl(profile.getProfilePictureUrl());
        response.setHeadline(profile.getHeadline());
        response.setAge(profile.getAge());
        response.setGender(profile.getGender());
        response.setLocation(profile.getLocation());
        response.setUniversity(profile.getUniversity());
        response.setMajor(profile.getMajor());
        response.setOpenToCollaborate(profile.isOpenToCollaborate());
        response.setOwner(isOwner);
        response.setConnected(isConnected);
        response.setCanMessage(canMessage);

        if (canView(visibility.getAbout(), isOwner, isConnected)) {
            response.setBio(profile.getBio());
            response.setLookingFor(toLookingFor(profile.getLookingFor()));
            response.setInterests(new ArrayList<>(profile.getInterests() == null ? List.of() : profile.getInterests()));
            response.setLanguages(toLanguageItems(profile));
        }

        if (canView(visibility.getSocialLinks(), isOwner, isConnected)) {
            response.setSocialLinks(toSocialLinks(profile.getSocialLinks()));
        }

        if (canView(visibility.getExperience(), isOwner, isConnected)) {
            response.setExperiences(toExperiences(profile));
        }

        if (canView(visibility.getProjects(), isOwner, isConnected)) {
            response.setProjects(toProjects(profile));
        }

        if (canView(visibility.getCollaborations(), isOwner, isConnected)) {
            response.setCollaborations(toCollaborations(profile));
        }

        if (canView(visibility.getCommunities(), isOwner, isConnected)) {
            response.setCommunities(toCommunities(targetUser));
        }

        if (canView(visibility.getActivity(), isOwner, isConnected)) {
            response.setStats(buildStats(targetUser));
            response.setRecentActivity(buildRecentActivity(targetUser));
        }

        return response;
    }

    @Transactional(readOnly = true)
    public ProfileSummaryResponse getProfileSummary(Long targetUserId, Long viewerUserId) {
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("User not found: " + targetUserId));
        UserProfile profile = getOrFallbackProfile(targetUser);

        boolean isSelf = viewerUserId != null && viewerUserId.equals(targetUserId);
        boolean isConnected = !isSelf && viewerUserId != null && areUsersMatched(viewerUserId, targetUserId);
        boolean canMessage = !isSelf && isConnected;
        UserProfile.VisibilitySettings visibility = profile.getVisibilitySettings() == null
                ? new UserProfile.VisibilitySettings()
                : profile.getVisibilitySettings();

        ProfileSummaryResponse summary = new ProfileSummaryResponse();
        summary.setUserId(targetUserId);
        summary.setName(profile.getName());
        summary.setProfilePictureUrl(profile.getProfilePictureUrl());
        summary.setHeadline(profile.getHeadline());
        summary.setLocation(profile.getLocation());
        summary.setUniversity(profile.getUniversity());
        summary.setMajor(profile.getMajor());
        summary.setSelf(isSelf);
        summary.setConnected(isConnected);
        summary.setCanMessage(canMessage);

        if (canView(visibility.getAbout(), isSelf, isConnected)) {
            summary.setBio(profile.getBio());
            summary.setTopInterests(profile.getInterests() == null
                    ? new ArrayList<>()
                    : profile.getInterests().stream().limit(5).toList());
        }

        if (canView(visibility.getActivity(), isSelf, isConnected)) {
            summary.setStats(buildStats(targetUser));
        }
        return summary;
    }

    private UserProfile getOrFallbackProfile(User targetUser) {
        return userProfileRepository.findByUserId(targetUser.getId())
                .orElseGet(() -> {
                    UserProfile fallback = new UserProfile();
                    fallback.setUser(targetUser);
                    fallback.setName(targetUser.getName());
                    fallback.setEmail(targetUser.getEmail());
                    fallback.setMajor("");
                    fallback.setLocation("");
                    fallback.setBio("");
                    return fallback;
                });
    }

    private boolean areUsersMatched(Long firstUserId, Long secondUserId) {
        if (firstUserId == null || secondUserId == null || firstUserId.equals(secondUserId)) {
            return false;
        }

        User lower = userRepository.findById(Math.min(firstUserId, secondUserId))
                .orElseThrow(() -> new RuntimeException("User not found"));
        User higher = userRepository.findById(Math.max(firstUserId, secondUserId))
                .orElseThrow(() -> new RuntimeException("User not found"));

        return studyMatchRepository.findByUser1AndUser2(lower, higher)
                .map(studyMatch -> studyMatch.getStatus() == StudyMatch.MatchStatus.MATCHED)
                .orElse(false);
    }

    private boolean canView(ProfileSectionVisibility visibility, boolean isOwner, boolean isConnected) {
        ProfileSectionVisibility effective = visibility == null ? ProfileSectionVisibility.PUBLIC : visibility;
        if (isOwner) {
            return true;
        }
        return switch (effective) {
            case PUBLIC -> true;
            case CONNECTIONS -> isConnected;
            case PRIVATE -> false;
        };
    }

    private ProfilePublicResponse.LookingForItem toLookingFor(UserProfile.LookingFor source) {
        ProfilePublicResponse.LookingForItem target = new ProfilePublicResponse.LookingForItem();
        if (source == null) {
            return target;
        }
        target.setStudyPartner(source.isStudyPartner());
        target.setLanguageExchange(source.isLanguageExchange());
        target.setFriendship(source.isFriendship());
        target.setNetworking(source.isNetworking());
        target.setCommunity(source.isCommunity());
        return target;
    }

    private ProfilePublicResponse.SocialLinksItem toSocialLinks(UserProfile.SocialLinks source) {
        ProfilePublicResponse.SocialLinksItem target = new ProfilePublicResponse.SocialLinksItem();
        if (source == null) {
            return target;
        }
        target.setGithub(source.getGithub());
        target.setLinkedin(source.getLinkedin());
        target.setInstagram(source.getInstagram());
        return target;
    }

    private List<ProfilePublicResponse.LanguageItem> toLanguageItems(UserProfile profile) {
        List<ProfilePublicResponse.LanguageItem> languages = new ArrayList<>();
        if (profile.getLanguages() == null) {
            return languages;
        }
        profile.getLanguages().forEach(item -> {
            if (item == null) {
                return;
            }
            ProfilePublicResponse.LanguageItem language = new ProfilePublicResponse.LanguageItem();
            language.setName(item.getName());
            language.setLevel(item.getLevel());
            languages.add(language);
        });
        return languages;
    }

    private List<ProfilePublicResponse.ExperienceItem> toExperiences(UserProfile profile) {
        List<ProfilePublicResponse.ExperienceItem> output = new ArrayList<>();
        if (profile.getExperiences() == null) {
            return output;
        }
        profile.getExperiences().forEach(item -> {
            if (item == null) {
                return;
            }
            ProfilePublicResponse.ExperienceItem experience = new ProfilePublicResponse.ExperienceItem();
            experience.setTitle(item.getTitle());
            experience.setOrganization(item.getOrganization());
            experience.setLocation(item.getLocation());
            experience.setStartDate(item.getStartDate());
            experience.setEndDate(item.getEndDate());
            experience.setCurrent(item.isCurrent());
            experience.setDescription(item.getDescription());
            output.add(experience);
        });
        return output;
    }

    private List<ProfilePublicResponse.ProjectItem> toProjects(UserProfile profile) {
        List<ProfilePublicResponse.ProjectItem> output = new ArrayList<>();
        if (profile.getProjects() == null) {
            return output;
        }
        profile.getProjects().forEach(item -> {
            if (item == null) {
                return;
            }
            ProfilePublicResponse.ProjectItem project = new ProfilePublicResponse.ProjectItem();
            project.setTitle(item.getTitle());
            project.setShortDescription(item.getShortDescription());
            project.setDescription(item.getDescription());
            project.setCoverImageUrl(item.getCoverImageUrl());
            project.setProjectUrl(item.getProjectUrl());
            project.setRepoUrl(item.getRepoUrl());
            project.setTechStack(item.getTechStack() == null ? new ArrayList<>() : new ArrayList<>(item.getTechStack()));
            project.setStartDate(item.getStartDate());
            project.setEndDate(item.getEndDate());
            output.add(project);
        });
        return output;
    }

    private List<ProfilePublicResponse.CollaborationItem> toCollaborations(UserProfile profile) {
        List<ProfilePublicResponse.CollaborationItem> output = new ArrayList<>();
        if (profile.getCollaborations() == null) {
            return output;
        }
        profile.getCollaborations().forEach(item -> {
            if (item == null) {
                return;
            }
            ProfilePublicResponse.CollaborationItem collaboration = new ProfilePublicResponse.CollaborationItem();
            collaboration.setTitle(item.getTitle());
            collaboration.setPartnerName(item.getPartnerName());
            collaboration.setCollaborationType(item.getCollaborationType());
            collaboration.setDescription(item.getDescription());
            collaboration.setStartDate(item.getStartDate());
            collaboration.setEndDate(item.getEndDate());
            collaboration.setResultSummary(item.getResultSummary());
            collaboration.setReferenceUrl(item.getReferenceUrl());
            output.add(collaboration);
        });
        return output;
    }

    private List<ProfilePublicResponse.CommunityItem> toCommunities(User targetUser) {
        Map<Long, ProfilePublicResponse.CommunityItem> byCommunityId = new LinkedHashMap<>();

        List<Community> owned = communityRepository.findTop10ByOwnerOrderByCreatedAtDesc(targetUser);
        for (Community community : owned) {
            ProfilePublicResponse.CommunityItem item = new ProfilePublicResponse.CommunityItem();
            item.setId(community.getId());
            item.setName(community.getName());
            item.setRole("OWNER");
            item.setTimestamp(community.getCreatedAt());
            byCommunityId.put(community.getId(), item);
        }

        List<CommunityMember> joined = communityMemberRepository.findTop10ByUserOrderByJoinedAtDesc(targetUser);
        for (CommunityMember membership : joined) {
            Community community = membership.getCommunity();
            if (community == null || byCommunityId.containsKey(community.getId())) {
                continue;
            }
            ProfilePublicResponse.CommunityItem item = new ProfilePublicResponse.CommunityItem();
            item.setId(community.getId());
            item.setName(community.getName());
            item.setRole("MEMBER");
            item.setTimestamp(membership.getJoinedAt());
            byCommunityId.put(community.getId(), item);
        }

        return byCommunityId.values().stream()
                .sorted(Comparator.comparing(
                        ProfilePublicResponse.CommunityItem::getTimestamp,
                        Comparator.nullsLast(LocalDateTime::compareTo)).reversed())
                .limit(10)
                .toList();
    }

    private ProfileStatsResponse buildStats(User targetUser) {
        long posts = postRepository.countByAuthor(targetUser);
        long comments = commentRepository.countByAuthor(targetUser);
        long joined = communityMemberRepository.countByUser(targetUser);
        long owned = communityRepository.countByOwner(targetUser);
        long matches = studyMatchRepository.countByUser1AndStatus(targetUser, StudyMatch.MatchStatus.MATCHED)
                + studyMatchRepository.countByUser2AndStatus(targetUser, StudyMatch.MatchStatus.MATCHED);

        return new ProfileStatsResponse(posts, comments, joined, owned, matches);
    }

    private List<RecentActivityItem> buildRecentActivity(User targetUser) {
        List<RecentActivityItem> activity = new ArrayList<>();

        postRepository.findTop10ByAuthorOrderByCreatedAtDesc(targetUser).forEach(post ->
                activity.add(new RecentActivityItem(
                        RecentActivityItem.ActivityType.POST,
                        "Published a post",
                        post.getCreatedAt(),
                        post.getId(),
                        "/authenticated/communities/" + post.getCommunity().getId()
                )));

        commentRepository.findTop10ByAuthorOrderByCreatedAtDesc(targetUser).forEach(comment ->
                activity.add(new RecentActivityItem(
                        RecentActivityItem.ActivityType.COMMENT,
                        "Added a comment",
                        comment.getCreatedAt(),
                        comment.getId(),
                        "/authenticated/communities/" + comment.getPost().getCommunity().getId()
                )));

        communityRepository.findTop10ByOwnerOrderByCreatedAtDesc(targetUser).forEach(community ->
                activity.add(new RecentActivityItem(
                        RecentActivityItem.ActivityType.COMMUNITY_CREATE,
                        "Created community: " + community.getName(),
                        community.getCreatedAt(),
                        community.getId(),
                        "/authenticated/communities/" + community.getId()
                )));

        communityMemberRepository.findTop10ByUserOrderByJoinedAtDesc(targetUser).forEach(membership ->
                activity.add(new RecentActivityItem(
                        RecentActivityItem.ActivityType.COMMUNITY_JOIN,
                        "Joined community: " + membership.getCommunity().getName(),
                        membership.getJoinedAt(),
                        membership.getCommunity().getId(),
                        "/authenticated/communities/" + membership.getCommunity().getId()
                )));

        return activity.stream()
                .sorted(Comparator.comparing(
                        RecentActivityItem::getTimestamp,
                        Comparator.nullsLast(LocalDateTime::compareTo)).reversed())
                .limit(10)
                .toList();
    }
}