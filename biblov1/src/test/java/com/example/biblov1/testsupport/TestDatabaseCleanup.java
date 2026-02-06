package com.example.biblov1.testsupport;

import com.example.biblov1.repository.*;
import org.springframework.stereotype.Component;

@Component
public class TestDatabaseCleanup {
    private final MessageRepository messageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final StudyMatchRepository studyMatchRepository;
    private final UserSwipeRepository userSwipeRepository;
    private final CommunityMemberRepository communityMemberRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final CommunityRepository communityRepository;
    private final SkillRepository skillRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;

    public TestDatabaseCleanup(
            MessageRepository messageRepository,
            ChatRoomRepository chatRoomRepository,
            StudyMatchRepository studyMatchRepository,
            UserSwipeRepository userSwipeRepository,
            CommunityMemberRepository communityMemberRepository,
            LikeRepository likeRepository,
            CommentRepository commentRepository,
            PostRepository postRepository,
            CommunityRepository communityRepository,
            SkillRepository skillRepository,
            UserProfileRepository userProfileRepository,
            UserRepository userRepository
    ) {
        this.messageRepository = messageRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.studyMatchRepository = studyMatchRepository;
        this.userSwipeRepository = userSwipeRepository;
        this.communityMemberRepository = communityMemberRepository;
        this.likeRepository = likeRepository;
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.communityRepository = communityRepository;
        this.skillRepository = skillRepository;
        this.userProfileRepository = userProfileRepository;
        this.userRepository = userRepository;
    }

    public void reset() {
        messageRepository.deleteAll();
        chatRoomRepository.deleteAll();
        studyMatchRepository.deleteAll();
        userSwipeRepository.deleteAll();
        likeRepository.deleteAll();
        commentRepository.deleteAll();
        postRepository.deleteAll();
        communityMemberRepository.deleteAll();
        communityRepository.deleteAll();
        skillRepository.deleteAll();
        userProfileRepository.deleteAll();
        userRepository.deleteAll();
    }
}

