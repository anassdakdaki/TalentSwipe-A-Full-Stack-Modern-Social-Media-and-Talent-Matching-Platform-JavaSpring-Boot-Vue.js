package com.example.biblov1.controller;

import com.example.biblov1.model.ChatRoom;
import com.example.biblov1.model.Message;
import com.example.biblov1.model.User;
import com.example.biblov1.model.StudyMatch;
import com.example.biblov1.service.ChatService;
import com.example.biblov1.service.UserService;
import com.example.biblov1.service.MatchService;
import com.example.biblov1.payload.request.SendMessageRequest;
import com.example.biblov1.payload.request.FindOrCreateChatRoomRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "http://localhost:5173")
public class ChatController {

    private final ChatService chatService;
    private final UserService userService;
    private final MatchService matchService;

    @Autowired
    public ChatController(ChatService chatService, UserService userService, MatchService matchService) {
        this.chatService = chatService;
        this.userService = userService;
        this.matchService = matchService;
    }

    @GetMapping("/rooms")
    public ResponseEntity<List<ChatRoom>> getUserChatRooms(@RequestAttribute("userId") Long userId) {
        List<ChatRoom> chatRooms = chatService.getUserChatRooms(userId);
        return ResponseEntity.ok(chatRooms);
    }

    @GetMapping("/rooms/{chatRoomId}/messages")
    public ResponseEntity<?> getChatRoomMessages(@PathVariable Long chatRoomId, @RequestAttribute("userId") Long userId) {
        try {
            List<Message> messages = chatService.getMessagesByChatRoom(chatRoomId, userId);
            return ResponseEntity.ok(messages);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(@RequestAttribute("userId") Long senderId, @RequestBody SendMessageRequest request) {
        try {
            Message message = chatService.saveMessage(request.getChatRoomId(), senderId, request.getContent());
            return ResponseEntity.ok(message);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to send message: " + e.getMessage()));
        }
    }

    @PostMapping("/findOrCreate")
    public ResponseEntity<?> findOrCreateChatRoom(@RequestBody FindOrCreateChatRoomRequest request) {
        try {
            StudyMatch studyMatch = matchService.getStudyMatchById(request.getStudyMatchId());
            
            ChatRoom chatRoom = chatService.findOrCreateChatRoom(request.getUser1Id(), request.getUser2Id(), studyMatch);
            return ResponseEntity.ok(chatRoom);
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to find or create chat room: " + e.getMessage()));
        }
    }
} 
