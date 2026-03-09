package com.example.biblov1.controller;

import com.example.biblov1.payload.request.onboarding.CompleteOnboardingRequest;
import com.example.biblov1.payload.response.onboarding.CompleteOnboardingResponse;
import com.example.biblov1.payload.response.onboarding.OnboardingOptionsResponse;
import com.example.biblov1.payload.response.onboarding.OnboardingStatusResponse;
import com.example.biblov1.service.OnboardingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/onboarding")
@CrossOrigin(origins = "http://localhost:5173")
public class OnboardingController {
    private final OnboardingService onboardingService;

    public OnboardingController(OnboardingService onboardingService) {
        this.onboardingService = onboardingService;
    }

    @GetMapping("/status")
    public ResponseEntity<OnboardingStatusResponse> getStatus(@RequestAttribute("userId") Long userId) {
        return ResponseEntity.ok(onboardingService.getStatus(userId));
    }

    @GetMapping("/options")
    public ResponseEntity<OnboardingOptionsResponse> getOptions(@RequestAttribute("userId") Long userId) {
        return ResponseEntity.ok(onboardingService.getOptions(userId));
    }

    @PutMapping("/complete")
    public ResponseEntity<CompleteOnboardingResponse> completeOnboarding(
            @RequestAttribute("userId") Long userId,
            @RequestBody CompleteOnboardingRequest request) {
        return ResponseEntity.ok(onboardingService.completeOnboarding(userId, request));
    }
}