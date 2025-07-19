package com.example.biblov1.repository;

import com.example.biblov1.model.StudyMatch;
import com.example.biblov1.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MatchRepository extends JpaRepository<StudyMatch, Long> {
    List<StudyMatch> findByUser1OrUser2(User user1, User user2);
    List<StudyMatch> findByUser1AndStatus(User user1, StudyMatch.MatchStatus status);
    List<StudyMatch> findByUser2AndStatus(User user2, StudyMatch.MatchStatus status);
    // Add more custom queries as needed
} 