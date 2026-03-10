package com.example.biblov1.repository;

import com.example.biblov1.model.Community;
import com.example.biblov1.model.Post;
import com.example.biblov1.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByCommunity(Community community);
    List<Post> findByCommunityOrderByCreatedAtDesc(Community community);
    List<Post> findByCommunityAndAuthorOrderByCreatedAtDesc(Community community, User author);
    void deleteByCommunity(Community community);
    List<Post> findTop10ByAuthorOrderByCreatedAtDesc(User author);
    @Query("""
            select p from Post p
            where p.community.id in :communityIds
            order by p.createdAt desc, p.id desc
            """)
    List<Post> findFeedPostsByCommunityIds(
            @Param("communityIds") List<Long> communityIds,
            Pageable pageable
    );
    @Query("""
            select p from Post p
            where p.community.id in :communityIds
              and (p.createdAt < :cursorCreatedAt or (p.createdAt = :cursorCreatedAt and p.id < :cursorPostId))
            order by p.createdAt desc, p.id desc
            """)
    List<Post> findFeedPostsByCommunityIdsBeforeCursor(
            @Param("communityIds") List<Long> communityIds,
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorPostId") Long cursorPostId,
            Pageable pageable
    );
    @Query("""
            select p from Post p
            where p.author.id not in :excludedAuthorIds
            order by p.createdAt desc, p.id desc
            """)
    List<Post> findRecentPostsByAuthorIdsNotIn(
            @Param("excludedAuthorIds") List<Long> excludedAuthorIds,
            Pageable pageable
    );
    long countByCommunity(Community community);
    long countByAuthor(User author);
} 
