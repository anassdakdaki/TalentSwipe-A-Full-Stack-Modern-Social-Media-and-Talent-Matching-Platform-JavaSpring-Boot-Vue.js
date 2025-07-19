package com.example.biblov1.repository;

import com.example.biblov1.model.ChatRoom;
import com.example.biblov1.model.User;
import com.example.biblov1.model.StudyMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    Optional<ChatRoom> findByStudyMatch(StudyMatch studyMatch);
    List<ChatRoom> findByUser1OrUser2(User user1, User user2);
} 