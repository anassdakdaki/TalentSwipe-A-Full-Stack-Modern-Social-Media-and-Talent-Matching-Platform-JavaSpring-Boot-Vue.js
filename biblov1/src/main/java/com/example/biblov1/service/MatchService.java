package com.example.biblov1.service;

import com.example.biblov1.model.StudyMatch;
import com.example.biblov1.model.User;
import com.example.biblov1.model.UserSwipe;
import com.example.biblov1.model.UserSwipe.SwipeType;
import com.example.biblov1.model.StudyMatch.MatchStatus;
import com.example.biblov1.repository.StudyMatchRepository;
import com.example.biblov1.repository.UserSwipeRepository;
import com.example.biblov1.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MatchService {
    private final StudyMatchRepository studyMatchRepository;
    private final UserSwipeRepository userSwipeRepository;
    private final UserRepository userRepository;
    private final ChatService chatService;
    private static final Logger logger = LoggerFactory.getLogger(MatchService.class);

    @Autowired
    public MatchService(StudyMatchRepository studyMatchRepository, UserSwipeRepository userSwipeRepository, UserRepository userRepository, ChatService chatService) {
        this.studyMatchRepository = studyMatchRepository;
        this.userSwipeRepository = userSwipeRepository;
        this.userRepository = userRepository;
        this.chatService = chatService;
    }

    // Method to handle a user's swipe action
    @Transactional
    public boolean processSwipe(Long swiperId, Long swipedId, SwipeType swipeType) {
        logger.info("Processing swipe: swiperId={}, swipedId={}, swipeType={}", swiperId, swipedId, swipeType);

        User swiper = userRepository.findById(swiperId)
                .orElseThrow(() -> new RuntimeException("Swiper user not found"));
        User swiped = userRepository.findById(swipedId)
                .orElseThrow(() -> new RuntimeException("Swiped user not found"));

        // Check if a swipe already exists from swiper to swiped
        Optional<UserSwipe> existingSwipe = userSwipeRepository.findBySwiperAndSwiped(swiper, swiped);

        UserSwipe userSwipe;
        if (existingSwipe.isPresent()) {
            logger.info("Updating existing swipe from {} to {}", swiperId, swipedId);
            userSwipe = existingSwipe.get();
            userSwipe.setSwipeType(swipeType);
            userSwipe.setCreatedAt(LocalDateTime.now()); // Update timestamp
        } else {
            logger.info("Creating new swipe from {} to {}", swiperId, swipedId);
            userSwipe = new UserSwipe();
            userSwipe.setSwiper(swiper);
            userSwipe.setSwiped(swiped);
            userSwipe.setSwipeType(swipeType);
        }

        try {
            logger.info("Attempting to save UserSwipe: swiperId={}, swipedId={}, swipeType={}", 
                        userSwipe.getSwiper().getId(), userSwipe.getSwiped().getId(), userSwipe.getSwipeType());
            userSwipeRepository.save(userSwipe);
            logger.info("UserSwipe saved successfully.");
        } catch (Exception e) {
            logger.error("Error saving UserSwipe: {}", e.getMessage(), e);
            // Depending on the error, you might want to re-throw or handle differently
            throw new RuntimeException("Failed to save user swipe", e);
        }

        boolean isMatch = false;
        // Check for a match only if the current swipe is a LIKE
        if (swipeType == SwipeType.LIKE) {
            // Check if the swiped user has also LIKED the swiper user
            Optional<UserSwipe> counterSwipe = userSwipeRepository.findBySwiperAndSwiped(swiped, swiper);

            if (counterSwipe.isPresent() && counterSwipe.get().getSwipeType() == SwipeType.LIKE) {
                // It's a match!
                StudyMatch match = createOrUpdateMatch(swiper, swiped);
                // Create or find chat room for the new match
                if (match != null) {
                    chatService.findOrCreateChatRoom(swiper.getId(), swiped.getId(), match);
                }
                isMatch = true;
            }
        }

        return isMatch;
    }

    // Helper method to create or update a StudyMatch
    private StudyMatch createOrUpdateMatch(User user1, User user2) {
        // Ensure user1.id < user2.id to avoid duplicate match entries for the same pair
        User u1 = user1.getId() < user2.getId() ? user1 : user2;
        User u2 = user1.getId() < user2.getId() ? user2 : user1;

        Optional<StudyMatch> existingMatch = studyMatchRepository.findByUser1AndUser2(u1, u2);

        StudyMatch match;
        if (existingMatch.isPresent()) {
            match = existingMatch.get();
            if (match.getStatus() != MatchStatus.MATCHED) {
                match.setStatus(MatchStatus.MATCHED);
                match.setUpdatedAt(LocalDateTime.now());
                studyMatchRepository.save(match);
            }
        } else {
            match = new StudyMatch();
            match.setUser1(u1);
            match.setUser2(u2);
            match.setStatus(MatchStatus.MATCHED);
            match.setCreatedAt(LocalDateTime.now());
            match.setUpdatedAt(LocalDateTime.now());
            studyMatchRepository.save(match);
        }
        return match;
    }

    public List<StudyMatch> getUserMatches(User user) {
        // Find matches where the user is either user1 or user2 and the status is MATCHED
        List<StudyMatch> matches1 = studyMatchRepository.findByUser1AndStatus(user, MatchStatus.MATCHED);
        List<StudyMatch> matches2 = studyMatchRepository.findByUser2AndStatus(user, MatchStatus.MATCHED);
        matches1.addAll(matches2);
        return matches1;
    }

    public StudyMatch getStudyMatchById(Long matchId) {
        return studyMatchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("StudyMatch not found with ID: " + matchId));
    }

    // The acceptMatch method seems specific to study groups, might not be needed for direct user matches
    // public StudyMatch acceptMatch(Long matchId) {
    //     StudyMatch match = studyMatchRepository.findById(matchId)
    //             .orElseThrow(() -> new RuntimeException("Match not found"));
    //     match.setAccepted(true);
    //     return studyMatchRepository.save(match);
    // }
} 