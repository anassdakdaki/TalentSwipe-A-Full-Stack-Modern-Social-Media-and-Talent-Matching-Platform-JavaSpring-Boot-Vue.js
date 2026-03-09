package com.example.biblov1.service;

import com.example.biblov1.model.ProfileCollaboration;
import com.example.biblov1.model.ProfileExperience;
import com.example.biblov1.model.ProfileProject;
import com.example.biblov1.model.ProfileSectionVisibility;
import com.example.biblov1.model.User;
import com.example.biblov1.model.UserProfile;
import com.example.biblov1.payload.request.profile.UpdateProfileEditorRequest;
import com.example.biblov1.payload.response.profile.ProfileEditorResponse;
import com.example.biblov1.repository.UserProfileRepository;
import com.example.biblov1.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

@Service
public class ProfileEditorService {
    private static final int MAX_HEADLINE = 120;
    private static final int MAX_SHORT_DESCRIPTION = 220;
    private static final int MAX_LONG_TEXT = 2000;
    private static final int MAX_TECH_STACK_ITEMS = 20;
    private static final int MAX_TECH_STACK_ITEM_LENGTH = 30;

    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;

    public ProfileEditorService(UserProfileRepository userProfileRepository, UserRepository userRepository) {
        this.userProfileRepository = userProfileRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public ProfileEditorResponse loadEditorProfile(Long userId) {
        UserProfile profile = getOrCreateProfile(userId);
        return toEditorResponse(profile);
    }

    @Transactional
    public ProfileEditorResponse updateEditorProfile(Long userId, UpdateProfileEditorRequest request) {
        UserProfile profile = getOrCreateProfile(userId);

        profile.setName(validateRequiredText(request.getName(), 120, "name"));
        profile.setAge(request.getAge());
        profile.setGender(validateOptionalText(request.getGender(), 50, "gender"));
        profile.setUniversity(validateOptionalText(request.getUniversity(), 120, "university"));
        profile.setMajor(validateRequiredText(request.getMajor(), 120, "major"));
        profile.setLocation(validateOptionalText(request.getLocation(), 120, "location"));
        profile.setBio(validateOptionalLongText(request.getBio(), "bio"));
        profile.setHeadline(validateOptionalText(request.getHeadline(), MAX_HEADLINE, "headline"));
        profile.setOpenToCollaborate(Boolean.TRUE.equals(request.getOpenToCollaborate()));
        profile.setProfilePictureUrl(validateOptionalUrl(request.getProfilePictureUrl(), "profilePictureUrl"));

        profile.setInterests(sanitizeStringList(request.getInterests(), 80));
        profile.setFeaturedCommunityIds(sanitizeFeaturedCommunities(request.getFeaturedCommunityIds()));

        profile.setLanguages(mapLanguages(request.getLanguages()));
        profile.setLookingFor(mapLookingFor(request.getLookingFor()));
        profile.setSocialLinks(mapSocialLinks(request.getSocialLinks()));
        profile.setVisibilitySettings(mapVisibilitySettings(request.getVisibilitySettings()));
        profile.setExperiences(mapExperiences(profile, request.getExperiences()));
        profile.setProjects(mapProjects(profile, request.getProjects()));
        profile.setCollaborations(mapCollaborations(profile, request.getCollaborations()));

        UserProfile saved = userProfileRepository.save(profile);
        return toEditorResponse(saved);
    }

    private UserProfile getOrCreateProfile(Long userId) {
        return userProfileRepository.findByUserId(userId).orElseGet(() -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found: " + userId));
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
            profile.setLookingFor(new UserProfile.LookingFor());
            profile.setSocialLinks(new UserProfile.SocialLinks());
            profile.setVisibilitySettings(new UserProfile.VisibilitySettings());
            return userProfileRepository.save(profile);
        });
    }

    private List<UserProfile.LanguageProficiency> mapLanguages(List<UpdateProfileEditorRequest.LanguageItem> items) {
        List<UserProfile.LanguageProficiency> mapped = new ArrayList<>();
        if (items == null) {
            return mapped;
        }

        for (UpdateProfileEditorRequest.LanguageItem item : items) {
            if (item == null) {
                continue;
            }
            String name = trimToNull(item.getName());
            String level = trimToNull(item.getLevel());
            if (name == null || level == null) {
                continue;
            }

            UserProfile.LanguageProficiency language = new UserProfile.LanguageProficiency();
            language.setName(validateRequiredText(name, 80, "language.name"));
            language.setLevel(validateRequiredText(level, 40, "language.level"));
            mapped.add(language);
        }
        return mapped;
    }

    private UserProfile.LookingFor mapLookingFor(UpdateProfileEditorRequest.LookingForItem item) {
        UserProfile.LookingFor target = new UserProfile.LookingFor();
        if (item == null) {
            return target;
        }
        target.setStudyPartner(item.isStudyPartner());
        target.setLanguageExchange(item.isLanguageExchange());
        target.setFriendship(item.isFriendship());
        target.setNetworking(item.isNetworking());
        target.setCommunity(item.isCommunity());
        return target;
    }

    private UserProfile.SocialLinks mapSocialLinks(UpdateProfileEditorRequest.SocialLinksItem item) {
        UserProfile.SocialLinks links = new UserProfile.SocialLinks();
        if (item == null) {
            return links;
        }
        links.setGithub(validateOptionalUrl(item.getGithub(), "socialLinks.github"));
        links.setLinkedin(validateOptionalUrl(item.getLinkedin(), "socialLinks.linkedin"));
        links.setInstagram(validateOptionalUrl(item.getInstagram(), "socialLinks.instagram"));
        return links;
    }

    private UserProfile.VisibilitySettings mapVisibilitySettings(UpdateProfileEditorRequest.VisibilitySettingsItem item) {
        UserProfile.VisibilitySettings settings = new UserProfile.VisibilitySettings();
        if (item == null) {
            return settings;
        }
        settings.setAbout(orDefaultVisibility(item.getAbout()));
        settings.setExperience(orDefaultVisibility(item.getExperience()));
        settings.setProjects(orDefaultVisibility(item.getProjects()));
        settings.setCollaborations(orDefaultVisibility(item.getCollaborations()));
        settings.setCommunities(orDefaultVisibility(item.getCommunities()));
        settings.setActivity(orDefaultVisibility(item.getActivity()));
        settings.setSocialLinks(orDefaultVisibility(item.getSocialLinks()));
        return settings;
    }

    private List<ProfileExperience> mapExperiences(UserProfile profile, List<UpdateProfileEditorRequest.ExperienceItem> items) {
        List<ProfileExperience> mapped = new ArrayList<>();
        if (items == null) {
            return mapped;
        }
        for (int i = 0; i < items.size(); i++) {
            UpdateProfileEditorRequest.ExperienceItem item = items.get(i);
            if (item == null) {
                continue;
            }
            String title = trimToNull(item.getTitle());
            String organization = trimToNull(item.getOrganization());
            if (title == null || organization == null) {
                continue;
            }

            ProfileExperience experience = new ProfileExperience();
            experience.setProfile(profile);
            experience.setTitle(validateRequiredText(title, 120, "experience.title"));
            experience.setOrganization(validateRequiredText(organization, 120, "experience.organization"));
            experience.setLocation(validateOptionalText(item.getLocation(), 120, "experience.location"));
            experience.setStartDate(item.getStartDate());
            experience.setEndDate(item.getEndDate());
            experience.setCurrent(item.isCurrent());
            experience.setDescription(validateOptionalLongText(item.getDescription(), "experience.description"));
            experience.setSortOrder(i);
            mapped.add(experience);
        }
        return mapped;
    }

    private List<ProfileProject> mapProjects(UserProfile profile, List<UpdateProfileEditorRequest.ProjectItem> items) {
        List<ProfileProject> mapped = new ArrayList<>();
        if (items == null) {
            return mapped;
        }
        for (int i = 0; i < items.size(); i++) {
            UpdateProfileEditorRequest.ProjectItem item = items.get(i);
            if (item == null) {
                continue;
            }
            String title = trimToNull(item.getTitle());
            if (title == null) {
                continue;
            }

            ProfileProject project = new ProfileProject();
            project.setProfile(profile);
            project.setTitle(validateRequiredText(title, 120, "project.title"));
            project.setShortDescription(validateOptionalText(item.getShortDescription(), MAX_SHORT_DESCRIPTION, "project.shortDescription"));
            project.setDescription(validateOptionalLongText(item.getDescription(), "project.description"));
            project.setCoverImageUrl(validateOptionalUrl(item.getCoverImageUrl(), "project.coverImageUrl"));
            project.setProjectUrl(validateOptionalUrl(item.getProjectUrl(), "project.projectUrl"));
            project.setRepoUrl(validateOptionalUrl(item.getRepoUrl(), "project.repoUrl"));
            project.setTechStack(sanitizeTechStack(item.getTechStack()));
            project.setStartDate(item.getStartDate());
            project.setEndDate(item.getEndDate());
            project.setSortOrder(i);
            mapped.add(project);
        }
        return mapped;
    }

    private List<ProfileCollaboration> mapCollaborations(UserProfile profile, List<UpdateProfileEditorRequest.CollaborationItem> items) {
        List<ProfileCollaboration> mapped = new ArrayList<>();
        if (items == null) {
            return mapped;
        }
        for (int i = 0; i < items.size(); i++) {
            UpdateProfileEditorRequest.CollaborationItem item = items.get(i);
            if (item == null) {
                continue;
            }
            String title = trimToNull(item.getTitle());
            if (title == null) {
                continue;
            }

            ProfileCollaboration collaboration = new ProfileCollaboration();
            collaboration.setProfile(profile);
            collaboration.setTitle(validateRequiredText(title, 120, "collaboration.title"));
            collaboration.setPartnerName(validateOptionalText(item.getPartnerName(), 120, "collaboration.partnerName"));
            collaboration.setCollaborationType(validateOptionalText(item.getCollaborationType(), 120, "collaboration.collaborationType"));
            collaboration.setDescription(validateOptionalLongText(item.getDescription(), "collaboration.description"));
            collaboration.setStartDate(item.getStartDate());
            collaboration.setEndDate(item.getEndDate());
            collaboration.setResultSummary(validateOptionalLongText(item.getResultSummary(), "collaboration.resultSummary"));
            collaboration.setReferenceUrl(validateOptionalUrl(item.getReferenceUrl(), "collaboration.referenceUrl"));
            collaboration.setSortOrder(i);
            mapped.add(collaboration);
        }
        return mapped;
    }

    private List<String> sanitizeTechStack(List<String> rawTechStack) {
        List<String> normalized = sanitizeStringList(rawTechStack, MAX_TECH_STACK_ITEM_LENGTH);
        if (normalized.size() > MAX_TECH_STACK_ITEMS) {
            throw new IllegalArgumentException("project.techStack supports up to 20 items");
        }
        return normalized;
    }

    private List<Long> sanitizeFeaturedCommunities(List<Long> rawCommunityIds) {
        if (rawCommunityIds == null) {
            return new ArrayList<>();
        }
        LinkedHashSet<Long> unique = new LinkedHashSet<>();
        for (Long id : rawCommunityIds) {
            if (id != null && id > 0) {
                unique.add(id);
            }
        }
        return new ArrayList<>(unique);
    }

    private List<String> sanitizeStringList(List<String> rawValues, int maxLengthPerItem) {
        if (rawValues == null) {
            return new ArrayList<>();
        }
        LinkedHashSet<String> uniqueValues = new LinkedHashSet<>();
        for (String value : rawValues) {
            String normalized = trimToNull(value);
            if (normalized == null) {
                continue;
            }
            if (normalized.length() > maxLengthPerItem) {
                throw new IllegalArgumentException("List item exceeds max length: " + maxLengthPerItem);
            }
            uniqueValues.add(normalized);
        }
        return new ArrayList<>(uniqueValues);
    }

    private String validateRequiredText(String rawValue, int maxLength, String fieldName) {
        String normalized = trimToNull(rawValue);
        if (normalized == null) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        if (normalized.length() > maxLength) {
            throw new IllegalArgumentException(fieldName + " exceeds max length " + maxLength);
        }
        return normalized;
    }

    private String validateOptionalText(String rawValue, int maxLength, String fieldName) {
        String normalized = trimToNull(rawValue);
        if (normalized == null) {
            return null;
        }
        if (normalized.length() > maxLength) {
            throw new IllegalArgumentException(fieldName + " exceeds max length " + maxLength);
        }
        return normalized;
    }

    private String validateOptionalLongText(String rawValue, String fieldName) {
        String normalized = trimToNull(rawValue);
        if (normalized == null) {
            return null;
        }
        if (normalized.length() > MAX_LONG_TEXT) {
            throw new IllegalArgumentException(fieldName + " exceeds max length " + MAX_LONG_TEXT);
        }
        return normalized;
    }

    private String validateOptionalUrl(String rawUrl, String fieldName) {
        String normalized = trimToNull(rawUrl);
        if (normalized == null) {
            return null;
        }
        if (normalized.length() > 1024) {
            throw new IllegalArgumentException(fieldName + " exceeds max length 1024");
        }
        try {
            URI uri = URI.create(normalized);
            String scheme = uri.getScheme();
            if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
                throw new IllegalArgumentException(fieldName + " must use http or https");
            }
            if (uri.getHost() == null || uri.getHost().isBlank()) {
                throw new IllegalArgumentException(fieldName + " must be a valid URL");
            }
            return normalized;
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(fieldName + " must be a valid URL");
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private ProfileSectionVisibility orDefaultVisibility(ProfileSectionVisibility value) {
        return value == null ? ProfileSectionVisibility.PUBLIC : value;
    }

    private ProfileEditorResponse toEditorResponse(UserProfile profile) {
        ProfileEditorResponse response = new ProfileEditorResponse();
        response.setUserId(profile.getUser().getId());
        response.setName(profile.getName());
        response.setEmail(profile.getEmail());
        response.setAge(profile.getAge());
        response.setGender(profile.getGender());
        response.setUniversity(profile.getUniversity());
        response.setMajor(profile.getMajor());
        response.setLocation(profile.getLocation());
        response.setBio(profile.getBio());
        response.setHeadline(profile.getHeadline());
        response.setOpenToCollaborate(profile.isOpenToCollaborate());
        response.setProfilePictureUrl(profile.getProfilePictureUrl());
        response.setInterests(new ArrayList<>(profile.getInterests() == null ? List.of() : profile.getInterests()));
        response.setFeaturedCommunityIds(new ArrayList<>(profile.getFeaturedCommunityIds() == null ? List.of() : profile.getFeaturedCommunityIds()));

        List<ProfileEditorResponse.LanguageItem> languageItems = new ArrayList<>();
        if (profile.getLanguages() != null) {
            profile.getLanguages().forEach(lang -> {
                if (lang == null) {
                    return;
                }
                ProfileEditorResponse.LanguageItem item = new ProfileEditorResponse.LanguageItem();
                item.setName(lang.getName());
                item.setLevel(lang.getLevel());
                languageItems.add(item);
            });
        }
        response.setLanguages(languageItems);

        ProfileEditorResponse.LookingForItem lookingForItem = new ProfileEditorResponse.LookingForItem();
        if (profile.getLookingFor() != null) {
            lookingForItem.setStudyPartner(profile.getLookingFor().isStudyPartner());
            lookingForItem.setLanguageExchange(profile.getLookingFor().isLanguageExchange());
            lookingForItem.setFriendship(profile.getLookingFor().isFriendship());
            lookingForItem.setNetworking(profile.getLookingFor().isNetworking());
            lookingForItem.setCommunity(profile.getLookingFor().isCommunity());
        }
        response.setLookingFor(lookingForItem);

        ProfileEditorResponse.SocialLinksItem socialLinksItem = new ProfileEditorResponse.SocialLinksItem();
        if (profile.getSocialLinks() != null) {
            socialLinksItem.setGithub(profile.getSocialLinks().getGithub());
            socialLinksItem.setLinkedin(profile.getSocialLinks().getLinkedin());
            socialLinksItem.setInstagram(profile.getSocialLinks().getInstagram());
        }
        response.setSocialLinks(socialLinksItem);

        ProfileEditorResponse.VisibilitySettingsItem visibility = new ProfileEditorResponse.VisibilitySettingsItem();
        UserProfile.VisibilitySettings sourceVisibility = profile.getVisibilitySettings();
        if (sourceVisibility != null) {
            visibility.setAbout(orDefaultVisibility(sourceVisibility.getAbout()));
            visibility.setExperience(orDefaultVisibility(sourceVisibility.getExperience()));
            visibility.setProjects(orDefaultVisibility(sourceVisibility.getProjects()));
            visibility.setCollaborations(orDefaultVisibility(sourceVisibility.getCollaborations()));
            visibility.setCommunities(orDefaultVisibility(sourceVisibility.getCommunities()));
            visibility.setActivity(orDefaultVisibility(sourceVisibility.getActivity()));
            visibility.setSocialLinks(orDefaultVisibility(sourceVisibility.getSocialLinks()));
        }
        response.setVisibilitySettings(visibility);

        List<ProfileEditorResponse.ExperienceItem> experiences = new ArrayList<>();
        if (profile.getExperiences() != null) {
            profile.getExperiences().forEach(exp -> {
                if (exp == null) {
                    return;
                }
                ProfileEditorResponse.ExperienceItem item = new ProfileEditorResponse.ExperienceItem();
                item.setId(exp.getId());
                item.setTitle(exp.getTitle());
                item.setOrganization(exp.getOrganization());
                item.setLocation(exp.getLocation());
                item.setStartDate(exp.getStartDate());
                item.setEndDate(exp.getEndDate());
                item.setCurrent(exp.isCurrent());
                item.setDescription(exp.getDescription());
                item.setSortOrder(exp.getSortOrder());
                experiences.add(item);
            });
        }
        response.setExperiences(experiences);

        List<ProfileEditorResponse.ProjectItem> projects = new ArrayList<>();
        if (profile.getProjects() != null) {
            profile.getProjects().forEach(project -> {
                if (project == null) {
                    return;
                }
                ProfileEditorResponse.ProjectItem item = new ProfileEditorResponse.ProjectItem();
                item.setId(project.getId());
                item.setTitle(project.getTitle());
                item.setShortDescription(project.getShortDescription());
                item.setDescription(project.getDescription());
                item.setCoverImageUrl(project.getCoverImageUrl());
                item.setProjectUrl(project.getProjectUrl());
                item.setRepoUrl(project.getRepoUrl());
                item.setTechStack(new ArrayList<>(project.getTechStack() == null ? List.of() : project.getTechStack()));
                item.setStartDate(project.getStartDate());
                item.setEndDate(project.getEndDate());
                item.setSortOrder(project.getSortOrder());
                projects.add(item);
            });
        }
        response.setProjects(projects);

        List<ProfileEditorResponse.CollaborationItem> collaborations = new ArrayList<>();
        if (profile.getCollaborations() != null) {
            profile.getCollaborations().forEach(collaboration -> {
                if (collaboration == null) {
                    return;
                }
                ProfileEditorResponse.CollaborationItem item = new ProfileEditorResponse.CollaborationItem();
                item.setId(collaboration.getId());
                item.setTitle(collaboration.getTitle());
                item.setPartnerName(collaboration.getPartnerName());
                item.setCollaborationType(collaboration.getCollaborationType());
                item.setDescription(collaboration.getDescription());
                item.setStartDate(collaboration.getStartDate());
                item.setEndDate(collaboration.getEndDate());
                item.setResultSummary(collaboration.getResultSummary());
                item.setReferenceUrl(collaboration.getReferenceUrl());
                item.setSortOrder(collaboration.getSortOrder());
                collaborations.add(item);
            });
        }
        response.setCollaborations(collaborations);

        return response;
    }
}