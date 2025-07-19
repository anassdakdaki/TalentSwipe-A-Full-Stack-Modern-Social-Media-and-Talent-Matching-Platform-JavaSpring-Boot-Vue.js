package com.example.biblov1.controller;

import com.example.biblov1.model.User;
import com.example.biblov1.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class DashboardController {

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<?> getDashboardData() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            User user = userService.getUserByEmail(email);

            // Create a map with dashboard data
            Map<String, Object> dashboardData = new HashMap<>();
            
            // Sample study groups
            List<Map<String, Object>> groups = new ArrayList<>();
            groups.add(createGroup("Calculus Study Group", "Mathematics", "2024-03-25T15:00:00"));
            groups.add(createGroup("History Discussion", "History", "2024-03-26T14:00:00"));
            dashboardData.put("groups", groups);

            return ResponseEntity.ok(dashboardData);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching dashboard data: " + e.getMessage());
        }
    }

    private Map<String, Object> createGroup(String name, String subject, String nextMeeting) {
        Map<String, Object> group = new HashMap<>();
        group.put("id", UUID.randomUUID().toString());
        group.put("name", name);
        group.put("subject", subject);
        group.put("nextMeeting", nextMeeting);
        return group;
    }
} 