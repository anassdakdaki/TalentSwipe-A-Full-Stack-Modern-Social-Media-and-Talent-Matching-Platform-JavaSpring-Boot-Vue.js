package com.example.biblov1.repository;

import com.example.biblov1.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
 
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    Optional<UserProfile> findByUserId(Long userId);
    Optional<UserProfile> findByEmail(String email);
    List<UserProfile> findByUserIdIn(List<Long> userIds);
    
    List<UserProfile> findByUserIdNot(Long userId);
} 
