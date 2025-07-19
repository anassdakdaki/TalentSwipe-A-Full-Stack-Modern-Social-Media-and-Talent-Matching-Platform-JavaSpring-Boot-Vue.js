<template>
  <div class="chat-page">
    <div class="chat-sidebar">
      <h3>My Chats</h3>
      <div v-if="loadingRooms" class="loading-message">Loading chats...</div>
      <div v-if="chatRooms.length === 0 && !loadingRooms" class="no-chats-message">
        No active chats yet.
      </div>
      <ul v-else class="chat-room-list">
        <li 
          v-for="room in chatRooms"
          :key="room.id"
          :class="{'active': selectedChatRoom && selectedChatRoom.id === room.id}"
          @click="selectChatRoom(room)"
        >
          {{ getOtherParticipantName(room) }}
        </li>
      </ul>
    </div>

    <div class="chat-main">
      <div v-if="!selectedChatRoom" class="chat-placeholder">
        Select a chat to start messaging
      </div>
      <div v-else class="chat-window">
        <div class="chat-header">
          <h4>{{ getOtherParticipantName(selectedChatRoom) }}</h4>
        </div>
        <div class="message-list" ref="messageList">
          <div 
            v-for="message in messages"
            :key="message.id"
            :class="{'my-message': message.sender.id === currentUserId, 'other-message': message.sender.id !== currentUserId}"
          >
            <span class="message-sender">{{ message.sender.name }}:</span>
            <span class="message-content">{{ message.content }}</span>
            <span class="message-timestamp">{{ formatTimestamp(message.timestamp) }}</span>
          </div>
        </div>
        <div class="message-input">
          <input 
            type="text" 
            v-model="newMessageContent"
            @keyup.enter="sendMessage"
            placeholder="Type a message..."
          />
          <button @click="sendMessage">Send</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: 'ChatPage',
  data() {
    return {
      chatRooms: [],
      selectedChatRoom: null,
      messages: [],
      newMessageContent: '',
      loadingRooms: false,
      loadingMessages: false,
      currentUserId: null, 
    };
  },
  created() {
    this.fetchCurrentUserId();
    this.fetchChatRooms();
  },
  watch: {
    selectedChatRoom: 'fetchMessages',
    messages: 'scrollToBottom',
  },
  methods: {
    async fetchCurrentUserId() {
      try {
        const token = localStorage.getItem('token');
        const response = await axios.get('http://localhost:8080/api/auth/me', { // Assuming an endpoint to get current user info
          headers: { Authorization: `Bearer ${token}` }
        });
        this.currentUserId = response.data.id; 
      } catch (error) {
        console.error('Error fetching current user ID:', error);
      }
    },
    async fetchChatRooms() {
      this.loadingRooms = true;
      try {
        const token = localStorage.getItem('token');
        const response = await axios.get('http://localhost:8080/api/chat/rooms', {
          headers: { Authorization: `Bearer ${token}` }
        });
        this.chatRooms = response.data;
        if (this.$route.params.chatRoomId) {
          const roomId = parseInt(this.$route.params.chatRoomId);
          this.selectedChatRoom = this.chatRooms.find(room => room.id === roomId) || null;
        } else if (this.chatRooms.length > 0) {
          this.selectedChatRoom = this.chatRooms[0];
        }
      } catch (error) {
        console.error('Error fetching chat rooms:', error);
      } finally {
        this.loadingRooms = false;
      }
    },
    async fetchMessages() {
      if (!this.selectedChatRoom) return;
      this.loadingMessages = true;
      try {
        const token = localStorage.getItem('token');
        const response = await axios.get(`http://localhost:8080/api/chat/rooms/${this.selectedChatRoom.id}/messages`, {
          headers: { Authorization: `Bearer ${token}` }
        });
        this.messages = response.data;
      } catch (error) {
        console.error('Error fetching messages:', error);
      } finally {
        this.loadingMessages = false;
      }
    },
    async sendMessage() {
      if (!this.newMessageContent.trim() || !this.selectedChatRoom) return;
      try {
        const token = localStorage.getItem('token');
        const messagePayload = {
          chatRoomId: this.selectedChatRoom.id,
          content: this.newMessageContent.trim(),
        };
        const response = await axios.post('http://localhost:8080/api/chat/send', messagePayload, {
          headers: { Authorization: `Bearer ${token}` }
        });
        this.messages.push(response.data); 
        this.newMessageContent = '';
      } catch (error) {
        console.error('Error sending message:', error);
      }
    },
    selectChatRoom(room) {
      this.selectedChatRoom = room;
      this.$router.push({ name: 'Chat', params: { chatRoomId: room.id } });
    },
    getOtherParticipantName(room) {
      if (!this.currentUserId) return 'Loading...';
      return room.user1.id === this.currentUserId ? room.user2.name : room.user1.name;
    },
    formatTimestamp(timestamp) {
      const date = new Date(timestamp);
      return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    },
    scrollToBottom() {
      this.$nextTick(() => {
        const messageList = this.$refs.messageList;
        if (messageList) {
          messageList.scrollTop = messageList.scrollHeight;
        }
      });
    },
  },
};
</script>

<style scoped>
.chat-page {
  display: flex;
  height: 100vh; 
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
  color: #e0e0e0;
  overflow: hidden;
  font-family: 'Poppins', sans-serif;
}

.chat-sidebar {
  width: 300px;
  background: rgba(255, 255, 255, 0.05);
  border-right: 1px solid rgba(255, 255, 255, 0.1);
  padding: 20px;
  overflow-y: auto;
  flex-shrink: 0;
}

.chat-sidebar h3 {
  color: #64ffda;
  margin-bottom: 20px;
  font-size: 1.5rem;
  text-align: center;
}

.loading-message,
.no-chats-message {
  text-align: center;
  color: rgba(224, 224, 224, 0.7);
  margin-top: 50px;
}

.chat-room-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.chat-room-list li {
  padding: 15px;
  margin-bottom: 10px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 10px;
  cursor: pointer;
  transition: background 0.3s ease, transform 0.2s ease;
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.2);
  font-size: 1.1rem;
}

.chat-room-list li:hover {
  background: rgba(255, 255, 255, 0.15);
  transform: translateX(5px);
}

.chat-room-list li.active {
  background: #64ffda; /* Accent color for active chat */
  color: #1a1a2e; /* Dark text for active chat */
  border-color: #64ffda;
  box-shadow: 0 4px 15px rgba(100, 255, 218, 0.4);
}

.chat-main {
  flex-grow: 1;
  display: flex;
  flex-direction: column;
  position: relative; /* For absolute positioning of chat-placeholder */
}

.chat-placeholder {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  font-size: 1.5rem;
  color: rgba(224, 224, 224, 0.6);
  text-align: center;
}

.chat-window {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: rgba(255, 255, 255, 0.08);
  border-radius: 10px; 
  overflow: hidden; /* Hide scrollbar for the main chat window */
}

.chat-header {
  padding: 15px 20px;
  background: rgba(255, 255, 255, 0.1);
  border-bottom: 1px solid rgba(255, 255, 255, 0.15);
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.2);
  text-align: center;
}

.chat-header h4 {
  margin: 0;
  color: #64ffda;
  font-size: 1.3rem;
}

.message-list {
  flex-grow: 1;
  padding: 20px;
  overflow-y: auto; /* Allow scrolling for messages */
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.my-message,
.other-message {
  display: flex;
  flex-direction: column;
  max-width: 70%;
  padding: 10px 15px;
  border-radius: 15px;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
  word-wrap: break-word; /* Ensure long words break */
}

.my-message {
  align-self: flex-end;
  background: linear-gradient(45deg, #64ffda, #3be8b0);
  color: #1a1a2e; 
  border-bottom-right-radius: 2px; /* Pointy corner for self */
}

.other-message {
  align-self: flex-start;
  background: rgba(255, 255, 255, 0.15);
  color: #e0e0e0;
  border-bottom-left-radius: 2px; /* Pointy corner for others */
}

.message-sender {
  font-size: 0.85rem;
  opacity: 0.8;
  margin-bottom: 3px;
}

.my-message .message-sender {
  color: #1a1a2e; /* Darker sender name for own messages */
}

.other-message .message-sender {
  color: #64ffda; /* Accent color for other sender name */
}

.message-content {
  font-size: 1rem;
  margin-bottom: 5px;
}

.message-timestamp {
  font-size: 0.75rem;
  color: rgba(0, 0, 0, 0.6); /* Darker for own messages */
  align-self: flex-end;
}

.other-message .message-timestamp {
  color: rgba(224, 224, 224, 0.6); /* Lighter for other messages */
  align-self: flex-start;
}

.message-input {
  display: flex;
  padding: 15px 20px;
  background: rgba(255, 255, 255, 0.05);
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.message-input input {
  flex-grow: 1;
  padding: 10px 15px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 25px;
  background: rgba(255, 255, 255, 0.1);
  color: #e0e0e0;
  font-size: 1rem;
  outline: none;
  margin-right: 10px;
  transition: border-color 0.3s ease;
}

.message-input input:focus {
  border-color: #64ffda;
}

.message-input button {
  background: #64ffda;
  color: #1a1a2e;
  padding: 10px 20px;
  border: none;
  border-radius: 25px;
  cursor: pointer;
  font-size: 1rem;
  transition: background-color 0.3s ease;
}

.message-input button:hover {
  background: #3be8b0;
}

/* Responsive Adjustments */
@media (max-width: 768px) {
  .chat-page {
    flex-direction: column;
    height: auto; /* Allow height to adjust */
    min-height: 100vh;
  }

  .chat-sidebar {
    width: 100%;
    border-right: none;
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);
    height: 200px; /* Limit height of sidebar on small screens */
  }

  .chat-main {
    height: calc(100vh - 200px); /* Fill remaining height */
  }

  .chat-room-list li {
    font-size: 1rem;
    padding: 12px;
  }

  .message-input {
    padding: 10px 15px;
  }

  .message-input input {
    padding: 8px 12px;
    font-size: 0.9rem;
  }

  .message-input button {
    padding: 8px 15px;
    font-size: 0.9rem;
  }

  .my-message,
  .other-message {
    max-width: 85%;
    padding: 8px 12px;
  }

  .chat-header h4 {
    font-size: 1.1rem;
  }
}

</style> 