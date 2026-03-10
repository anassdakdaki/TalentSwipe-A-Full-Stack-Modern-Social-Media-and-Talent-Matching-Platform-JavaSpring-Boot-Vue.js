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
const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080').replace(/\/+$/, '');

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
        const response = await axios.get(`${API_BASE_URL}/api/auth/me`, {
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
        const response = await axios.get(`${API_BASE_URL}/api/chat/rooms`, {
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
        const response = await axios.get(`${API_BASE_URL}/api/chat/rooms/${this.selectedChatRoom.id}/messages`, {
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
        const response = await axios.post(`${API_BASE_URL}/api/chat/send`, messagePayload, {
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
  display: grid;
  grid-template-columns: 300px minmax(0, 1fr);
  height: calc(100vh - 72px);
  min-height: 520px;
  background: var(--theme-page-background);
  color: var(--theme-text-primary);
  overflow: hidden;
  border-top: 1px solid var(--theme-divider);
}

.chat-sidebar {
  background: var(--theme-surface-elevated);
  border-right: 1px solid var(--theme-divider);
  padding: 18px 14px;
  overflow-y: auto;
  flex-shrink: 0;
}

.chat-sidebar h3 {
  color: var(--theme-heading-color);
  margin: 2px 0 14px;
  font-size: 1.95rem;
  line-height: 1.1;
  font-family: var(--theme-font-heading);
}

.loading-message,
.no-chats-message {
  color: var(--theme-text-secondary);
  margin-top: 28px;
  font-size: 0.96rem;
}

.chat-room-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.chat-room-list li {
  padding: 12px;
  margin-bottom: 8px;
  background: var(--theme-surface-1);
  border-radius: 10px;
  cursor: pointer;
  transition: background 0.2s ease, transform 0.2s ease, border-color 0.2s ease;
  border: 1px solid var(--theme-surface-border);
  box-shadow: var(--theme-shadow-soft);
  font-size: 1rem;
  color: var(--theme-text-primary);
  font-weight: 600;
  line-height: 1.35;
}

.chat-room-list li:hover {
  background: color-mix(in srgb, var(--theme-accent) 10%, var(--theme-surface-1));
  border-color: color-mix(in srgb, var(--theme-accent) 50%, var(--theme-surface-border));
  transform: translateX(2px);
}

.chat-room-list li.active {
  background: color-mix(in srgb, var(--theme-accent) 20%, var(--theme-surface-1));
  color: var(--theme-text-primary);
  border-color: color-mix(in srgb, var(--theme-accent) 60%, var(--theme-surface-border));
  box-shadow: 0 8px 20px color-mix(in srgb, var(--theme-accent) 22%, transparent);
}

.chat-main {
  display: flex;
  flex-direction: column;
  min-width: 0;
  background: transparent;
}

.chat-placeholder {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  font-size: clamp(1.5rem, 2.8vw, 2.7rem);
  color: var(--theme-text-subtle);
  text-align: center;
  font-family: var(--theme-font-heading);
}

.chat-window {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: var(--theme-surface-elevated);
  overflow: hidden;
}

.chat-header {
  padding: 14px 18px;
  background: var(--theme-surface-1);
  border-bottom: 1px solid var(--theme-divider);
}

.chat-header h4 {
  margin: 0;
  color: var(--theme-heading-color);
  font-size: 1.14rem;
  font-family: var(--theme-font-heading);
}

.message-list {
  flex-grow: 1;
  padding: 16px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 10px;
  background: color-mix(in srgb, var(--theme-page-background) 72%, var(--theme-surface-elevated));
}

.my-message,
.other-message {
  display: flex;
  flex-direction: column;
  max-width: min(72%, 680px);
  padding: 10px 13px;
  border-radius: 14px;
  box-shadow: var(--theme-shadow-soft);
  word-wrap: break-word;
  border: 1px solid var(--theme-surface-border);
}

.my-message {
  align-self: flex-end;
  background: var(--theme-button-primary-bg);
  color: var(--theme-button-primary-text);
  border-bottom-right-radius: 4px;
  border-color: color-mix(in srgb, var(--theme-accent) 55%, transparent);
}

.other-message {
  align-self: flex-start;
  background: var(--theme-surface-1);
  color: var(--theme-text-primary);
  border-bottom-left-radius: 4px;
}

.message-sender {
  font-size: 0.8rem;
  margin-bottom: 4px;
  font-weight: 700;
}

.my-message .message-sender {
  color: color-mix(in srgb, var(--theme-button-primary-text) 78%, var(--theme-text-primary));
}

.other-message .message-sender {
  color: var(--theme-accent);
}

.message-content {
  font-size: 0.95rem;
  margin-bottom: 6px;
  line-height: 1.45;
}

.message-timestamp {
  font-size: 0.75rem;
  color: color-mix(in srgb, var(--theme-button-primary-text) 72%, transparent);
  align-self: flex-end;
}

.other-message .message-timestamp {
  color: var(--theme-text-subtle);
  align-self: flex-start;
}

.message-input {
  display: flex;
  padding: 12px 14px;
  background: var(--theme-surface-elevated);
  border-top: 1px solid var(--theme-divider);
  gap: 8px;
}

.message-input input {
  flex: 1;
  min-width: 0;
  padding: 10px 12px;
  border: 1px solid var(--theme-input-border);
  border-radius: 11px;
  background: var(--theme-input-bg);
  color: var(--theme-input-text);
  font-size: 0.95rem;
  outline: none;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.message-input input::placeholder {
  color: var(--theme-input-placeholder);
}

.message-input input:focus {
  border-color: var(--theme-accent);
  box-shadow: 0 0 0 3px color-mix(in srgb, var(--theme-accent) 18%, transparent);
}

.message-input button {
  background: var(--theme-button-primary-bg);
  color: var(--theme-button-primary-text);
  padding: 0 18px;
  min-height: 41px;
  border: none;
  border-radius: 11px;
  cursor: pointer;
  font-size: 0.92rem;
  font-weight: 700;
  box-shadow: var(--theme-button-primary-shadow);
  transition: transform 0.2s ease, filter 0.2s ease;
}

.message-input button:hover {
  transform: translateY(-1px);
  filter: brightness(1.03);
}

@media (max-width: 1100px) {
  .chat-page {
    grid-template-columns: 260px minmax(0, 1fr);
  }
}

@media (max-width: 768px) {
  .chat-page {
    grid-template-columns: 1fr;
    height: auto;
    min-height: calc(100vh - 84px);
  }

  .chat-sidebar {
    width: 100%;
    border-right: none;
    border-bottom: 1px solid var(--theme-divider);
    max-height: 240px;
    padding: 12px 10px;
  }

  .chat-main {
    min-height: calc(100vh - 320px);
  }

  .chat-room-list li {
    padding: 12px;
  }

  .message-input {
    padding: 10px;
  }

  .message-input input {
    padding: 9px 10px;
  }

  .message-input button {
    min-height: 39px;
    padding: 0 14px;
  }

  .my-message,
  .other-message {
    max-width: 88%;
    padding: 8px 12px;
  }
}

</style> 
