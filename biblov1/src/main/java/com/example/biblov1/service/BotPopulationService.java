
package com.example.biblov1.service;

import com.example.biblov1.model.Comment;
import com.example.biblov1.model.Community;
import com.example.biblov1.model.Like;
import com.example.biblov1.model.Post;
import com.example.biblov1.model.User;
import com.example.biblov1.model.UserProfile;
import com.example.biblov1.repository.CommentRepository;
import com.example.biblov1.repository.CommunityRepository;
import com.example.biblov1.repository.LikeRepository;
import com.example.biblov1.repository.PostRepository;
import com.example.biblov1.repository.UserProfileRepository;
import com.example.biblov1.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BotPopulationService {
    private static final Logger logger = LoggerFactory.getLogger(BotPopulationService.class);
    private static final List<String> CROSS_COMMUNITY_IMAGE_POOL = List.of(
            "https://images.pexels.com/photos/3184611/pexels-photo-3184611.jpeg?auto=compress&cs=tinysrgb&w=1400&h=1400&fit=crop",
            "https://images.pexels.com/photos/3184360/pexels-photo-3184360.jpeg?auto=compress&cs=tinysrgb&w=1400&h=1400&fit=crop",
            "https://images.pexels.com/photos/3184423/pexels-photo-3184423.jpeg?auto=compress&cs=tinysrgb&w=1400&h=1400&fit=crop",
            "https://images.pexels.com/photos/3184328/pexels-photo-3184328.jpeg?auto=compress&cs=tinysrgb&w=1400&h=1400&fit=crop",
            "https://images.pexels.com/photos/3184356/pexels-photo-3184356.jpeg?auto=compress&cs=tinysrgb&w=1400&h=1400&fit=crop",
            "https://images.pexels.com/photos/3184292/pexels-photo-3184292.jpeg?auto=compress&cs=tinysrgb&w=1400&h=1400&fit=crop",
            "https://images.pexels.com/photos/3184418/pexels-photo-3184418.jpeg?auto=compress&cs=tinysrgb&w=1400&h=1400&fit=crop",
            "https://images.pexels.com/photos/3182773/pexels-photo-3182773.jpeg?auto=compress&cs=tinysrgb&w=1400&h=1400&fit=crop",
            "https://images.pexels.com/photos/3182791/pexels-photo-3182791.jpeg?auto=compress&cs=tinysrgb&w=1400&h=1400&fit=crop",
            "https://images.pexels.com/photos/3184291/pexels-photo-3184291.jpeg?auto=compress&cs=tinysrgb&w=1400&h=1400&fit=crop",
            "https://images.pexels.com/photos/3183132/pexels-photo-3183132.jpeg?auto=compress&cs=tinysrgb&w=1400&h=1400&fit=crop",
            "https://images.pexels.com/photos/3183152/pexels-photo-3183152.jpeg?auto=compress&cs=tinysrgb&w=1400&h=1400&fit=crop",
            "https://images.pexels.com/photos/3183201/pexels-photo-3183201.jpeg?auto=compress&cs=tinysrgb&w=1400&h=1400&fit=crop",
            "https://images.pexels.com/photos/3183171/pexels-photo-3183171.jpeg?auto=compress&cs=tinysrgb&w=1400&h=1400&fit=crop",
            "https://images.pexels.com/photos/3184650/pexels-photo-3184650.jpeg?auto=compress&cs=tinysrgb&w=1400&h=1400&fit=crop",
            "https://images.pexels.com/photos/3184405/pexels-photo-3184405.jpeg?auto=compress&cs=tinysrgb&w=1400&h=1400&fit=crop",
            "https://images.pexels.com/photos/3184407/pexels-photo-3184407.jpeg?auto=compress&cs=tinysrgb&w=1400&h=1400&fit=crop",
            "https://images.pexels.com/photos/3182827/pexels-photo-3182827.jpeg?auto=compress&cs=tinysrgb&w=1400&h=1400&fit=crop",
            "https://images.pexels.com/photos/3182746/pexels-photo-3182746.jpeg?auto=compress&cs=tinysrgb&w=1400&h=1400&fit=crop",
            "https://images.pexels.com/photos/3184325/pexels-photo-3184325.jpeg?auto=compress&cs=tinysrgb&w=1400&h=1400&fit=crop",
            "https://images.pexels.com/photos/3183178/pexels-photo-3183178.jpeg?auto=compress&cs=tinysrgb&w=1400&h=1400&fit=crop",
            "https://images.pexels.com/photos/3182781/pexels-photo-3182781.jpeg?auto=compress&cs=tinysrgb&w=1400&h=1400&fit=crop",
            "https://images.pexels.com/photos/3182768/pexels-photo-3182768.jpeg?auto=compress&cs=tinysrgb&w=1400&h=1400&fit=crop",
            "https://images.pexels.com/photos/3183176/pexels-photo-3183176.jpeg?auto=compress&cs=tinysrgb&w=1400&h=1400&fit=crop",
            "https://images.pexels.com/photos/3183192/pexels-photo-3183192.jpeg?auto=compress&cs=tinysrgb&w=1400&h=1400&fit=crop",
            "https://images.pexels.com/photos/3183191/pexels-photo-3183191.jpeg?auto=compress&cs=tinysrgb&w=1400&h=1400&fit=crop",
            "https://images.pexels.com/photos/3183157/pexels-photo-3183157.jpeg?auto=compress&cs=tinysrgb&w=1400&h=1400&fit=crop",
            "https://images.pexels.com/photos/3183138/pexels-photo-3183138.jpeg?auto=compress&cs=tinysrgb&w=1400&h=1400&fit=crop",
            "https://images.pexels.com/photos/3182832/pexels-photo-3182832.jpeg?auto=compress&cs=tinysrgb&w=1400&h=1400&fit=crop"
    );

    private static final List<BotProfileDefinition> BOT_DEFINITIONS = List.of(
            new BotProfileDefinition(
                    "Aiden Park",
                    "aiden.park@biblo.bots",
                    "Welcome123!",
                    25,
                    "male",
                    "University of Melbourne",
                    "AI Systems Engineering",
                    "Melbourne, Australia",
                    "I coordinate weekly study circles for people building AI products, machine-learning projects, and startup experiments.",
                    "https://images.pexels.com/photos/1181359/pexels-photo-1181359.jpeg?auto=compress&cs=tinysrgb&w=600&h=600&fit=crop",
                    List.of("Artificial Intelligence", "Machine Learning", "Product Building", "Startups", "Research"),
                    List.of(new Language("English", "native"), new Language("Korean", "intermediate")),
                    new LookingFor(true, true, true, true, true),
                    new SocialLinks("https://github.com", "https://www.linkedin.com", "https://instagram.com"),
                    "Hey there! I saw we matched, glad you are here. Happy to connect on projects or study plans.",
                    new CommunityDefinition(
                            "AI Product Lab",
                            "Hands-on AI collaboration sessions, model-sharing, and startup idea prototyping.",
                            List.of("AI", "Machine Learning", "Research", "Startups"),
                            "https://images.pexels.com/photos/1108099/pexels-photo-1108099.jpeg?auto=compress&cs=tinysrgb&w=1600&h=900&fit=crop",
                            List.of(
                                    new BotPost(
                                            "Welcome to AI Product Lab. Share your current ML project and feedback goals.",
                                            Set.of("ai", "projects", "collaboration"),
                                            "https://images.pexels.com/photos/695644/pexels-photo-695644.jpeg?auto=compress&cs=tinysrgb&w=1400&h=1400&fit=crop"
                                    ),
                                    new BotPost(
                                            "I am hosting a weekly prompt challenge this Friday. Post one idea and I will share a quick review.",
                                            Set.of("machinelearning", "community", "challenges"),
                                            "https://images.pexels.com/photos/1181690/pexels-photo-1181690.jpeg?auto=compress&cs=tinysrgb&w=1400&h=1400&fit=crop"
                                    ),
                                    new BotPost(
                                            "Need help validating an AI app idea? I am open to co-building study-to-launch roadmaps.",
                                            Set.of("startup", "mentorship", "networking"),
                                            "https://images.pexels.com/photos/3861958/pexels-photo-3861958.jpeg?auto=compress&cs=tinysrgb&w=1400&h=1400&fit=crop"
                                    )
                            )
                    )
            ),
            new BotProfileDefinition(
                    "Sofia Marin",
                    "sofia.marin@biblo.bots",
                    "Welcome123!",
                    24,
                    "female",
                    "King's University",
                    "Product Design",
                    "Lisbon, Portugal",
                    "I run practical UX community sprints where students exchange portfolio feedback and career strategies.",
                    "https://images.pexels.com/photos/1181244/pexels-photo-1181244.jpeg?auto=compress&cs=tinysrgb&w=600&h=600&fit=crop",
                    List.of("UX Design", "Portfolio", "Career Growth", "Case Studies", "Community"),
                    List.of(new Language("Portuguese", "native"), new Language("English", "advanced")),
                    new LookingFor(true, false, true, false, true),
                    new SocialLinks("https://github.com", "https://www.linkedin.com", "https://instagram.com"),
                    "Hi! Great to connect. Share your latest portfolio or case study and I will help you refine it.",
                    new CommunityDefinition(
                            "Design & Career Studio",
                            "Mock case studies, portfolio critiques, and practical skill-sharing for design-minded students.",
                            List.of("Design", "UX", "Portfolio", "Career"),
                            "https://images.pexels.com/photos/3183197/pexels-photo-3183197.jpeg?auto=compress&cs=tinysrgb&w=1600&h=900&fit=crop",
                            List.of(
                                    new BotPost(
                                            "This week: upload one portfolio piece you are most proud of and ask for 3 actionable critiques.",
                                            Set.of("portfolio", "feedback", "ux"),
                                            "https://images.pexels.com/photos/3183198/pexels-photo-3183198.jpeg?auto=compress&cs=tinysrgb&w=1400&h=1400&fit=crop"
                                    ),
                                    new BotPost(
                                            "Design challenge of the week: explain your process in 5 steps and show one measurable outcome.",
                                            Set.of("process", "design", "learning"),
                                            "https://images.pexels.com/photos/3769021/pexels-photo-3769021.jpeg?auto=compress&cs=tinysrgb&w=1400&h=1400&fit=crop"
                                    ),
                                    new BotPost(
                                            "Need internship leads in product? I am sharing resources and networking tactics from recent teams I support.",
                                            Set.of("internship", "networking", "career"),
                                            "https://images.pexels.com/photos/3184339/pexels-photo-3184339.jpeg?auto=compress&cs=tinysrgb&w=1400&h=1400&fit=crop"
                                    )
                            )
                    )
            ),
            new BotProfileDefinition(
                    "Jordan Khan",
                    "jordan.khan@biblo.bots",
                    "Welcome123!",
                    26,
                    "non-binary",
                    "Carnegie Tech",
                    "Computer Science",
                    "Toronto, Canada",
                    "I like pairing up on collaborative coding, hackathons, and interview prep grounded in practical software craft.",
                    "https://images.pexels.com/photos/1181519/pexels-photo-1181519.jpeg?auto=compress&cs=tinysrgb&w=600&h=600&fit=crop",
                    List.of("LeetCode", "System Design", "Hackathons", "Cloud", "Coding"),
                    List.of(new Language("English", "native"), new Language("Spanish", "intermediate")),
                    new LookingFor(true, true, false, true, false),
                    new SocialLinks("https://github.com", "https://www.linkedin.com", null),
                    "Nice to match! If you are up for it, I can pair with you on interview prep or a small coding sprint.",
                    new CommunityDefinition(
                            "Code & Career Sprint",
                            "Practice interviews, coding drills, and peer-led mini projects for career-ready engineering progress.",
                            List.of("Coding", "Interview", "Hackathon", "Cloud"),
                            "https://images.pexels.com/photos/3861969/pexels-photo-3861969.jpeg?auto=compress&cs=tinysrgb&w=1600&h=900&fit=crop",
                            List.of(
                                    new BotPost(
                                            "Drop a problem you solved this week and a challenge you still want help with.",
                                            Set.of("leetcode", "coding", "learning"),
                                            "https://images.pexels.com/photos/3184639/pexels-photo-3184639.jpeg?auto=compress&cs=tinysrgb&w=1400&h=1400&fit=crop"
                                    ),
                                    new BotPost(
                                            "Weekend mini-sprint: build a tiny API feature and post your architecture plan first.",
                                            Set.of("api", "backend", "practice"),
                                            "https://images.pexels.com/photos/3183150/pexels-photo-3183150.jpeg?auto=compress&cs=tinysrgb&w=1400&h=1400&fit=crop"
                                    ),
                                    new BotPost(
                                            "Interview grind check-in: share your top 3 weak topics and lets patch one together.",
                                            Set.of("interviews", "career", "engineering"),
                                            "https://images.pexels.com/photos/3861964/pexels-photo-3861964.jpeg?auto=compress&cs=tinysrgb&w=1400&h=1400&fit=crop"
                                    )
                            )
                    )
            ),
            new BotProfileDefinition(
                    "Emma Laurent",
                    "emma.laurent@biblo.bots",
                    "Welcome123!",
                    23,
                    "female",
                    "Sorbonne Paris",
                    "Business Analytics",
                    "Paris, France",
                    "I help students turn campus ideas into internship-ready projects and realistic networking habits.",
                    "https://images.pexels.com/photos/1181263/pexels-photo-1181263.jpeg?auto=compress&cs=tinysrgb&w=600&h=600&fit=crop",
                    List.of("Business Analytics", "Data", "Networking", "Entrepreneurship", "Study Plans"),
                    List.of(new Language("French", "native"), new Language("English", "fluent")),
                    new LookingFor(false, true, true, true, true),
                    new SocialLinks("https://github.com", "https://www.linkedin.com", "https://instagram.com"),
                    "Hi, great to meet you. I can help structure your study or career plan into practical weekly milestones.",
                    new CommunityDefinition(
                            "Campus Strategy Network",
                            "Realistic project roadmaps, business case practice, and networking for practical student growth.",
                            List.of("Business", "Networking", "Projects", "Mentorship"),
                            "https://images.pexels.com/photos/3184465/pexels-photo-3184465.jpeg?auto=compress&cs=tinysrgb&w=1600&h=900&fit=crop",
                            List.of(
                                    new BotPost(
                                            "This Friday: map one business problem you can validate on campus in 72 hours.",
                                            Set.of("business", "validation", "projects"),
                                            "https://images.pexels.com/photos/3153198/pexels-photo-3153198.jpeg?auto=compress&cs=tinysrgb&w=1400&h=1400&fit=crop"
                                    ),
                                    new BotPost(
                                            "How are people building better internship workflows? Share one process and I will comment with improvements.",
                                            Set.of("internship", "workflow", "mentorship"),
                                            "https://images.pexels.com/photos/1181316/pexels-photo-1181316.jpeg?auto=compress&cs=tinysrgb&w=1400&h=1400&fit=crop"
                                    ),
                                    new BotPost(
                                            "I am collecting great student-led project ideas for recruiter-friendly storytelling practice.",
                                            Set.of("portfolio", "career", "presentations"),
                                            "https://images.pexels.com/photos/1181345/pexels-photo-1181345.jpeg?auto=compress&cs=tinysrgb&w=1400&h=1400&fit=crop"
                                    )
                            )
                    )
            ),
            new BotProfileDefinition(
                    "Diego Ortega",
                    "diego.ortega@biblo.bots",
                    "Welcome123!",
                    27,
                    "male",
                    "University of Sao Paulo",
                    "Finance and Management",
                    "Sao Paulo, Brazil",
                    "I run discussion circles on fintech, quant learning, and cross-campus study support for career transitions.",
                    "https://images.pexels.com/photos/1181406/pexels-photo-1181406.jpeg?auto=compress&cs=tinysrgb&w=600&h=600&fit=crop",
                    List.of("Finance", "Data Literacy", "Studying", "Career Transition", "Fintech"),
                    List.of(new Language("Portuguese", "native"), new Language("English", "advanced")),
                    new LookingFor(true, true, true, true, true),
                    new SocialLinks("https://github.com", "https://www.linkedin.com", "https://instagram.com"),
                    "Ola! Great to connect. We can align on your goals for fintech or finance learning and practical prep.",
                    new CommunityDefinition(
                            "Fintech Study Circle",
                            "Quant, finance, and fintech discussion groups with practical study planning and career support.",
                            List.of("Finance", "Fintech", "Quant", "Career"),
                            "https://images.pexels.com/photos/3182812/pexels-photo-3182812.jpeg?auto=compress&cs=tinysrgb&w=1600&h=900&fit=crop",
                            List.of(
                                    new BotPost(
                                            "Share your current quant or finance focus and I will suggest a 2-week study schedule.",
                                            Set.of("finance", "quant", "study"),
                                            "https://images.pexels.com/photos/3184306/pexels-photo-3184306.jpeg?auto=compress&cs=tinysrgb&w=1400&h=1400&fit=crop"
                                    ),
                                    new BotPost(
                                            "Career transition tip: translate one technical skill into a recruiter-ready story. Post yours below.",
                                            Set.of("career", "storytelling", "networking"),
                                            "https://images.pexels.com/photos/3183148/pexels-photo-3183148.jpeg?auto=compress&cs=tinysrgb&w=1400&h=1400&fit=crop"
                                    ),
                                    new BotPost(
                                            "I am posting a weekly challenge set for practical financial modeling fundamentals.",
                                            Set.of("finance", "modeling", "practical"),
                                            "https://images.pexels.com/photos/3182822/pexels-photo-3182822.jpeg?auto=compress&cs=tinysrgb&w=1400&h=1400&fit=crop"
                                    )
                            )
                    )
            ),
            new BotProfileDefinition(
                    "Lina Chen",
                    "lina.chen@biblo.bots",
                    "Welcome123!",
                    22,
                    "female",
                    "ESADE Business School",
                    "Tourism and International Business",
                    "Barcelona, Spain",
                    "I build practical travel planning circles for students who love backpacking, photography, and cultural exchange.",
                    "https://images.pexels.com/photos/1181298/pexels-photo-1181298.jpeg?auto=compress&cs=tinysrgb&w=600&h=600&fit=crop",
                    List.of("Travel Planning", "Backpacking", "Cultural Exchange", "Photography", "Community"),
                    List.of(new Language("English", "native"), new Language("Mandarin", "advanced"), new Language("Spanish", "advanced")),
                    new LookingFor(true, true, true, true, true),
                    new SocialLinks("https://github.com", "https://www.linkedin.com", "https://instagram.com"),
                    "Great to match. Lets swap travel plans, route ideas, and practical packing advice for student trips.",
                    new CommunityDefinition(
                            "Travel Lovers",
                            "Student travel stories, itinerary planning, and culture-first meetups for explorers on a budget.",
                            List.of("Travel", "Backpacking", "Culture", "Photography"),
                            "https://images.pexels.com/photos/572056/pexels-photo-572056.jpeg?auto=compress&cs=tinysrgb&w=1600&h=900&fit=crop",
                            List.of(
                                    new BotPost(
                                            "Share one destination on your student bucket list and what makes it special for you.",
                                            Set.of("travel", "students", "stories"),
                                            "https://images.pexels.com/photos/3182763/pexels-photo-3182763.jpeg?auto=compress&cs=tinysrgb&w=1400&h=1400&fit=crop"
                                    ),
                                    new BotPost(
                                            "Weekend challenge: post a 3-day budget itinerary with transport, food, and one hidden gem.",
                                            Set.of("itinerary", "budget", "planning"),
                                            "https://images.pexels.com/photos/380769/pexels-photo-380769.jpeg?auto=compress&cs=tinysrgb&w=1400&h=1400&fit=crop"
                                    ),
                                    new BotPost(
                                            "Photo thread: drop your favorite travel shot and add one tip for first-time visitors.",
                                            Set.of("photography", "tips", "community"),
                                            "https://images.pexels.com/photos/2102416/pexels-photo-2102416.jpeg?auto=compress&cs=tinysrgb&w=1400&h=1400&fit=crop"
                                    )
                            )
                    )
            )
    );

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final CommunityRepository communityRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;
    private final CommunityService communityService;
    private final PostService postService;
    private final MatchService matchService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.bots.enabled:true}")
    private boolean botsEnabled;

    @Value("${app.bots.count:6}")
    private int botCount;

    @Value("${app.bots.autoMatchOnRegister:false}")
    private boolean autoMatchOnRegister;

    @Value("${app.bots.cross-community.enabled:true}")
    private boolean crossCommunityEnabled;

    @Value("${app.bots.cross-community.min-posts:2}")
    private int crossCommunityMinPosts;

    @Value("${app.bots.cross-community.max-posts:3}")
    private int crossCommunityMaxPosts;

    @Value("${app.bots.cross-community.include-human-communities:true}")
    private boolean includeHumanCommunities;

    public BotPopulationService(UserRepository userRepository,
                                UserProfileRepository userProfileRepository,
                                CommunityRepository communityRepository,
                                PostRepository postRepository,
                                CommentRepository commentRepository,
                                LikeRepository likeRepository,
                                CommunityService communityService,
                                PostService postService,
                                MatchService matchService,
                                PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.communityRepository = communityRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.likeRepository = likeRepository;
        this.communityService = communityService;
        this.postService = postService;
        this.matchService = matchService;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean isEnabled() {
        return botsEnabled;
    }

    @Transactional
    public void seedBotProfilesAndCommunities() {
        if (!botsEnabled) {
            logger.info("Bot seeding is disabled (app.bots.enabled=false).");
            return;
        }

        List<BotProfileDefinition> configuredBots = getActiveBotDefinitions();
        if (configuredBots.isEmpty()) {
            logger.info("No bot definitions selected (app.bots.count={}).", botCount);
            return;
        }

        for (BotProfileDefinition botDefinition : configuredBots) {
            try {
                User botUser = createOrUpdateBotUser(botDefinition);
                createOrUpdateBotProfile(botUser, botDefinition);
                Community botCommunity = createOrUpdateBotCommunity(botUser, botDefinition.community());
                seedBotCommunityPosts(botCommunity, botUser, botDefinition.community());
            } catch (Exception e) {
                logger.warn("Failed to seed bot user/community for {}", botDefinition.name(), e);
            }
        }

        ensureBotCliqueMatches(configuredBots);
        seedCrossCommunityMembershipAndPosts(configuredBots);
        seedCrossBotEngagement(configuredBots);

        if (autoMatchOnRegister) {
            matchBotsWithExistingUsers(false);
        } else {
            logger.info("Bot auto-match on register/startup is disabled (app.bots.autoMatchOnRegister=false).");
        }
    }

    @Transactional
    public void ensureBotsMatchedWithUser(Long userId) {
        if (!botsEnabled || !autoMatchOnRegister) {
            return;
        }
        if (userId == null) {
            return;
        }
        matchBotsWithUser(userId, false);
    }

    @Transactional(readOnly = true)
    public List<Long> getBotUserIds() {
        return getActiveBotDefinitions().stream()
                .map(bot -> userRepository.findByEmail(bot.email()).orElse(null))
                .filter(Objects::nonNull)
                .map(User::getId)
                .distinct()
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BotDefinitionSnapshot> getActiveBotDefinitionsSnapshot() {
        return getActiveBotDefinitions().stream()
                .map(definition -> new BotDefinitionSnapshot(
                        definition.name(),
                        definition.email(),
                        definition.welcomeMessage(),
                        definition.interests()
                ))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BotDefinitionSnapshot getActiveBotDefinitionByEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return getActiveBotDefinitionsSnapshot().stream()
                .filter(definition -> definition.email().equalsIgnoreCase(email))
                .findFirst()
                .orElse(null);
    }

    private void matchBotsWithExistingUsers(boolean force) {
        Set<Long> botUserIds = new HashSet<>(getBotUserIds());
        for (User user : userRepository.findAll()) {
            if (!botUserIds.contains(user.getId())) {
                matchBotsWithUser(user.getId(), force);
            }
        }
    }

    private void matchBotsWithUser(Long humanUserId, boolean force) {
        if (!force && !autoMatchOnRegister) {
            return;
        }

        User humanUser = userRepository.findById(humanUserId)
                .orElseThrow(() -> new RuntimeException("User not found for bot matching: " + humanUserId));
        if (isBotEmail(humanUser.getEmail())) {
            return;
        }

        for (BotProfileDefinition botDefinition : getActiveBotDefinitions()) {
            userRepository.findByEmail(botDefinition.email()).ifPresentOrElse(bot -> {
                try {
                    matchService.ensureMatchedPair(bot.getId(), humanUser.getId(), botDefinition.welcomeMessage(), false);
                } catch (Exception e) {
                    logger.warn("Failed to ensure bot match with user {} for bot {}: {}", humanUser.getId(), botDefinition.name(), e.getMessage());
                }
            }, () -> logger.warn("Bot user not available for matching: {}", botDefinition.email()));
        }
    }

    private List<BotProfileDefinition> getActiveBotDefinitions() {
        if (botCount <= 0) {
            return List.of();
        }
        int maxCount = Math.min(botCount, BOT_DEFINITIONS.size());
        return BOT_DEFINITIONS.subList(0, maxCount);
    }

    private boolean isBotEmail(String email) {
        return getActiveBotDefinitions().stream().anyMatch(def -> def.email().equalsIgnoreCase(email));
    }

    private void ensureBotCliqueMatches(List<BotProfileDefinition> configuredBots) {
        List<User> activeBots = configuredBots.stream()
                .map(definition -> userRepository.findByEmail(definition.email()).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (activeBots.size() < 2) {
            return;
        }

        for (int i = 0; i < activeBots.size(); i++) {
            for (int j = i + 1; j < activeBots.size(); j++) {
                User botA = activeBots.get(i);
                User botB = activeBots.get(j);
                try {
                    matchService.processSwipe(botA.getId(), botB.getId(), com.example.biblov1.model.UserSwipe.SwipeType.LIKE);
                    matchService.processSwipe(botB.getId(), botA.getId(), com.example.biblov1.model.UserSwipe.SwipeType.LIKE);
                } catch (Exception ex) {
                    logger.warn("Failed to ensure bot clique match between {} and {}: {}", botA.getEmail(), botB.getEmail(), ex.getMessage());
                }
            }
        }
    }

    private User createOrUpdateBotUser(BotProfileDefinition definition) {
        User botUser = userRepository.findByEmail(definition.email())
                .orElseGet(() -> {
                    User newBot = new User();
                    newBot.setName(definition.name());
                    newBot.setEmail(definition.email());
                    newBot.setPassword(passwordEncoder.encode(definition.rawPassword()));
                    return userRepository.save(newBot);
                });

        if (!definition.name().equals(botUser.getName())) {
            botUser.setName(definition.name());
            userRepository.save(botUser);
        }

        return userRepository.findByEmail(definition.email())
                .orElseThrow(() -> new RuntimeException("Bot user missing after seed: " + definition.email()));
    }

    private void createOrUpdateBotProfile(User botUser, BotProfileDefinition definition) {
        UserProfile profile = userProfileRepository.findByUserId(botUser.getId())
                .orElseGet(() -> {
                    UserProfile newProfile = new UserProfile();
                    newProfile.setUser(botUser);
                    return newProfile;
                });

        profile.setName(definition.name());
        profile.setEmail(definition.email());
        profile.setAge(definition.age());
        profile.setGender(definition.gender());
        profile.setUniversity(definition.university());
        profile.setMajor(definition.major());
        profile.setLocation(definition.location());
        profile.setBio(definition.bio());
        profile.setProfilePictureUrl(definition.profilePictureUrl());
        profile.setInterests(new java.util.ArrayList<>(definition.interests()));
        profile.setLanguages(definition.languages().stream()
                .map(botLanguage -> {
                    UserProfile.LanguageProficiency languageProficiency = new UserProfile.LanguageProficiency();
                    languageProficiency.setName(botLanguage.name());
                    languageProficiency.setLevel(botLanguage.level());
                    return languageProficiency;
                })
                .collect(Collectors.toCollection(java.util.ArrayList::new)));
        profile.setLookingFor(new UserProfile.LookingFor());
        profile.getLookingFor().setStudyPartner(definition.lookingFor().studyPartner());
        profile.getLookingFor().setLanguageExchange(definition.lookingFor().languageExchange());
        profile.getLookingFor().setFriendship(definition.lookingFor().friendship());
        profile.getLookingFor().setNetworking(definition.lookingFor().networking());
        profile.getLookingFor().setCommunity(definition.lookingFor().community());
        profile.setSocialLinks(new UserProfile.SocialLinks());
        profile.getSocialLinks().setGithub(definition.socialLinks().github());
        profile.getSocialLinks().setLinkedin(definition.socialLinks().linkedin());
        profile.getSocialLinks().setInstagram(definition.socialLinks().instagram());

        userProfileRepository.save(profile);
    }

    private Community createOrUpdateBotCommunity(User botUser, CommunityDefinition definition) {
        Community community = communityRepository.findByName(definition.name())
                .orElseGet(() -> communityService.createCommunity(
                        definition.name(),
                        definition.description(),
                        botUser.getId(),
                        definition.tags(),
                        definition.imageUrl()
                ));

        boolean changed = false;
        if (!definition.description().equals(community.getDescription())) {
            community.setDescription(definition.description());
            changed = true;
        }
        if (!definition.imageUrl().equals(community.getImageUrl())) {
            community.setImageUrl(definition.imageUrl());
            changed = true;
        }
        Set<String> desiredTags = new HashSet<>(definition.tags());
        if (!desiredTags.equals(community.getTags())) {
            community.setTags(desiredTags);
            changed = true;
        }
        if (!botUser.getId().equals(community.getOwner().getId())) {
            community.setOwner(botUser);
            changed = true;
        }
        if (changed) {
            communityRepository.save(community);
        }

        try {
            if (!communityService.isUserMemberOfCommunity(botUser.getId(), community.getId())) {
                communityService.joinCommunity(community.getId(), botUser.getId());
            }
        } catch (IllegalStateException ignored) {
            logger.debug("Bot {} already a member of {}", botUser.getEmail(), community.getName());
        } catch (Exception ex) {
            logger.warn("Unable to ensure bot owner membership for {} in {}: {}", botUser.getEmail(), community.getName(), ex.getMessage());
        }

        return community;
    }

    private void seedBotCommunityPosts(Community community, User owner, CommunityDefinition definition) {
        if (definition.starterPosts().isEmpty()) {
            return;
        }

        List<BotPost> targetPosts = definition.starterPosts().subList(0, Math.min(3, definition.starterPosts().size()));
        Map<String, Post> existingPostsByContent = postRepository.findByCommunityOrderByCreatedAtDesc(community).stream()
                .collect(Collectors.toMap(Post::getContent, post -> post, (first, second) -> first, LinkedHashMap::new));

        for (BotPost starterPost : targetPosts) {
            Post existingPost = existingPostsByContent.get(starterPost.content());
            if (existingPost != null) {
                updatePostImageIfNeeded(existingPost, starterPost.imageUrl());
                continue;
            }

            Post createdPost = postService.createPost(
                    community.getId(),
                    owner.getId(),
                    starterPost.content(),
                    null,
                    new HashSet<>(starterPost.hashtags())
            );
            updatePostImageIfNeeded(createdPost, starterPost.imageUrl());
        }
    }

    private void updatePostImageIfNeeded(Post post, String desiredImageUrl) {
        if (post == null || desiredImageUrl == null || desiredImageUrl.isBlank()) {
            return;
        }
        if (!desiredImageUrl.equals(post.getImageUrl())) {
            post.setImageUrl(desiredImageUrl);
            if (post.getHashtags() != null) {
                post.setHashtags(new HashSet<>(post.getHashtags()));
            }
            postRepository.save(post);
        }
    }

    private void seedCrossBotEngagement(List<BotProfileDefinition> configuredBots) {
        List<User> botUsers = configuredBots.stream()
                .map(definition -> userRepository.findByEmail(definition.email()).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (botUsers.size() < 2) {
            return;
        }

        for (int definitionIndex = 0; definitionIndex < configuredBots.size(); definitionIndex++) {
            BotProfileDefinition definition = configuredBots.get(definitionIndex);
            Community community = communityRepository.findByName(definition.community().name()).orElse(null);
            if (community == null) {
                continue;
            }

            List<Post> communityPosts = postRepository.findByCommunityOrderByCreatedAtDesc(community);
            if (communityPosts.isEmpty()) {
                continue;
            }

            List<User> engagers = botUsers.stream()
                    .filter(user -> !user.getId().equals(community.getOwner().getId()))
                    .collect(Collectors.toList());
            if (engagers.isEmpty()) {
                continue;
            }

            int offset = definitionIndex % engagers.size();
            User firstEngager = engagers.get(offset);
            User secondEngager = engagers.size() > 1 ? engagers.get((offset + 1) % engagers.size()) : null;
            User thirdEngager = engagers.size() > 2 ? engagers.get((offset + 2) % engagers.size()) : null;

            int postLimit = Math.min(3, communityPosts.size());
            for (int postIndex = 0; postIndex < postLimit; postIndex++) {
                Post post = communityPosts.get(postIndex);
                seedLikeIfMissing(post, firstEngager);
                if (secondEngager != null) {
                    seedLikeIfMissing(post, secondEngager);
                }
                if (thirdEngager != null) {
                    seedLikeIfMissing(post, thirdEngager);
                }

                seedCommentIfMissing(post, firstEngager, buildEngagementComment(post, community.getName(), 1));
                if (secondEngager != null) {
                    seedCommentIfMissing(post, secondEngager, buildEngagementComment(post, community.getName(), 2));
                }
            }
        }
    }

    private void seedCrossCommunityMembershipAndPosts(List<BotProfileDefinition> configuredBots) {
        if (!crossCommunityEnabled) {
            logger.info("Cross-community bot seeding is disabled (app.bots.cross-community.enabled=false).");
            return;
        }

        List<BotSeedContext> botContexts = configuredBots.stream()
                .map(definition -> userRepository.findByEmail(definition.email())
                        .map(user -> new BotSeedContext(definition, user))
                        .orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (botContexts.isEmpty()) {
            logger.info("No active bots found for cross-community seeding.");
            return;
        }

        Set<Long> botUserIds = botContexts.stream()
                .map(context -> context.user().getId())
                .collect(Collectors.toSet());

        List<Community> targetCommunities = resolveCrossCommunityTargets(botUserIds);
        if (targetCommunities.isEmpty()) {
            logger.info("No target communities found for cross-community seeding.");
            return;
        }

        int communitiesProcessed = 0;
        int membershipsCreated = 0;
        int postsCreated = 0;
        int skipped = 0;
        int errors = 0;

        for (Community community : targetCommunities) {
            if (community == null || community.getId() == null) {
                skipped++;
                continue;
            }

            communitiesProcessed++;
            for (BotSeedContext context : botContexts) {
                User botUser = context.user();
                BotProfileDefinition definition = context.definition();

                if (botUser == null || botUser.getId() == null) {
                    skipped++;
                    continue;
                }

                try {
                    if (ensureBotMembership(community, botUser)) {
                        membershipsCreated++;
                    }
                } catch (Exception ex) {
                    errors++;
                    logger.warn("Failed to ensure membership for bot {} in {}: {}", botUser.getEmail(), community.getName(), ex.getMessage());
                    continue;
                }

                if (community.getOwner() == null || community.getOwner().getId() == null) {
                    skipped++;
                    continue;
                }
                if (community.getOwner().getId().equals(botUser.getId())) {
                    continue;
                }

                try {
                    postsCreated += seedCrossCommunityPosts(community, botUser, definition);
                } catch (Exception ex) {
                    errors++;
                    logger.warn("Failed to seed cross-community posts for bot {} in {}: {}", botUser.getEmail(), community.getName(), ex.getMessage());
                }
            }
        }

        logger.info(
                "Cross-community seeding summary: communitiesProcessed={}, membershipsCreated={}, postsCreated={}, skipped={}, errors={}",
                communitiesProcessed,
                membershipsCreated,
                postsCreated,
                skipped,
                errors
        );
    }

    private List<Community> resolveCrossCommunityTargets(Set<Long> botUserIds) {
        List<Community> allCommunities = communityRepository.findAll();
        if (includeHumanCommunities) {
            return allCommunities;
        }

        return allCommunities.stream()
                .filter(community -> community.getOwner() != null
                        && community.getOwner().getId() != null
                        && botUserIds.contains(community.getOwner().getId()))
                .collect(Collectors.toList());
    }

    private boolean ensureBotMembership(Community community, User botUser) {
        try {
            if (!communityService.isUserMemberOfCommunity(botUser.getId(), community.getId())) {
                communityService.joinCommunity(community.getId(), botUser.getId());
                return true;
            }
        } catch (IllegalStateException ignored) {
            logger.debug("Bot {} already joined {}.", botUser.getEmail(), community.getName());
        }
        return false;
    }

    private int seedCrossCommunityPosts(Community community, User botUser, BotProfileDefinition definition) {
        int targetPosts = resolveCrossCommunityPostTarget(botUser.getId(), community.getId());
        if (targetPosts <= 0) {
            return 0;
        }

        List<Post> existingPosts = postRepository.findByCommunityAndAuthorOrderByCreatedAtDesc(community, botUser);
        Map<String, Post> existingPostsByContent = existingPosts.stream()
                .collect(Collectors.toMap(Post::getContent, post -> post, (first, second) -> first, LinkedHashMap::new));
        Set<String> existingContents = existingPosts.stream()
                .map(Post::getContent)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        int createdCount = 0;
        for (int slot = 0; slot < targetPosts; slot++) {
            String content = buildCrossCommunityPostContent(definition, community, slot);
            String imageUrl = buildCrossCommunityImageUrl(definition, community, slot);
            if (existingContents.contains(content)) {
                updatePostImageIfNeeded(existingPostsByContent.get(content), imageUrl);
                continue;
            }

            Set<String> hashtags = buildCrossCommunityHashtags(definition, community, slot);
            Post createdPost = postService.createPost(community.getId(), botUser.getId(), content, null, hashtags);
            updatePostImageIfNeeded(createdPost, imageUrl);
            existingContents.add(content);
            existingPostsByContent.put(content, createdPost);
            createdCount++;
        }

        return createdCount;
    }

    private int resolveCrossCommunityPostTarget(Long botUserId, Long communityId) {
        int minPosts = Math.max(0, Math.min(crossCommunityMinPosts, crossCommunityMaxPosts));
        int maxPosts = Math.max(minPosts, Math.max(crossCommunityMinPosts, crossCommunityMaxPosts));
        int range = maxPosts - minPosts + 1;
        return minPosts + Math.floorMod(Objects.hash(botUserId, communityId), range);
    }

    private String buildCrossCommunityPostContent(BotProfileDefinition definition, Community community, int slot) {
        String communityName = community.getName() == null ? "this community" : community.getName();
        String botName = definition.name();
        List<String> communityTagPhrases = normalizePhraseValues(
                community.getTags() == null ? List.of() : new ArrayList<>(community.getTags())
        );
        String interest = pickByIndex(normalizePhraseValues(definition.interests()), slot, "peer learning");
        String communityTag = pickByIndex(communityTagPhrases, slot, "community goals");
        int variant = Math.floorMod(slot, 3);

        if (variant == 0) {
            return String.format(
                    Locale.ROOT,
                    "%s here from another study circle. In %s, who is currently working on %s? I can share a simple weekly plan and compare notes.",
                    botName,
                    communityName,
                    interest
            );
        }
        if (variant == 1) {
            return String.format(
                    Locale.ROOT,
                    "Cross-community check-in for %s: I noticed strong momentum around %s. If you want, I can post a lightweight collaboration template for this week.",
                    communityName,
                    communityTag
            );
        }
        return String.format(
                Locale.ROOT,
                "Quick collaboration thread in %s. I am focusing on %s this week and happy to pair with anyone who wants accountability.",
                communityName,
                interest
        );
    }

    private Set<String> buildCrossCommunityHashtags(BotProfileDefinition definition, Community community, int slot) {
        LinkedHashSet<String> hashtags = new LinkedHashSet<>();

        List<String> communityTags = sanitizeTokens(community.getTags() == null ? List.of() : new ArrayList<>(community.getTags()));
        List<String> interests = sanitizeTokens(definition.interests());

        for (String tag : communityTags) {
            if (hashtags.size() >= 6) {
                break;
            }
            hashtags.add(tag);
        }

        int offset = Math.floorMod(slot, Math.max(1, interests.size()));
        for (int i = 0; i < interests.size() && hashtags.size() < 8; i++) {
            hashtags.add(interests.get((offset + i) % interests.size()));
        }

        hashtags.add("crosscommunity");
        hashtags.add("biblo");
        return hashtags;
    }

    private String buildCrossCommunityImageUrl(BotProfileDefinition definition, Community community, int slot) {
        if (CROSS_COMMUNITY_IMAGE_POOL.isEmpty()) {
            return null;
        }
        int index = Math.floorMod(
                Objects.hash(definition.email(), community.getName(), slot),
                CROSS_COMMUNITY_IMAGE_POOL.size()
        );
        return CROSS_COMMUNITY_IMAGE_POOL.get(index);
    }

    private List<String> sanitizeTokens(List<String> rawValues) {
        if (rawValues == null || rawValues.isEmpty()) {
            return List.of();
        }

        LinkedHashSet<String> unique = new LinkedHashSet<>();
        for (String rawValue : rawValues) {
            if (rawValue == null) {
                continue;
            }
            String normalized = rawValue.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "");
            if (!normalized.isBlank()) {
                unique.add(normalized);
            }
        }
        return new ArrayList<>(unique);
    }

    private List<String> normalizePhraseValues(List<String> rawValues) {
        if (rawValues == null || rawValues.isEmpty()) {
            return List.of();
        }

        LinkedHashSet<String> unique = new LinkedHashSet<>();
        for (String rawValue : rawValues) {
            if (rawValue == null) {
                continue;
            }
            String normalized = rawValue.replaceAll("[\\r\\n\\t]+", " ")
                    .replaceAll("\\s+", " ")
                    .trim();
            if (!normalized.isBlank()) {
                unique.add(normalized.toLowerCase(Locale.ROOT));
            }
        }
        return new ArrayList<>(unique);
    }

    private String pickByIndex(List<String> values, int index, String fallback) {
        if (values == null || values.isEmpty()) {
            return fallback;
        }
        return values.get(Math.floorMod(index, values.size()));
    }

    private void seedLikeIfMissing(Post post, User user) {
        if (likeRepository.findByPostAndUser(post, user).isPresent()) {
            return;
        }
        Like like = new Like();
        like.setPost(post);
        like.setUser(user);
        likeRepository.save(like);
    }

    private void seedCommentIfMissing(Post post, User author, String content) {
        if (commentRepository.existsByPostAndAuthorAndContent(post, author, content)) {
            return;
        }
        Comment comment = new Comment();
        comment.setPost(post);
        comment.setAuthor(author);
        comment.setContent(content);
        commentRepository.save(comment);
    }

    private String buildEngagementComment(Post post, String communityName, int variant) {
        String snippet = normalizePostSnippet(post.getContent());
        int normalizedVariant = Math.floorMod(variant, 4);
        if (normalizedVariant == 0) {
            return "This is useful context for " + communityName + ". I can share a checklist if anyone wants one.";
        }
        if (normalizedVariant == 1) {
            return "Great thread in " + communityName + ". The point about \"" + snippet + "\" is practical.";
        }
        if (normalizedVariant == 2) {
            return "I am in for this. I will share my progress in " + communityName + " later this week.";
        }
        return "Following this thread. I tried a similar approach and it helped me move faster.";
    }

    private String normalizePostSnippet(String content) {
        if (content == null || content.isBlank()) {
            return "this plan";
        }
        String normalized = content.replace("\"", "").replaceAll("\\s+", " ").trim();
        if (normalized.length() <= 64) {
            return normalized;
        }
        return normalized.substring(0, 64).trim() + "...";
    }

    private record BotProfileDefinition(
            String name,
            String email,
            String rawPassword,
            int age,
            String gender,
            String university,
            String major,
            String location,
            String bio,
            String profilePictureUrl,
            List<String> interests,
            List<Language> languages,
            LookingFor lookingFor,
            SocialLinks socialLinks,
            String welcomeMessage,
            CommunityDefinition community
    ) {}

    private record CommunityDefinition(
            String name,
            String description,
            List<String> tags,
            String imageUrl,
            List<BotPost> starterPosts
    ) {}

    private record BotPost(String content, Set<String> hashtags, String imageUrl) {
        private BotPost(String content, Set<String> hashtags) {
            this(content, hashtags, null);
        }
    }

    private record Language(String name, String level) {}

    private record LookingFor(
            boolean studyPartner,
            boolean languageExchange,
            boolean friendship,
            boolean networking,
            boolean community
    ) {}

    private record SocialLinks(String github, String linkedin, String instagram) {}

    private record BotSeedContext(BotProfileDefinition definition, User user) {}

    public record BotDefinitionSnapshot(
            String name,
            String email,
            String welcomeMessage,
            List<String> interests
    ) {}
}
