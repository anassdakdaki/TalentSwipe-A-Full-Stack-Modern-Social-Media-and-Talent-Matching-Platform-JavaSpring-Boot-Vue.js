package com.example.biblov1.service;

import com.example.biblov1.model.Community;
import com.example.biblov1.model.CommunityMember;
import com.example.biblov1.model.User;
import com.example.biblov1.model.UserProfile;
import com.example.biblov1.payload.request.onboarding.CompleteOnboardingRequest;
import com.example.biblov1.payload.response.onboarding.CompleteOnboardingResponse;
import com.example.biblov1.payload.response.onboarding.OnboardingOptionsResponse;
import com.example.biblov1.payload.response.onboarding.OnboardingStatusResponse;
import com.example.biblov1.repository.CommunityMemberRepository;
import com.example.biblov1.repository.CommunityRepository;
import com.example.biblov1.repository.UserProfileRepository;
import com.example.biblov1.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class OnboardingService {
    private static final int MAX_INTERESTS = 25;
    private static final int MAX_TAGS = 25;
    private static final int MAX_COMMUNITIES = 40;

    private static final List<String> DEFAULT_INTEREST_SUGGESTIONS = List.of(
            "Software Engineering", "Product Management", "Data Science", "AI", "Machine Learning",
            "Cybersecurity", "Cloud", "UI/UX", "Startup Building", "Career Growth",
            "Interview Prep", "Open Source", "Business Strategy", "Marketing", "Finance",
            "Photography", "Language Learning", "Design", "Writing", "Public Speaking"
    );

    private static final List<ContentDefinition> CONTENT_DEFINITIONS = List.of(
            new ContentDefinition("studyGuides", "Study Guides", "Practical study plans, summaries, and resource breakdowns."),
            new ContentDefinition("projectShowcases", "Project Showcases", "Real projects, demos, and implementation walk-throughs."),
            new ContentDefinition("communityDiscussions", "Community Discussions", "Conversations, Q&A threads, and peer advice."),
            new ContentDefinition("events", "Events & Challenges", "Hackathons, meetups, and structured learning events."),
            new ContentDefinition("careerOpportunities", "Career Opportunities", "Internships, collaboration calls, and hiring leads.")
    );

    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;
    private final CommunityRepository communityRepository;
    private final CommunityMemberRepository communityMemberRepository;
    private final CommunityService communityService;

    public OnboardingService(UserProfileRepository userProfileRepository,
                             UserRepository userRepository,
                             CommunityRepository communityRepository,
                             CommunityMemberRepository communityMemberRepository,
                             CommunityService communityService) {
        this.userProfileRepository = userProfileRepository;
        this.userRepository = userRepository;
        this.communityRepository = communityRepository;
        this.communityMemberRepository = communityMemberRepository;
        this.communityService = communityService;
    }

    @Transactional
    public OnboardingStatusResponse getStatus(Long userId) {
        UserProfile profile = getOrCreateProfile(userId);
        return new OnboardingStatusResponse(profile.isOnboardingCompleted());
    }

    @Transactional
    public OnboardingOptionsResponse getOptions(Long userId) {
        UserProfile profile = getOrCreateProfile(userId);
        User user = profile.getUser();

        OnboardingOptionsResponse response = new OnboardingOptionsResponse();
        response.setCompleted(profile.isOnboardingCompleted());
        response.setCurrentInterests(new ArrayList<>(profile.getInterests() == null ? List.of() : profile.getInterests()));
        response.setCurrentPreferredCommunityTags(
                new ArrayList<>(profile.getPreferredCommunityTags() == null ? List.of() : profile.getPreferredCommunityTags())
        );
        response.setOpenToCollaborate(profile.isOpenToCollaborate());
        response.setCurrentLookingFor(mapLookingFor(profile.getLookingFor()));
        response.setCurrentContentPreferences(mapContentPreferences(profile.getContentPreferences()));
        response.setContentOptions(buildContentOptions());

        List<CommunityMember> memberships = communityMemberRepository.findByUser(user);
        Set<Long> joinedCommunityIds = memberships.stream()
                .map(membership -> membership.getCommunity() == null ? null : membership.getCommunity().getId())
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        response.setJoinedCommunityIds(new ArrayList<>(joinedCommunityIds));

        List<String> normalizedInterests = normalizePhrases(profile.getInterests(), MAX_INTERESTS, 40);
        List<String> normalizedPreferredTags = normalizePhrases(profile.getPreferredCommunityTags(), MAX_TAGS, 40);
        Set<String> signals = new LinkedHashSet<>();
        signals.addAll(normalizedInterests.stream().map(this::normalizeToken).filter(s -> !s.isBlank()).toList());
        signals.addAll(normalizedPreferredTags.stream().map(this::normalizeToken).filter(s -> !s.isBlank()).toList());

        List<Community> communities = communityRepository.findAll();
        response.setCommunityOptions(communities.stream()
                .map(community -> buildCommunityOption(community, joinedCommunityIds, signals))
                .sorted(Comparator
                        .comparing(OnboardingOptionsResponse.CommunityOptionItem::isRecommended).reversed()
                        .thenComparing(OnboardingOptionsResponse.CommunityOptionItem::getMembersCount, Comparator.reverseOrder())
                        .thenComparing(OnboardingOptionsResponse.CommunityOptionItem::getName, Comparator.nullsLast(String::compareToIgnoreCase)))
                .collect(Collectors.toCollection(ArrayList::new)));

        response.setInterestSuggestions(buildInterestSuggestions(communities));
        response.setCommunityTagSuggestions(buildCommunityTagSuggestions(communities));
        return response;
    }

    @Transactional
    public CompleteOnboardingResponse completeOnboarding(Long userId, CompleteOnboardingRequest request) {
        UserProfile profile = getOrCreateProfile(userId);

        List<String> interests = normalizePhrases(request.getInterests(), MAX_INTERESTS, 40);
        if (interests.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please choose at least one interest.");
        }

        List<String> preferredTags = normalizePhrases(request.getPreferredCommunityTags(), MAX_TAGS, 40);
        List<Long> selectedCommunityIds = normalizeCommunityIds(request.getSelectedCommunityIds());

        UserProfile.LookingFor lookingFor = profile.getLookingFor() == null ? new UserProfile.LookingFor() : profile.getLookingFor();
        if (request.getLookingFor() != null) {
            lookingFor.setStudyPartner(request.getLookingFor().isStudyPartner());
            lookingFor.setLanguageExchange(request.getLookingFor().isLanguageExchange());
            lookingFor.setFriendship(request.getLookingFor().isFriendship());
            lookingFor.setNetworking(request.getLookingFor().isNetworking());
            lookingFor.setCommunity(request.getLookingFor().isCommunity());
        }
        profile.setLookingFor(lookingFor);

        UserProfile.ContentPreferences contentPreferences = profile.getContentPreferences() == null
                ? new UserProfile.ContentPreferences()
                : profile.getContentPreferences();
        if (request.getContentPreferences() != null) {
            contentPreferences.setStudyGuides(request.getContentPreferences().isStudyGuides());
            contentPreferences.setProjectShowcases(request.getContentPreferences().isProjectShowcases());
            contentPreferences.setCommunityDiscussions(request.getContentPreferences().isCommunityDiscussions());
            contentPreferences.setEvents(request.getContentPreferences().isEvents());
            contentPreferences.setCareerOpportunities(request.getContentPreferences().isCareerOpportunities());
        }
        profile.setContentPreferences(contentPreferences);

        boolean openToCollaborate = request.getOpenToCollaborate() != null
                ? request.getOpenToCollaborate()
                : lookingFor.isNetworking() || lookingFor.isStudyPartner() || lookingFor.isCommunity();

        profile.setOpenToCollaborate(openToCollaborate);
        profile.setInterests(interests);
        profile.setPreferredCommunityTags(preferredTags);

        List<Long> joinedCommunityIds = new ArrayList<>();
        for (Long communityId : selectedCommunityIds) {
            Community community = communityRepository.findById(communityId).orElse(null);
            if (community == null) {
                continue;
            }
            try {
                if (!communityService.isUserMemberOfCommunity(userId, communityId)) {
                    communityService.joinCommunity(communityId, userId);
                }
                joinedCommunityIds.add(communityId);
            } catch (IllegalStateException ignored) {
                joinedCommunityIds.add(communityId);
            }
        }

        List<Long> featuredIds = selectedCommunityIds.stream().limit(8).collect(Collectors.toCollection(ArrayList::new));
        profile.setFeaturedCommunityIds(featuredIds);
        profile.setOnboardingCompleted(true);
        profile.setOnboardingCompletedAt(LocalDateTime.now());
        userProfileRepository.save(profile);

        CompleteOnboardingResponse response = new CompleteOnboardingResponse();
        response.setCompleted(true);
        response.setJoinedCommunitiesCount(joinedCommunityIds.size());
        response.setJoinedCommunityIds(joinedCommunityIds);
        return response;
    }

    private UserProfile getOrCreateProfile(Long userId) {
        return userProfileRepository.findByUserId(userId).orElseGet(() -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

            UserProfile profile = new UserProfile();
            profile.setUser(user);
            profile.setName(user.getName());
            profile.setEmail(user.getEmail());
            profile.setMajor("");
            profile.setLocation("");
            profile.setBio("");
            profile.setHeadline("");
            profile.setOpenToCollaborate(false);
            profile.setInterests(new ArrayList<>());
            profile.setLanguages(new ArrayList<>());
            profile.setFeaturedCommunityIds(new ArrayList<>());
            profile.setPreferredCommunityTags(new ArrayList<>());
            profile.setOnboardingCompleted(false);
            profile.setOnboardingCompletedAt(null);
            profile.setExperiences(new ArrayList<>());
            profile.setProjects(new ArrayList<>());
            profile.setCollaborations(new ArrayList<>());
            profile.setLookingFor(new UserProfile.LookingFor());
            profile.setSocialLinks(new UserProfile.SocialLinks());
            profile.setContentPreferences(new UserProfile.ContentPreferences());
            profile.setVisibilitySettings(new UserProfile.VisibilitySettings());
            return userProfileRepository.save(profile);
        });
    }

    private OnboardingOptionsResponse.CommunityOptionItem buildCommunityOption(Community community,
                                                                               Set<Long> joinedCommunityIds,
                                                                               Set<String> signals) {
        OnboardingOptionsResponse.CommunityOptionItem item = new OnboardingOptionsResponse.CommunityOptionItem();
        item.setId(community.getId());
        item.setName(community.getName());
        item.setDescription(community.getDescription());
        item.setImageUrl(community.getImageUrl());

        List<String> tags = community.getTags() == null
                ? List.of()
                : community.getTags().stream().filter(Objects::nonNull).toList();
        item.setTags(new ArrayList<>(tags));
        item.setMembersCount(communityMemberRepository.countByCommunity(community));
        item.setJoined(joinedCommunityIds.contains(community.getId()));

        List<String> matchedTags = tags.stream()
                .filter(tag -> signals.contains(normalizeToken(tag)))
                .distinct()
                .limit(3)
                .toList();
        boolean recommended = !matchedTags.isEmpty();
        item.setRecommended(recommended);
        if (recommended) {
            item.setRecommendationReason("Matches your interests: " + String.join(", ", matchedTags));
        }

        return item;
    }

    private List<String> buildInterestSuggestions(List<Community> communities) {
        LinkedHashSet<String> suggestions = new LinkedHashSet<>(DEFAULT_INTEREST_SUGGESTIONS);
        for (String tag : buildCommunityTagSuggestions(communities)) {
            if (suggestions.size() >= 30) {
                break;
            }
            suggestions.add(toDisplayTag(tag));
        }
        return new ArrayList<>(suggestions);
    }

    private List<String> buildCommunityTagSuggestions(List<Community> communities) {
        Map<String, Long> frequency = new LinkedHashMap<>();
        for (Community community : communities) {
            if (community.getTags() == null) {
                continue;
            }
            for (String tag : community.getTags()) {
                String normalized = normalizeToken(tag);
                if (!normalized.isBlank()) {
                    frequency.put(normalized, frequency.getOrDefault(normalized, 0L) + 1L);
                }
            }
        }

        return frequency.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .limit(30)
                .map(this::toDisplayTag)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private List<Long> normalizeCommunityIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        LinkedHashSet<Long> unique = new LinkedHashSet<>();
        for (Long id : ids) {
            if (id != null && id > 0) {
                unique.add(id);
            }
            if (unique.size() >= MAX_COMMUNITIES) {
                break;
            }
        }
        return new ArrayList<>(unique);
    }

    private List<String> normalizePhrases(List<String> values, int maxItems, int maxLength) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }

        LinkedHashSet<String> unique = new LinkedHashSet<>();
        for (String raw : values) {
            if (raw == null) {
                continue;
            }
            String normalized = raw.replaceAll("[\\r\\n\\t]+", " ")
                    .replaceAll("\\s+", " ")
                    .trim();
            if (normalized.isBlank()) {
                continue;
            }
            if (normalized.length() > maxLength) {
                normalized = normalized.substring(0, maxLength).trim();
            }
            if (!normalized.isBlank()) {
                unique.add(normalized);
            }
            if (unique.size() >= maxItems) {
                break;
            }
        }

        return new ArrayList<>(unique);
    }

    private String normalizeToken(String value) {
        if (value == null) {
            return "";
        }
        return value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "").trim();
    }

    private String toDisplayTag(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }

        String spaced = value.replaceAll("([a-z])([A-Z])", "$1 $2")
                .replaceAll("[_\\-]+", " ")
                .replaceAll("\\s+", " ")
                .trim();
        if (spaced.isBlank()) {
            return "";
        }

        String[] parts = spaced.split(" ");
        List<String> titled = new ArrayList<>();
        for (String part : parts) {
            if (part.isBlank()) {
                continue;
            }
            if (part.length() == 1) {
                titled.add(part.toUpperCase(Locale.ROOT));
            } else {
                titled.add(part.substring(0, 1).toUpperCase(Locale.ROOT) + part.substring(1).toLowerCase(Locale.ROOT));
            }
        }
        return String.join(" ", titled);
    }

    private OnboardingOptionsResponse.LookingForItem mapLookingFor(UserProfile.LookingFor source) {
        OnboardingOptionsResponse.LookingForItem item = new OnboardingOptionsResponse.LookingForItem();
        if (source == null) {
            return item;
        }
        item.setStudyPartner(source.isStudyPartner());
        item.setLanguageExchange(source.isLanguageExchange());
        item.setFriendship(source.isFriendship());
        item.setNetworking(source.isNetworking());
        item.setCommunity(source.isCommunity());
        return item;
    }

    private OnboardingOptionsResponse.ContentPreferencesItem mapContentPreferences(UserProfile.ContentPreferences source) {
        OnboardingOptionsResponse.ContentPreferencesItem item = new OnboardingOptionsResponse.ContentPreferencesItem();
        if (source == null) {
            return item;
        }
        item.setStudyGuides(source.isStudyGuides());
        item.setProjectShowcases(source.isProjectShowcases());
        item.setCommunityDiscussions(source.isCommunityDiscussions());
        item.setEvents(source.isEvents());
        item.setCareerOpportunities(source.isCareerOpportunities());
        return item;
    }

    private List<OnboardingOptionsResponse.ContentOptionItem> buildContentOptions() {
        return CONTENT_DEFINITIONS.stream().map(definition -> {
            OnboardingOptionsResponse.ContentOptionItem item = new OnboardingOptionsResponse.ContentOptionItem();
            item.setKey(definition.key());
            item.setLabel(definition.label());
            item.setDescription(definition.description());
            return item;
        }).collect(Collectors.toCollection(ArrayList::new));
    }

    private record ContentDefinition(String key, String label, String description) {}
}