package com.example.biblov1;

import com.example.biblov1.testsupport.TestAuthSupport;
import com.example.biblov1.testsupport.TestDatabaseCleanup;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ChatAuthorizationIntegrationTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private TestDatabaseCleanup databaseCleanup;

    @BeforeEach
    void resetDb() {
        databaseCleanup.reset();
    }

    @Test
    void should_prevent_non_matched_user_from_sending_message() throws Exception {
        TestAuthSupport.TestUser a = TestAuthSupport.registerAndLogin(mockMvc, objectMapper, "Alice", "a@example.com", "password123");
        TestAuthSupport.TestUser b = TestAuthSupport.registerAndLogin(mockMvc, objectMapper, "Bob", "b@example.com", "password123");
        TestAuthSupport.TestUser c = TestAuthSupport.registerAndLogin(mockMvc, objectMapper, "Carol", "c@example.com", "password123");

        makeMutualMatch(a, b);
        long chatRoomId = getFirstChatRoomId(a);

        mockMvc.perform(
                        post("/api/chat/send")
                                .contentType(APPLICATION_JSON)
                                .header("Authorization", "Bearer " + c.token())
                                .content(objectMapper.writeValueAsString(Map.of(
                                        "chatRoomId", chatRoomId,
                                        "content", "hello from C"
                                )))
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void should_allow_matched_users_to_send_and_read_messages() throws Exception {
        TestAuthSupport.TestUser a = TestAuthSupport.registerAndLogin(mockMvc, objectMapper, "Alice", "a@example.com", "password123");
        TestAuthSupport.TestUser b = TestAuthSupport.registerAndLogin(mockMvc, objectMapper, "Bob", "b@example.com", "password123");

        makeMutualMatch(a, b);
        long chatRoomId = getFirstChatRoomId(a);

        mockMvc.perform(
                        post("/api/chat/send")
                                .contentType(APPLICATION_JSON)
                                .header("Authorization", "Bearer " + a.token())
                                .content(objectMapper.writeValueAsString(Map.of(
                                        "chatRoomId", chatRoomId,
                                        "content", "Hi B!"
                                )))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Hi B!"));

        mockMvc.perform(
                        get("/api/chat/rooms/" + chatRoomId + "/messages")
                                .header("Authorization", "Bearer " + b.token())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content").value("Hi B!"));
    }

    private void makeMutualMatch(TestAuthSupport.TestUser a, TestAuthSupport.TestUser b) throws Exception {
        mockMvc.perform(
                        post("/api/matches/swipe")
                                .contentType(APPLICATION_JSON)
                                .header("Authorization", "Bearer " + a.token())
                                .content(objectMapper.writeValueAsString(Map.of(
                                        "swipedUserId", b.id(),
                                        "swipeType", "LIKE"
                                )))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.match").value(false));

        mockMvc.perform(
                        post("/api/matches/swipe")
                                .contentType(APPLICATION_JSON)
                                .header("Authorization", "Bearer " + b.token())
                                .content(objectMapper.writeValueAsString(Map.of(
                                        "swipedUserId", a.id(),
                                        "swipeType", "LIKE"
                                )))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.match").value(true));
    }

    private long getFirstChatRoomId(TestAuthSupport.TestUser authenticatedUser) throws Exception {
        MvcResult result = mockMvc.perform(
                        get("/api/chat/rooms")
                                .header("Authorization", "Bearer " + authenticatedUser.token())
                )
                .andExpect(status().isOk())
                .andReturn();

        JsonNode rooms = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(rooms.isArray()).isTrue();
        assertThat(rooms.size()).isGreaterThanOrEqualTo(1);
        return rooms.get(0).get("id").asLong();
    }
}
