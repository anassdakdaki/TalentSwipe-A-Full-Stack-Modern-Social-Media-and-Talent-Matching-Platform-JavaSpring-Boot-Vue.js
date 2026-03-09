package com.example.biblov1.repository;

import com.example.biblov1.model.Like;
import com.example.biblov1.model.Post;
import com.example.biblov1.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
    interface PostLikeCountProjection {
        Long getPostId();
        Long getCount();
    }

    Optional<Like> findByPostAndUser(Post post, User user);
    long countByPost(Post post);
    List<Like> findByUser(User user);
    List<Like> findTop30ByPost_Author_IdAndUser_IdNotOrderByCreatedAtDesc(Long authorId, Long userId);
    @Query("""
            select l.post.id as postId, count(l) as count
            from Like l
            where l.post.id in :postIds
            group by l.post.id
            """)
    List<PostLikeCountProjection> countByPostIds(@Param("postIds") List<Long> postIds);
    @Query("""
            select l.post.id
            from Like l
            where l.user.id = :userId and l.post.id in :postIds
            """)
    List<Long> findLikedPostIdsByUserIdAndPostIds(@Param("userId") Long userId, @Param("postIds") List<Long> postIds);
} 
