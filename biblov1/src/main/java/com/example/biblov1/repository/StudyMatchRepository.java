package com.example.biblov1.repository;

import com.example.biblov1.model.StudyMatch;
import com.example.biblov1.model.User;
import com.example.biblov1.model.StudyMatch.MatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudyMatchRepository extends JpaRepository<StudyMatch, Long> {
    List<StudyMatch> findByUser1OrUser2(User user1, User user2);
    
    Optional<StudyMatch> findByUser1AndUser2(User user1, User user2);
    List<StudyMatch> findByUser1AndStatus(User user1, MatchStatus status);
    List<StudyMatch> findByUser2AndStatus(User user2, MatchStatus status);
} 