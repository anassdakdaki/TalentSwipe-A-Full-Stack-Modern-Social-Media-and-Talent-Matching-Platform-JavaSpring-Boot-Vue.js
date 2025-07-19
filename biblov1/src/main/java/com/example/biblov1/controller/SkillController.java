package com.example.biblov1.controller;

import com.example.biblov1.model.Skill;
import com.example.biblov1.model.User;
import com.example.biblov1.repository.UserRepository;
import com.example.biblov1.repository.SkillRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/skills")
public class SkillController {

    private final SkillRepository skillRepository;
    private final UserRepository userRepository;

    public SkillController(SkillRepository skillRepository, UserRepository userRepository) {
        this.skillRepository = skillRepository;
        this.userRepository = userRepository;
    }

    // Add skill to a user by userId
    @PostMapping("/user/{userId}")
    public ResponseEntity<?> addSkillToUser(@PathVariable Long userId, @RequestBody Skill skill) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().body("User not found with ID: " + userId);
        }

        skill.setUser(user);
        Skill savedSkill = skillRepository.save(skill);
        return ResponseEntity.ok(savedSkill);
    }
}
