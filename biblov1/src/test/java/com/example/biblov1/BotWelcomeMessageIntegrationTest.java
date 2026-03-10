package com.example.biblov1;

import com.example.biblov1.model.ChatRoom;
import com.example.biblov1.model.Message;
import com.example.biblov1.repository.ChatRoomRepository;
import com.example.biblov1.repository.MessageRepository;
import com.example.biblov1.service.BotIdentityService;
import com.example.biblov1.service.BotPopulationService;
import com.example.biblov1.testsupport.TestAuthSupport;
import com.example.biblov1.testsupport.TestDatabaseCleanup;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BotWelcomeMessageIntegrationTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private TestDatabaseCleanup databaseCleanup;
    @Autowired private BotPopulationService botPopulationService;
    @Autowired private BotIdentityService botIdentityService;
    @Autowired private ChatRoomRepository chatRoomRepository;
    @Autowired private MessageRepository messageRepository;

    @BeforeEach
    void resetDb() {
        databaseCleanup.reset();
    }

    @Test
    void should_send_delayed_welcome_message_after_human_matches_bot() throws Exception {
        botPopulationService.seedBotProfilesAndCommunities();
        Long botId = botIdentityService.getActiveBotUserIds().get(0);

        TestAuthSupport.TestUser human = TestAuthSupport.registerAndLogin(
                mockMvc,
                objectMapper,
                "Human Welcome",
                "welcome-human@example.com",
                "password123"
        );

        mockMvc.perform(
                        post("/api/matches/swipe")
                                .contentType(APPLICATION_JSON)
                                .header("Authorization", "Bearer " + human.token())
                                .content(objectMapper.writeValueAsString(Map.of(
                                        "swipedUserId", botId,
                                        "swipeType", "LIKE"
                                )))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.match").value(true));

        ChatRoom chatRoom = waitForChatRoom(human.id(), botId, Duration.ofSeconds(5));
        assertThat(chatRoom).isNotNull();

        Message botMessage = waitForBotMessage(chatRoom, botId, Duration.ofSeconds(5));
        assertThat(botMessage).isNotNull();
        assertThat(botMessage.getContent()).isNotBlank();
    }

    private ChatRoom waitForChatRoom(Long humanId, Long botId, Duration timeout) throws InterruptedException {
        Instant deadline = Instant.now().plus(timeout);
        while (Instant.now().isBefore(deadline)) {
            ChatRoom found = chatRoomRepository.findAll().stream()
                    .filter(room -> room.getUser1() != null && room.getUser2() != null)
                    .filter(room -> {
                        Long user1Id = room.getUser1().getId();
                        Long user2Id = room.getUser2().getId();
                        return (humanId.equals(user1Id) && botId.equals(user2Id))
                                || (humanId.equals(user2Id) && botId.equals(user1Id));
                    })
                    .findFirst()
                    .orElse(null);
            if (found != null) {
                return found;
            }
            Thread.sleep(100);
        }
        return null;
    }

    private Message waitForBotMessage(ChatRoom room, Long botId, Duration timeout) throws InterruptedException {
        Instant deadline = Instant.now().plus(timeout);
        while (Instant.now().isBefore(deadline)) {
            List<Message> messages = messageRepository.findByChatRoomOrderByTimestampAsc(room);
            Message fromBot = messages.stream()
                    .filter(message -> message.getSender() != null && botId.equals(message.getSender().getId()))
                    .findFirst()
                    .orElse(null);
            if (fromBot != null) {
                return fromBot;
            }
            Thread.sleep(100);
        }
        return null;
    }
}
