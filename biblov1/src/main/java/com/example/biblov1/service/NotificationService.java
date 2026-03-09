package com.example.biblov1.service;

import com.example.biblov1.model.ChatRoom;
import com.example.biblov1.model.Comment;
import com.example.biblov1.model.Like;
import com.example.biblov1.model.Message;
import com.example.biblov1.model.StudyMatch;
import com.example.biblov1.model.StudyMatch.MatchStatus;
import com.example.biblov1.model.User;
import com.example.biblov1.payload.response.AppNotificationResponse;
import com.example.biblov1.payload.response.NotificationFeedResponse;
import com.example.biblov1.repository.CommentRepository;
import com.example.biblov1.repository.LikeRepository;
import com.example.biblov1.repository.MessageRepository;
import com.example.biblov1.repository.StudyMatchRepository;
import com.example.biblov1.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private final UserRepository userRepository;
    private final ChatService chatService;
    private final MessageRepository messageRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final StudyMatchRepository studyMatchRepository;

    @Autowired
    public NotificationService(
            UserRepository userRepository,
            ChatService chatService,
            MessageRepository messageRepository,
            LikeRepository likeRepository,
            CommentRepository commentRepository,
            StudyMatchRepository studyMatchRepository
    ) {
        this.userRepository = userRepository;
        this.chatService = chatService;
        this.messageRepository = messageRepository;
        this.likeRepository = likeRepository;
        this.commentRepository = commentRepository;
        this.studyMatchRepository = studyMatchRepository;
    }

    @Transactional(readOnly = true)
    public NotificationFeedResponse getNotificationFeed(Long userId, int requestedLimit, LocalDateTime since) {
        int limit = Math.min(Math.max(requestedLimit, 1), 60);
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<AppNotificationResponse> notifications = new ArrayList<>();
        List<ChatRoom> userChatRooms = chatService.getUserChatRooms(userId);

        Map<Long, Long> matchToChatRoomId = userChatRooms.stream()
                .filter(room -> room.getStudyMatch() != null && room.getStudyMatch().getId() != null)
                .collect(Collectors.toMap(room -> room.getStudyMatch().getId(), ChatRoom::getId, (first, second) -> first));

        if (!userChatRooms.isEmpty()) {
            List<Message> messages = messageRepository.findTop30ByChatRoomInAndSender_IdNotOrderByTimestampDesc(userChatRooms, userId);
            for (Message message : messages) {
                String senderName = message.getSender() != null ? message.getSender().getName() : "Someone";
                Long chatRoomId = message.getChatRoom() != null ? message.getChatRoom().getId() : null;
                notifications.add(new AppNotificationResponse(
                        "message-" + message.getId(),
                        "MESSAGE",
                        "New message",
                        senderName + ": " + trimForNotification(message.getContent(), 80),
                        senderName,
                        chatRoomId,
                        chatRoomId != null ? "/authenticated/chat/" + chatRoomId : "/authenticated/chat",
                        safeTime(message.getTimestamp())
                ));
            }
        }

        List<Like> likes = likeRepository.findTop30ByPost_Author_IdAndUser_IdNotOrderByCreatedAtDesc(userId, userId);
        for (Like like : likes) {
            String actorName = like.getUser() != null ? like.getUser().getName() : "Someone";
            Long communityId = like.getPost() != null && like.getPost().getCommunity() != null
                    ? like.getPost().getCommunity().getId() : null;
            Long postId = like.getPost() != null ? like.getPost().getId() : null;
            notifications.add(new AppNotificationResponse(
                    "like-" + like.getId(),
                    "LIKE",
                    "New like",
                    actorName + " liked your post.",
                    actorName,
                    postId,
                    communityId != null ? "/authenticated/communities/" + communityId : "/authenticated/communities",
                    safeTime(like.getCreatedAt())
            ));
        }

        List<Comment> comments = commentRepository.findTop30ByPost_Author_IdAndAuthor_IdNotOrderByCreatedAtDesc(userId, userId);
        for (Comment comment : comments) {
            String actorName = comment.getAuthor() != null ? comment.getAuthor().getName() : "Someone";
            Long communityId = comment.getPost() != null && comment.getPost().getCommunity() != null
                    ? comment.getPost().getCommunity().getId() : null;
            Long postId = comment.getPost() != null ? comment.getPost().getId() : null;
            notifications.add(new AppNotificationResponse(
                    "comment-" + comment.getId(),
                    "COMMENT",
                    "New comment",
                    actorName + ": " + trimForNotification(comment.getContent(), 80),
                    actorName,
                    postId,
                    communityId != null ? "/authenticated/communities/" + communityId : "/authenticated/communities",
                    safeTime(comment.getCreatedAt())
            ));
        }

        List<StudyMatch> matches = new ArrayList<>();
        matches.addAll(studyMatchRepository.findByUser1AndStatus(currentUser, MatchStatus.MATCHED));
        matches.addAll(studyMatchRepository.findByUser2AndStatus(currentUser, MatchStatus.MATCHED));
        List<StudyMatch> recentMatches = matches.stream()
                .sorted(Comparator.comparing((StudyMatch match) -> safeTime(match.getUpdatedAt())).reversed())
                .limit(30)
                .collect(Collectors.toList());
        for (StudyMatch match : recentMatches) {
            User otherUser = currentUser.getId().equals(match.getUser1().getId()) ? match.getUser2() : match.getUser1();
            String actorName = otherUser != null ? otherUser.getName() : "A user";
            Long relatedChatRoomId = matchToChatRoomId.get(match.getId());
            notifications.add(new AppNotificationResponse(
                    "match-" + match.getId(),
                    "MATCH",
                    "New match",
                    "You matched with " + actorName + ".",
                    actorName,
                    match.getId(),
                    relatedChatRoomId != null ? "/authenticated/chat/" + relatedChatRoomId : "/authenticated/matches",
                    safeTime(match.getUpdatedAt())
            ));
        }

        notifications.sort(Comparator.comparing((AppNotificationResponse notification) -> safeTime(notification.getCreatedAt())).reversed());
        List<AppNotificationResponse> limitedNotifications = notifications.size() > limit
                ? new ArrayList<>(notifications.subList(0, limit))
                : notifications;

        LocalDateTime baseline = since != null ? since : LocalDateTime.MIN;
        long unreadCount = limitedNotifications.stream()
                .filter(notification -> safeTime(notification.getCreatedAt()).isAfter(baseline))
                .count();

        Map<String, Long> countsByType = new HashMap<>();
        limitedNotifications.forEach(notification -> countsByType.merge(notification.getType(), 1L, Long::sum));

        return new NotificationFeedResponse(limitedNotifications, unreadCount, countsByType);
    }

    private LocalDateTime safeTime(LocalDateTime time) {
        return time != null ? time : LocalDateTime.MIN;
    }

    private String trimForNotification(String text, int maxLength) {
        if (text == null || text.isBlank()) {
            return "";
        }
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength).trim() + "...";
    }
}
