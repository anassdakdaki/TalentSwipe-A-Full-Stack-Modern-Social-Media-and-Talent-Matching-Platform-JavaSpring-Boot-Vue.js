package com.example.biblov1.service;

import com.example.biblov1.model.Skill;
import com.example.biblov1.model.User;
import com.example.biblov1.repository.SkillRepository;
import com.example.biblov1.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SkillService {

    private final SkillRepository skillRepository;
    private final UserRepository userRepository;

    public SkillService(SkillRepository skillRepository, UserRepository userRepository) {
        this.skillRepository = skillRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Skill createSkill(Skill skill) {
        if (skill == null || skill.getUser() == null || skill.getUser().getId() == null) {
            throw new IllegalArgumentException("Skill and associated user must not be null");
        }

        Long userId = skill.getUser().getId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        skill.setUser(user);
        return skillRepository.save(skill);
    }

    @Transactional(readOnly = true)
    public List<Skill> getAllSkills() {
        return skillRepository.findAll();
    }
}
