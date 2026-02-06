package com.example.biblov1.service;

import com.example.biblov1.model.ChatRoom;
import com.example.biblov1.model.Message;
import com.example.biblov1.model.User;
import com.example.biblov1.model.StudyMatch;
import com.example.biblov1.repository.ChatRoomRepository;
import com.example.biblov1.repository.MessageRepository;
import com.example.biblov1.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    @Autowired
    public ChatService(ChatRoomRepository chatRoomRepository, MessageRepository messageRepository, UserRepository userRepository) {
        this.chatRoomRepository = chatRoomRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ChatRoom createChatRoom(User user1, User user2, StudyMatch studyMatch) {
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setUser1(user1);
        chatRoom.setUser2(user2);
        chatRoom.setStudyMatch(studyMatch);
        return chatRoomRepository.save(chatRoom);
    }

    @Transactional
    public Message saveMessage(Long chatRoomId, Long senderId, String content) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new RuntimeException("ChatRoom not found"));
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        // Ensure sender is part of the chat room
        if (!isParticipant(chatRoom, senderId)) {
            throw new AccessDeniedException("Sender is not a participant of this chat room.");
        }

        Message message = new Message();
        message.setChatRoom(chatRoom);
        message.setSender(sender);
        message.setContent(content);
        return messageRepository.save(message);
    }

    @Transactional(readOnly = true)
    public List<Message> getMessagesByChatRoom(Long chatRoomId, Long requesterId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new RuntimeException("ChatRoom not found"));
        if (!isParticipant(chatRoom, requesterId)) {
            throw new AccessDeniedException("User is not a participant of this chat room.");
        }
        return messageRepository.findByChatRoomOrderByTimestampAsc(chatRoom);
    }

    @Transactional(readOnly = true)
    public List<ChatRoom> getUserChatRooms(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return chatRoomRepository.findByUser1OrUser2(user, user);
    }

    // Method to find an existing chat room or create a new one if it doesn't exist
    @Transactional
    public ChatRoom findOrCreateChatRoom(Long user1Id, Long user2Id, StudyMatch studyMatch) {
        // Fetch User objects by their IDs
        User u1 = userRepository.findById(user1Id)
                .orElseThrow(() -> new RuntimeException("User 1 not found"));
        User u2 = userRepository.findById(user2Id)
                .orElseThrow(() -> new RuntimeException("User 2 not found"));

        // Find existing chat room by match
        Optional<ChatRoom> existingChatRoom = chatRoomRepository.findByStudyMatch(studyMatch);

        if (existingChatRoom.isPresent()) {
            return existingChatRoom.get();
        } else {
            // Create a new chat room
            return createChatRoom(u1, u2, studyMatch);
        }
    }

    private boolean isParticipant(ChatRoom chatRoom, Long userId) {
        if (userId == null) {
            return false;
        }
        Long user1Id = chatRoom.getUser1() != null ? chatRoom.getUser1().getId() : null;
        Long user2Id = chatRoom.getUser2() != null ? chatRoom.getUser2().getId() : null;
        return userId.equals(user1Id) || userId.equals(user2Id);
    }
} 
