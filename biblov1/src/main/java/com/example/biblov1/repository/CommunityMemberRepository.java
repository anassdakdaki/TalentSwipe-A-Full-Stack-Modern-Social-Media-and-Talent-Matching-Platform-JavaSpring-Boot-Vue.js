package com.example.biblov1.repository;

import com.example.biblov1.model.CommunityMember;
import com.example.biblov1.model.Community;
import com.example.biblov1.model.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CommunityMemberRepository extends JpaRepository<CommunityMember, Long> {
    List<CommunityMember> findByCommunity(Community community);
    List<CommunityMember> findByUser(User user);
    List<CommunityMember> findTop10ByUserOrderByJoinedAtDesc(User user);
    Optional<CommunityMember> findByCommunityAndUser(Community community, User user);
    @Query("select cm.community.id from CommunityMember cm where cm.user.id = :userId")
    List<Long> findCommunityIdsByUserId(@Param("userId") Long userId);
    long countByCommunity(Community community);
    long countByUser(User user);
} 
