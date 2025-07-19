package com.example.biblov1.repository;

import com.example.biblov1.model.Community;
import com.example.biblov1.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByCommunityOrderByCreatedAtDesc(Community community);
} 