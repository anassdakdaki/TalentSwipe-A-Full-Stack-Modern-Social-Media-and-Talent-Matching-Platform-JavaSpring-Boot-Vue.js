package com.example.biblov1.controller;

import com.example.biblov1.model.User;
import com.example.biblov1.model.UserProfile;
import com.example.biblov1.payload.request.LoginRequest;
import com.example.biblov1.payload.request.SignupRequest;
import com.example.biblov1.payload.response.JwtResponse;
import com.example.biblov1.payload.response.MessageResponse;
import com.example.biblov1.repository.UserRepository;
import com.example.biblov1.repository.UserProfileRepository;
import com.example.biblov1.config.security.jwt.JwtUtils;
import com.example.biblov1.service.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserProfileRepository userProfileRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);
            
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            
            return ResponseEntity.ok(new JwtResponse(jwt, 
                                                   userDetails.getId(), 
                                                   userDetails.getUsername(), 
                                                   userDetails.getEmail()));
        } catch (Exception e) {
            logger.error("Login error for email {}: {}", loginRequest.getEmail(), e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("Error: Invalid email or password"));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        try {
            logger.info("Received registration request for email: {}", signUpRequest.getEmail());
            
            if (userRepository.existsByEmail(signUpRequest.getEmail())) {
                logger.warn("Registration failed: Email already in use - {}", signUpRequest.getEmail());
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("Error: Email is already in use!"));
            }

            // Create new user's account
            User user = new User(
                signUpRequest.getName(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword())
            );

            user = userRepository.save(user);
            
            // Create user profile
            UserProfile userProfile = new UserProfile();
            userProfile.setUser(user);
            userProfile.setName(signUpRequest.getName());
            userProfile.setEmail(signUpRequest.getEmail());
            userProfile.setAge(null); // Default to null
            userProfile.setGender(null);
            userProfile.setUniversity("");
            userProfile.setMajor("");
            userProfile.setLocation("");
            userProfile.setBio("");
            userProfile.setInterests(new ArrayList<>());
            userProfile.setLanguages(new ArrayList<>());
            userProfile.setLookingFor(new UserProfile.LookingFor());
            userProfile.setSocialLinks(new UserProfile.SocialLinks());
            userProfileRepository.save(userProfile);
            
            logger.info("User and profile registered successfully: {}", signUpRequest.getEmail());

            return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
        } catch (Exception e) {
            logger.error("Registration error for email {}: {}", signUpRequest.getEmail(), e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "User not authenticated"));
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return ResponseEntity.ok(Map.of("id", userDetails.getId(), "email", userDetails.getEmail(), "username", userDetails.getUsername()));
    }
} 