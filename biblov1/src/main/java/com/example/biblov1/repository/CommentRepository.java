package com.example.biblov1.repository;

import com.example.biblov1.model.Comment;
import com.example.biblov1.model.Post;
import com.example.biblov1.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    interface PostCommentCountProjection {
        Long getPostId();
        Long getCount();
    }

    List<Comment> findByPostOrderByCreatedAtAsc(Post post);
    void deleteByPostIn(List<Post> posts);
    boolean existsByPostAndAuthor(Post post, User author);
    boolean existsByPostAndAuthorAndContent(Post post, User author, String content);
    List<Comment> findTop10ByAuthorOrderByCreatedAtDesc(User author);
    long countByAuthor(User author);
    List<Comment> findTop30ByPost_Author_IdAndAuthor_IdNotOrderByCreatedAtDesc(Long authorId, Long excludedAuthorId);
    @Query("""
            select c.post.id as postId, count(c) as count
            from Comment c
            where c.post.id in :postIds
            group by c.post.id
            """)
    List<PostCommentCountProjection> countByPostIds(@Param("postIds") List<Long> postIds);
} 
