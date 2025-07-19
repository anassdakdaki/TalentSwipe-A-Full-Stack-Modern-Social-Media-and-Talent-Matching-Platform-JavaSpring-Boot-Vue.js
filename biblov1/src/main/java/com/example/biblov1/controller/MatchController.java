package com.example.biblov1.controller;

import com.example.biblov1.model.StudyMatch;
import com.example.biblov1.model.User;
import com.example.biblov1.service.MatchService;
import com.example.biblov1.service.UserService;
import com.example.biblov1.model.UserSwipe.SwipeType;
import com.example.biblov1.payload.request.SwipeRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/matches")
@CrossOrigin(origins = "http://localhost:5173")
public class MatchController {
    private static final Logger logger = LoggerFactory.getLogger(MatchController.class);
    private final MatchService matchService;
    private final UserService userService;

    @Autowired
    public MatchController(MatchService matchService, UserService userService) {
        this.matchService = matchService;
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<List<StudyMatch>> getUserMatches() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        User currentUser = userService.getUserByEmail(userEmail);

        List<StudyMatch> matches = matchService.getUserMatches(currentUser);
        return ResponseEntity.ok(matches);
    }

    @PostMapping("/swipe")
    public ResponseEntity<?> processSwipe(@RequestAttribute("userId") Long swiperId, @RequestBody SwipeRequest swipeRequest) {
        try {
            logger.info("Processing swipe from user {} on user {} with type {}", swiperId, swipeRequest.getSwipedUserId(), swipeRequest.getSwipeType());
            boolean isMatch = matchService.processSwipe(swiperId, swipeRequest.getSwipedUserId(), swipeRequest.getSwipeType());
            if (isMatch) {
                logger.info("Match occurred between {} and {}", swiperId, swipeRequest.getSwipedUserId());
                return ResponseEntity.ok(Map.of("match", true));
            } else {
                return ResponseEntity.ok(Map.of("match", false));
            }
        } catch (Exception e) {
            logger.error("Error processing swipe: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // The acceptMatch method seems specific to study groups, might not be needed for direct user matches
    // @PostMapping("/{matchId}/accept")
    // public ResponseEntity<StudyMatch> acceptMatch(@PathVariable Long matchId) {
    //     StudyMatch match = matchService.acceptMatch(matchId);
    //     return ResponseEntity.ok(match);
    // }
} 