package com.example.biblov1.service;

import com.example.biblov1.model.ChatRoom;
import com.example.biblov1.model.Message;
import com.example.biblov1.model.User;
import com.example.biblov1.model.StudyMatch;
import com.example.biblov1.repository.ChatRoomRepository;
import com.example.biblov1.repository.MessageRepository;
import com.example.biblov1.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
        if (!chatRoom.getUser1().equals(sender) && !chatRoom.getUser2().equals(sender)) {
            throw new IllegalArgumentException("Sender is not a participant of this chat room.");
        }

        Message message = new Message();
        message.setChatRoom(chatRoom);
        message.setSender(sender);
        message.setContent(content);
        return messageRepository.save(message);
    }

    @Transactional(readOnly = true)
    public List<Message> getMessagesByChatRoom(Long chatRoomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new RuntimeException("ChatRoom not found"));
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
} 