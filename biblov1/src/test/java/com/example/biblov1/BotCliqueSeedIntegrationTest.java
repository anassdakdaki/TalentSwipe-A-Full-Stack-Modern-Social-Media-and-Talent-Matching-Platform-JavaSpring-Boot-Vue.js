package com.example.biblov1;

import com.example.biblov1.model.StudyMatch;
import com.example.biblov1.model.UserSwipe;
import com.example.biblov1.repository.StudyMatchRepository;
import com.example.biblov1.repository.UserSwipeRepository;
import com.example.biblov1.service.BotIdentityService;
import com.example.biblov1.service.BotPopulationService;
import com.example.biblov1.testsupport.TestDatabaseCleanup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BotCliqueSeedIntegrationTest {
    @Autowired private TestDatabaseCleanup databaseCleanup;
    @Autowired private BotPopulationService botPopulationService;
    @Autowired private BotIdentityService botIdentityService;
    @Autowired private StudyMatchRepository studyMatchRepository;
    @Autowired private UserSwipeRepository userSwipeRepository;

    @BeforeEach
    void resetDb() {
        databaseCleanup.reset();
    }

    @Test
    void should_seed_bot_clique_with_reciprocal_likes_and_matches() {
        botPopulationService.seedBotProfilesAndCommunities();
        List<Long> botIds = botIdentityService.getActiveBotUserIds();
        assertThat(botIds.size()).isGreaterThan(1);

        int expectedMatchPairs = botIds.size() * (botIds.size() - 1) / 2;
        int expectedDirectionalLikes = botIds.size() * (botIds.size() - 1);

        for (Long botA : botIds) {
            for (Long botB : botIds) {
                if (botA.equals(botB)) {
                    continue;
                }
                assertThat(userSwipeRepository.existsBySwiper_IdAndSwiped_IdAndSwipeType(botA, botB, UserSwipe.SwipeType.LIKE))
                        .isTrue();
            }
        }

        long totalDirectionalLikes = userSwipeRepository.findAll().stream()
                .filter(swipe -> swipe.getSwipeType() == UserSwipe.SwipeType.LIKE)
                .filter(swipe -> swipe.getSwiper() != null && swipe.getSwiped() != null)
                .filter(swipe -> botIds.contains(swipe.getSwiper().getId()) && botIds.contains(swipe.getSwiped().getId()))
                .count();
        assertThat(totalDirectionalLikes).isEqualTo(expectedDirectionalLikes);

        long matchedPairs = studyMatchRepository.findAll().stream()
                .filter(match -> match.getStatus() == StudyMatch.MatchStatus.MATCHED)
                .filter(match -> match.getUser1() != null && match.getUser2() != null)
                .filter(match -> botIds.contains(match.getUser1().getId()) && botIds.contains(match.getUser2().getId()))
                .count();
        assertThat(matchedPairs).isEqualTo(expectedMatchPairs);
    }
}
