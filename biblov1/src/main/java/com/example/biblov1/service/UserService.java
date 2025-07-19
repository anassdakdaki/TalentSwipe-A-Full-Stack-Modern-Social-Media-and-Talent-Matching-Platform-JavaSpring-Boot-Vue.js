package com.example.biblov1.service;

import com.example.biblov1.model.User;
import com.example.biblov1.model.Skill;
import com.example.biblov1.repository.UserRepository;
import com.example.biblov1.repository.SkillRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final SkillRepository skillRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, 
                      SkillRepository skillRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.skillRepository = skillRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User createUser(User user) {
        // Check if email is null or empty
        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        
        // Create new user and set all fields
        User newUser = new User();
        newUser.setName(user.getName());
        newUser.setEmail(user.getEmail());
        
        // Encode and set the password
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            newUser.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        
        // Save the new user to the database
        return userRepository.save(newUser);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    public User updatePassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        user.setPassword(passwordEncoder.encode(newPassword));
        return userRepository.save(user);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found with id: " + userId);
        }
        userRepository.deleteById(userId);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }

    @Transactional
    public User updateUser(User user) {
        if (!userRepository.existsById(user.getId())) {
            throw new RuntimeException("User not found with id: " + user.getId());
        }
        return userRepository.save(user);
    }

    @Transactional
    public User addSkill(Long userId, Skill skill) {
        User user = getUserById(userId);
        skill.setUser(user);
        skillRepository.save(skill);
        return getUserById(userId); // Return updated user
    }

    @Transactional
    public User removeSkill(Long userId, Long skillId) {
        User user = getUserById(userId);
        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new RuntimeException("Skill not found with id: " + skillId));
        
        if (!skill.getUser().getId().equals(userId)) {
            throw new RuntimeException("Skill does not belong to user");
        }
        
        skillRepository.delete(skill);
        return getUserById(userId); // Return updated user
    }
}
