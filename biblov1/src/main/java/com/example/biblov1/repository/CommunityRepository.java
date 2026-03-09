package com.example.biblov1.repository;

import com.example.biblov1.model.Community;
import com.example.biblov1.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommunityRepository extends JpaRepository<Community, Long> {
    Optional<Community> findByName(String name);
    long countByOwner(User owner);
    List<Community> findTop10ByOwnerOrderByCreatedAtDesc(User owner);
} 
