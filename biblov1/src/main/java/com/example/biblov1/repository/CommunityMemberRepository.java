package com.example.biblov1.repository;

import com.example.biblov1.model.CommunityMember;
import com.example.biblov1.model.Community;
import com.example.biblov1.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CommunityMemberRepository extends JpaRepository<CommunityMember, Long> {
    List<CommunityMember> findByCommunity(Community community);
    List<CommunityMember> findByUser(User user);
    Optional<CommunityMember> findByCommunityAndUser(Community community, User user);
    long countByCommunity(Community community);
} 