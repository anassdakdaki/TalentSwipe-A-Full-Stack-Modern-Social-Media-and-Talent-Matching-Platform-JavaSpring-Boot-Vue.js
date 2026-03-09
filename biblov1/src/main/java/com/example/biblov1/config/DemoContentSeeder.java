package com.example.biblov1.config;

import com.example.biblov1.model.Community;
import com.example.biblov1.model.User;
import com.example.biblov1.model.UserProfile;
import com.example.biblov1.repository.CommunityRepository;
import com.example.biblov1.repository.PostRepository;
import com.example.biblov1.repository.UserProfileRepository;
import com.example.biblov1.repository.UserRepository;
import com.example.biblov1.service.CommunityService;
import com.example.biblov1.service.BotPopulationService;
import com.example.biblov1.service.PostService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

@Component
@Profile("!test")
public class DemoContentSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final CommunityRepository communityRepository;
    private final PostRepository postRepository;
    private final CommunityService communityService;
    private final PostService postService;
    private final BotPopulationService botPopulationService;
    private final PasswordEncoder passwordEncoder;

    public DemoContentSeeder(UserRepository userRepository,
                             UserProfileRepository userProfileRepository,
                             CommunityRepository communityRepository,
                             PostRepository postRepository,
                             CommunityService communityService,
                             PostService postService,
                             BotPopulationService botPopulationService,
                             PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.communityRepository = communityRepository;
        this.postRepository = postRepository;
        this.communityService = communityService;
        this.postService = postService;
        this.botPopulationService = botPopulationService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        List<User> seededUsers = createSeedUsers();
        List<Community> seededCommunities = createSeedCommunities(seededUsers);

        if (!seededCommunities.isEmpty()) {
            createSeedPosts(seededCommunities, seededUsers);
        }

        botPopulationService.seedBotProfilesAndCommunities();
    }

    private List<User> createSeedUsers() {
        List<User> users = new ArrayList<>();

        users.add(createOrRefreshProfile("Nina Patel", "nina.community@biblo.local", userProfile -> {
            userProfile.setAge(22);
            userProfile.setGender("female");
            userProfile.setUniversity("Northbridge University");
            userProfile.setMajor("Business Analytics");
            userProfile.setLocation("London, UK");
            userProfile.setBio("I connect students with shared goals in startup mentoring, internships, and networking.");
            userProfile.setProfilePictureUrl("/uploads/2a6225f0-1c64-4377-96a6-78c8fe12b831.jpg");
            userProfile.setInterests(new ArrayList<>(Arrays.asList("Business", "Startups", "Data", "Mentoring", "Career Planning")));
            userProfile.setLanguages(new ArrayList<>(Arrays.asList(language("English", "native"), language("Spanish", "intermediate"))));
            userProfile.setLookingFor(lookingForProfile(true, true, true, false, true));
            userProfile.setSocialLinks(socialLinks("https://github.com/ninapatel", "https://www.linkedin.com/in/ninapatel", null));
        }));

        users.add(createOrRefreshProfile("Leo Kim", "leo.community@biblo.local", userProfile -> {
            userProfile.setAge(24);
            userProfile.setGender("male");
            userProfile.setUniversity("Westlake College");
            userProfile.setMajor("Computer Science");
            userProfile.setLocation("San Francisco, USA");
            userProfile.setBio("Building open-source study circles for AI, cloud, and product internships.");
            userProfile.setProfilePictureUrl("/uploads/a434d0fc-cc16-49f1-8d64-e52ff5bb3d06.jpg");
            userProfile.setInterests(new ArrayList<>(Arrays.asList("AI", "Machine Learning", "Hackathons", "Tech Events", "Programming")));
            userProfile.setLanguages(new ArrayList<>(Arrays.asList(language("English", "native"), language("Korean", "fluent"))));
            userProfile.setLookingFor(lookingForProfile(true, false, false, true, true));
            userProfile.setSocialLinks(socialLinks("https://github.com/leokim", "https://www.linkedin.com/in/leo-kim", "https://instagram.com/leokim"));
        }));

        users.add(createOrRefreshProfile("Sofia Rivera", "sofia.community@biblo.local", userProfile -> {
            userProfile.setAge(23);
            userProfile.setGender("female");
            userProfile.setUniversity("Harbor Tech");
            userProfile.setMajor("Product Design");
            userProfile.setLocation("SÃ£o Paulo, Brazil");
            userProfile.setBio("I run community challenges around design thinking, study groups, and campus events.");
            userProfile.setProfilePictureUrl("/uploads/6dd6bc8b-39a7-4f45-be63-77c82c2c43bf_image_fx (2).jpg");
            userProfile.setInterests(new ArrayList<>(Arrays.asList("Product Design", "UX", "Branding", "Community Building")));
            userProfile.setLanguages(new ArrayList<>(Arrays.asList(language("Portuguese", "native"), language("English", "advanced"))));
            userProfile.setLookingFor(lookingForProfile(false, true, true, true, false));
            userProfile.setSocialLinks(socialLinks(null, "https://www.linkedin.com/in/sofiarivera", "https://instagram.com/sofiarivera"));
        }));

        return users;
    }

    private User createOrRefreshProfile(String name, String email, Consumer<UserProfile> configure) {
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setName(name);
            newUser.setEmail(email);
            newUser.setPassword(passwordEncoder.encode("Welcome123!"));
            return userRepository.save(newUser);
        });

        if (!name.equals(user.getName())) {
            user.setName(name);
            userRepository.save(user);
        }

        UserProfile profile = userProfileRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    UserProfile newProfile = new UserProfile();
                    newProfile.setUser(user);
                    return newProfile;
                });

        profile.setName(name);
        profile.setEmail(email);
        profile.setAge(0);
        profile.setGender("");
        profile.setUniversity("");
        profile.setMajor("");
        profile.setLocation("");
        profile.setBio("");
        profile.setInterests(new ArrayList<>());
        profile.setLanguages(new ArrayList<>());
        profile.setLookingFor(new UserProfile.LookingFor());
        profile.setSocialLinks(new UserProfile.SocialLinks());
        configure.accept(profile);
        userProfileRepository.save(profile);

        return userRepository.findByEmail(email).orElseThrow();
    }

    private List<Community> createSeedCommunities(List<User> users) {
        List<Community> communities = new ArrayList<>();

        communities.add(createCommunityIfMissing(
                "Business Builders Circle",
                "Cross-campus startup ideas, mock interviews, and practical business projects for students.",
                users.get(0),
                Arrays.asList("Business", "Networking", "Career"),
                "https://images.pexels.com/photos/1181671/pexels-photo-1181671.jpeg?auto=compress&cs=tinysrgb&w=1600&h=900&fit=crop"
        ));

        communities.add(createCommunityIfMissing(
                "AI Research Collaborative",
                "Hands-on AI and data discussion groups with practical experiments and study sessions.",
                users.get(1),
                Arrays.asList("AI", "Machine Learning", "Data Science"),
                "https://images.pexels.com/photos/3756678/pexels-photo-3756678.jpeg?auto=compress&cs=tinysrgb&w=1600&h=900&fit=crop"
        ));

        communities.add(createCommunityIfMissing(
                "Campus Impact Hub",
                "Events, workshops, and weekly check-ins for students who love learning in teams.",
                users.get(2),
                Arrays.asList("Community", "Events", "Mentorship"),
                "https://images.pexels.com/photos/1181248/pexels-photo-1181248.jpeg?auto=compress&cs=tinysrgb&w=1600&h=900&fit=crop"
        ));

        communities.add(createCommunityIfMissing(
                "Global Impact Community",
                "Cross-border collaboration, social innovation, and practical projects for local and global impact.",
                users.get(0),
                Arrays.asList("Impact", "Global", "Collaboration"),
                "https://images.pexels.com/photos/3769138/pexels-photo-3769138.jpeg?auto=compress&cs=tinysrgb&w=1600&h=900&fit=crop"
        ));

        return communities;
    }

    private Community createCommunityIfMissing(String name, String description, User owner, List<String> tags, String imageUrl) {
        Optional<Community> existingCommunity = communityRepository.findByName(name);
        if (existingCommunity.isPresent()) {
            Community community = existingCommunity.get();
            boolean changed = false;

            if (!Objects.equals(description, community.getDescription())) {
                community.setDescription(description);
                changed = true;
            }

            Set<String> normalizedTags = tags == null
                    ? new LinkedHashSet<>()
                    : tags.stream()
                    .filter(tag -> tag != null && !tag.trim().isEmpty())
                    .map(String::trim)
                    .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
            if (!Objects.equals(normalizedTags, community.getTags())) {
                community.setTags(normalizedTags);
                changed = true;
            }

            if (!Objects.equals(imageUrl, community.getImageUrl())) {
                community.setImageUrl(imageUrl);
                changed = true;
            }

            if (community.getOwner() == null || !Objects.equals(community.getOwner().getId(), owner.getId())) {
                community.setOwner(owner);
                changed = true;
            }

            if (changed) {
                community = communityRepository.save(community);
            }
            return community;
        }
        return communityService.createCommunity(name, description, owner.getId(), tags, imageUrl);
    }

    private void createSeedPosts(List<Community> communities, List<User> users) {
        createSeedPostIfEmpty(communities.get(0).getId(), users.get(0).getId(), "Welcome to Business Builders Circle!", Set.of("startup", "career", "networking"));
        createSeedPostIfEmpty(communities.get(1).getId(), users.get(1).getId(), "Share your first experiment and get feedback this week.", Set.of("ai", "learning", "projects"));
        createSeedPostIfEmpty(communities.get(2).getId(), users.get(2).getId(), "Host your first peer-learning session and send the agenda here.", Set.of("community", "campus", "study"));
        createSeedPostIfEmpty(communities.get(3).getId(), users.get(0).getId(), "Introduce one local challenge you care about and one practical way others can support.", Set.of("impact", "global", "collaboration"));
    }

    private void createSeedPostIfEmpty(Long communityId, Long userId, String content, Set<String> hashtags) {
        Community community = communityRepository.findById(communityId).orElseThrow();
        if (postRepositoryCount(community) == 0) {
            postService.createPost(communityId, userId, content, null, hashtags);
        }
    }

    private long postRepositoryCount(Community community) {
        return postRepository.countByCommunity(community);
    }

    private UserProfile.LanguageProficiency language(String name, String level) {
        UserProfile.LanguageProficiency languageProficiency = new UserProfile.LanguageProficiency();
        languageProficiency.setName(name);
        languageProficiency.setLevel(level);
        return languageProficiency;
    }

    private UserProfile.LookingFor lookingForProfile(boolean studyPartner, boolean languageExchange, boolean friendship, boolean networking, boolean community) {
        UserProfile.LookingFor lookingFor = new UserProfile.LookingFor();
        lookingFor.setStudyPartner(studyPartner);
        lookingFor.setLanguageExchange(languageExchange);
        lookingFor.setFriendship(friendship);
        lookingFor.setNetworking(networking);
        lookingFor.setCommunity(community);
        return lookingFor;
    }

    private UserProfile.SocialLinks socialLinks(String github, String linkedin, String instagram) {
        UserProfile.SocialLinks socialLinks = new UserProfile.SocialLinks();
        socialLinks.setGithub(github);
        socialLinks.setLinkedin(linkedin);
        socialLinks.setInstagram(instagram);
        return socialLinks;
    }
}
