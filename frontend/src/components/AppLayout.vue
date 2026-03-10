<template>
  <div class="app-layout">
    <DesktopSidebar
      v-if="!isMobile"
      :collapsed="isTablet"
      :current-path="$route.path"
      :chat-unread-count="chatUnreadCount"
    />

    <div class="layout-main">
      <TopNavBar
        v-if="!isMobile"
        :unread-notifications="unreadCount"
        :user-name="currentUserName"
        :user-avatar-url="currentUserAvatar"
        :show-search="topNavConfig.showSearch"
        :search-placeholder="topNavConfig.searchPlaceholder"
        :page-label="topNavConfig.pageLabel"
        @search="handleSearch"
        @toggle-notifications="toggleNotifications"
        @logout="logout"
      />

      <div class="view-region">
        <router-view />
      </div>
    </div>

    <transition name="instant-popup">
      <button
        v-if="instantPopup.visible"
        class="instant-popup"
        @click="openInstantPopupTarget"
      >
        <span class="instant-popup-icon">
          <i :class="['fas', getNotificationIcon(instantPopup.type)]"></i>
        </span>
        <span class="instant-popup-copy">
          <strong>{{ instantPopup.title }}</strong>
          <small>{{ instantPopup.message }}</small>
        </span>
      </button>
    </transition>

    <div class="notification-layer">
      <transition name="notification-panel">
        <div v-if="isNotificationsOpen" ref="notificationPanel" class="notification-panel">
          <div class="notification-panel-header">
            <h4>Notifications</h4>
            <button class="mark-read-button" @click="markNotificationsSeen">Mark all read</button>
          </div>
          <p v-if="notificationsLoading" class="notification-status">Loading...</p>
          <p v-else-if="notifications.length === 0" class="notification-status">No notifications yet.</p>
          <div v-else class="notification-list">
            <button
              v-for="notification in notifications"
              :key="notification.id"
              class="notification-item"
              @click="openNotification(notification)"
            >
              <div class="notification-icon">
                <i :class="['fas', getNotificationIcon(notification.type)]"></i>
              </div>
              <div class="notification-copy">
                <p class="notification-title">{{ notification.title }}</p>
                <p class="notification-message">{{ notification.message }}</p>
                <p class="notification-time">{{ formatNotificationTime(notification.createdAt) }}</p>
              </div>
            </button>
          </div>
        </div>
      </transition>
    </div>

    <div v-if="isMobile" class="mobile-controls">
      <button class="notification-bell" @click="toggleNotifications" aria-label="Notifications">
        <i class="fas fa-bell"></i>
        <span v-if="unreadCount > 0" class="notification-badge">
          {{ formatBadgeCount(unreadCount) }}
        </span>
      </button>

      <div class="fab-container">
        <button class="fab" @click="toggleFabMenu" aria-label="Navigation menu">
          <i :class="['fas', isFabMenuOpen ? 'fa-times' : 'fa-bars']"></i>
        </button>

        <transition name="fab-menu">
          <nav v-if="isFabMenuOpen" class="fab-menu" aria-label="Mobile navigation">
            <router-link to="/authenticated/feed" class="menu-item" @click="toggleFabMenu">
              <i class="fas fa-stream"></i>
              <span>Feed</span>
            </router-link>
            <router-link to="/authenticated/communities" class="menu-item" @click="toggleFabMenu">
              <i class="fas fa-users"></i>
              <span>Communities</span>
            </router-link>
            <router-link to="/authenticated/matches" class="menu-item" @click="toggleFabMenu">
              <i class="fas fa-user-plus"></i>
              <span>Connect</span>
            </router-link>
            <router-link to="/authenticated/chat" class="menu-item" @click="toggleFabMenu">
              <i class="fas fa-comments"></i>
              <span>Chat</span>
              <span v-if="chatUnreadCount > 0" class="menu-badge">{{ formatBadgeCount(chatUnreadCount) }}</span>
            </router-link>
            <router-link to="/authenticated/profile" class="menu-item" @click="toggleFabMenu">
              <i class="fas fa-user"></i>
              <span>Profile</span>
            </router-link>
            <router-link to="/authenticated/settings" class="menu-item" @click="toggleFabMenu">
              <i class="fas fa-cog"></i>
              <span>Settings</span>
            </router-link>
            <button class="menu-item menu-item-button" @click="logout">
              <i class="fas fa-sign-out-alt"></i>
              <span>Logout</span>
            </button>
          </nav>
        </transition>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';
import DesktopSidebar from './navigation/DesktopSidebar.vue';
import TopNavBar from './navigation/TopNavBar.vue';

const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080').replace(/\/+$/, '');

export default {
  name: 'AppLayout',
  components: {
    DesktopSidebar,
    TopNavBar,
  },
  data() {
    return {
      viewportWidth: typeof window !== 'undefined' ? window.innerWidth : 1280,
      isFabMenuOpen: false,
      isNotificationsOpen: false,
      notifications: [],
      unreadCount: 0,
      chatUnreadCount: 0,
      notificationsLoading: false,
      notificationPollHandle: null,
      instantPopupTimer: null,
      hasLoadedNotificationSnapshot: false,
      knownNotificationIds: [],
      instantPopup: {
        visible: false,
        title: '',
        message: '',
        type: 'MESSAGE',
        route: '',
      },
      lastSeenAt: localStorage.getItem('biblo.notifications.lastSeenAt') || new Date(0).toISOString(),
      chatLastSeenAt: localStorage.getItem('biblo.notifications.chatLastSeenAt') || new Date(0).toISOString(),
      currentUserName: '',
      currentUserAvatar: '',
    };
  },
  computed: {
    isMobile() {
      return this.viewportWidth < 768;
    },
    isTablet() {
      return this.viewportWidth >= 768 && this.viewportWidth < 1200;
    },
    topNavConfig() {
      const routeName = this.$route?.name;
      switch (routeName) {
        case 'Feed':
          return {
            showSearch: true,
            searchPlaceholder: 'Search communities',
            pageLabel: 'Feed',
            searchTargetName: 'Communities',
          };
        case 'Communities':
          return {
            showSearch: true,
            searchPlaceholder: 'Search communities',
            pageLabel: 'Communities',
            searchTargetName: 'Communities',
          };
        case 'CommunityDetail':
          return {
            showSearch: true,
            searchPlaceholder: 'Search communities',
            pageLabel: 'Community',
            searchTargetName: 'Communities',
          };
        case 'Matches':
          return {
            showSearch: false,
            searchPlaceholder: '',
            pageLabel: 'Connect',
            searchTargetName: null,
          };
        case 'Chat':
          return {
            showSearch: false,
            searchPlaceholder: '',
            pageLabel: 'Chat',
            searchTargetName: null,
          };
        case 'Settings':
          return {
            showSearch: false,
            searchPlaceholder: '',
            pageLabel: 'Settings',
            searchTargetName: null,
          };
        case 'Profile':
        case 'PublicProfile':
          return {
            showSearch: false,
            searchPlaceholder: '',
            pageLabel: 'Profile',
            searchTargetName: null,
          };
        default:
          return {
            showSearch: true,
            searchPlaceholder: 'Search communities',
            pageLabel: 'Workspace',
            searchTargetName: 'Communities',
          };
      }
    },
  },
  created() {
    if (this.$route?.name === 'Chat') {
      this.markChatSeen();
    }
    this.fetchCurrentUser();
    this.fetchNotifications();
    this.startNotificationPolling();
  },
  mounted() {
    window.addEventListener('resize', this.handleResize);
    document.addEventListener('pointerdown', this.handleGlobalPointerDown);
  },
  beforeUnmount() {
    window.removeEventListener('resize', this.handleResize);
    document.removeEventListener('pointerdown', this.handleGlobalPointerDown);
    this.stopNotificationPolling();
    this.clearInstantPopupTimer();
  },
  watch: {
    '$route.fullPath'() {
      this.isFabMenuOpen = false;
      if (this.$route?.name === 'Chat') {
        this.markChatSeen();
      }
      if (this.notificationPollHandle == null) {
        this.fetchNotifications();
        this.startNotificationPolling();
      }
    },
  },
  methods: {
    handleResize() {
      this.viewportWidth = window.innerWidth;
      if (!this.isMobile) {
        this.isFabMenuOpen = false;
      }
    },
    normalizeMediaUrl(url) {
      if (!url) {
        return '';
      }
      if (url.startsWith('http://') || url.startsWith('https://')) {
        return url;
      }
      return `${API_BASE_URL}${url.startsWith('/') ? url : `/${url}`}`;
    },
    async fetchCurrentUser() {
      try {
        const token = localStorage.getItem('token');
        const response = await axios.get(`${API_BASE_URL}/api/auth/me`, {
          headers: { Authorization: `Bearer ${token}` },
        });
        this.currentUserName = response.data?.username || 'Account';
        this.currentUserAvatar = this.normalizeMediaUrl(response.data?.profilePictureUrl || '');
      } catch (error) {
        this.currentUserName = 'Account';
        this.currentUserAvatar = '';
      }
    },
    handleSearch(query) {
      if (!this.topNavConfig.showSearch) {
        return;
      }
      const value = (query || '').trim();
      if (!value) {
        return;
      }
      const targetRouteName = this.topNavConfig.searchTargetName;
      if (!targetRouteName) {
        return;
      }
      this.$router.push({
        name: targetRouteName,
        query: { q: value },
      });
    },
    toggleFabMenu() {
      this.isFabMenuOpen = !this.isFabMenuOpen;
    },
    toggleNotifications() {
      this.isNotificationsOpen = !this.isNotificationsOpen;
      if (this.isNotificationsOpen) {
        this.markNotificationsSeen();
        this.fetchNotifications();
      }
    },
    handleGlobalPointerDown(event) {
      if (!this.isNotificationsOpen) {
        return;
      }
      const panel = this.$refs.notificationPanel;
      if (panel && panel.contains(event.target)) {
        return;
      }
      this.isNotificationsOpen = false;
    },
    async fetchNotifications() {
      this.notificationsLoading = true;
      try {
        const token = localStorage.getItem('token');
        const response = await axios.get(`${API_BASE_URL}/api/notifications`, {
          headers: { Authorization: `Bearer ${token}` },
          params: {
            limit: 20,
            since: this.lastSeenAt,
          },
        });

        const fetchedNotifications = response.data.notifications || [];
        if (this.hasLoadedNotificationSnapshot) {
          const newestNotification = fetchedNotifications.find(
            (item) => item && item.id && !this.knownNotificationIds.includes(item.id)
          );
          if (newestNotification) {
            this.showInstantPopup(newestNotification);
          }
        }

        this.notifications = fetchedNotifications;
        this.applyUnreadCounters(fetchedNotifications);
        this.knownNotificationIds = fetchedNotifications
          .map((item) => item?.id)
          .filter((id) => (typeof id === 'string' || typeof id === 'number'));
        this.hasLoadedNotificationSnapshot = true;
      } catch (error) {
        console.error('Failed to fetch notifications:', error);
      } finally {
        this.notificationsLoading = false;
      }
    },
    markNotificationsSeen() {
      const nowIso = new Date().toISOString();
      this.lastSeenAt = nowIso;
      this.chatLastSeenAt = nowIso;
      localStorage.setItem('biblo.notifications.lastSeenAt', this.lastSeenAt);
      localStorage.setItem('biblo.notifications.chatLastSeenAt', this.chatLastSeenAt);
      this.unreadCount = 0;
      this.chatUnreadCount = 0;
    },
    markChatSeen() {
      this.chatLastSeenAt = new Date().toISOString();
      localStorage.setItem('biblo.notifications.chatLastSeenAt', this.chatLastSeenAt);
      this.chatUnreadCount = 0;

      // If current unread is only from message notifications, clear the top badge too.
      this.applyUnreadCounters(this.notifications);
    },
    parseIsoOrEpoch(value) {
      const parsed = Date.parse(value || '');
      return Number.isNaN(parsed) ? 0 : parsed;
    },
    isNotificationUnread(notification, isMessage) {
      const createdAtMs = this.parseIsoOrEpoch(notification?.createdAt);
      if (!createdAtMs) {
        return false;
      }
      const baseMs = isMessage
        ? Math.max(this.parseIsoOrEpoch(this.lastSeenAt), this.parseIsoOrEpoch(this.chatLastSeenAt))
        : this.parseIsoOrEpoch(this.lastSeenAt);
      return createdAtMs > baseMs;
    },
    applyUnreadCounters(notifications) {
      const items = Array.isArray(notifications) ? notifications : [];
      let unread = 0;
      let chatUnread = 0;

      items.forEach((notification) => {
        const type = notification?.type || '';
        const isMessage = type === 'MESSAGE';
        if (this.isNotificationUnread(notification, isMessage)) {
          unread += 1;
          if (isMessage) {
            chatUnread += 1;
          }
        }
      });

      this.unreadCount = unread;
      this.chatUnreadCount = this.$route?.name === 'Chat' ? 0 : chatUnread;
    },
    startNotificationPolling() {
      this.stopNotificationPolling();
      this.notificationPollHandle = setInterval(() => {
        this.fetchNotifications();
      }, 15000);
    },
    stopNotificationPolling() {
      if (this.notificationPollHandle) {
        clearInterval(this.notificationPollHandle);
        this.notificationPollHandle = null;
      }
    },
    openNotification(notification) {
      if (notification?.route) {
        this.$router.push(notification.route);
      }
      this.isNotificationsOpen = false;
      this.markNotificationsSeen();
    },
    showInstantPopup(notification) {
      if (!notification || this.isNotificationsOpen) {
        return;
      }
      this.instantPopup.title = notification.title || 'New notification';
      this.instantPopup.message = notification.message || '';
      this.instantPopup.type = notification.type || 'MESSAGE';
      this.instantPopup.route = notification.route || '';
      this.instantPopup.visible = true;
      this.clearInstantPopupTimer();
      this.instantPopupTimer = setTimeout(() => {
        this.instantPopup.visible = false;
        this.instantPopupTimer = null;
      }, 1000);
    },
    openInstantPopupTarget() {
      if (this.instantPopup.route) {
        this.$router.push(this.instantPopup.route);
      }
      this.instantPopup.visible = false;
      this.clearInstantPopupTimer();
    },
    clearInstantPopupTimer() {
      if (this.instantPopupTimer) {
        clearTimeout(this.instantPopupTimer);
        this.instantPopupTimer = null;
      }
    },
    getNotificationIcon(type) {
      switch (type) {
        case 'MESSAGE':
          return 'fa-comment-dots';
        case 'LIKE':
          return 'fa-heart';
        case 'COMMENT':
          return 'fa-reply';
        case 'MATCH':
          return 'fa-user-check';
        default:
          return 'fa-bell';
      }
    },
    formatNotificationTime(timestamp) {
      if (!timestamp) {
        return '';
      }
      return new Date(timestamp).toLocaleString();
    },
    formatBadgeCount(count) {
      return count > 99 ? '99+' : String(count);
    },
    logout() {
      localStorage.removeItem('token');
      this.$router.push({ name: 'Login' });
    },
  },
};
</script>

<style scoped>
.app-layout {
  min-height: 100vh;
  display: flex;
  background: var(--theme-app-background);
  color: var(--theme-text-primary);
}

.layout-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.view-region {
  flex: 1;
  min-height: 0;
}

.notification-layer {
  position: fixed;
  top: 76px;
  right: 20px;
  z-index: 1100;
}

.notification-panel {
  width: min(92vw, 390px);
  max-height: min(72vh, 530px);
  border-radius: 14px;
  border: 1px solid var(--theme-surface-border);
  background: var(--theme-surface-elevated);
  color: var(--theme-text-primary);
  box-shadow: var(--theme-shadow-strong);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.notification-panel-header {
  padding: 12px 14px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  border-bottom: 1px solid var(--theme-divider);
}

.notification-panel-header h4 {
  margin: 0;
  font-size: 0.95rem;
  color: var(--theme-heading-color);
}

.mark-read-button {
  border-radius: 999px;
  padding: 6px 10px;
  border: 1px solid var(--theme-button-secondary-border);
  background: var(--theme-button-secondary-bg);
  color: var(--theme-button-secondary-text);
  font-size: 0.76rem;
  font-weight: 600;
  cursor: pointer;
}

.notification-status {
  padding: 16px;
  font-size: 0.9rem;
  color: var(--theme-text-secondary);
}

.notification-list {
  overflow-y: auto;
}

.notification-item {
  width: 100%;
  border: none;
  border-bottom: 1px solid var(--theme-divider);
  background: transparent;
  color: inherit;
  text-align: left;
  padding: 12px 14px;
  display: flex;
  align-items: flex-start;
  gap: 10px;
  cursor: pointer;
}

.notification-item:hover {
  background: var(--theme-menu-item-hover);
}

.notification-icon {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: var(--theme-chip-bg);
  color: var(--theme-chip-text);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.notification-copy {
  min-width: 0;
}

.notification-title {
  margin: 0;
  font-size: 0.88rem;
  font-weight: 700;
}

.notification-message {
  margin-top: 2px;
  font-size: 0.83rem;
  color: var(--theme-text-secondary);
}

.notification-time {
  margin-top: 6px;
  font-size: 0.74rem;
  color: var(--theme-text-subtle);
}

.notification-panel-enter-active,
.notification-panel-leave-active {
  transition: all 0.2s ease;
}

.notification-panel-enter-from,
.notification-panel-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

.instant-popup {
  position: fixed;
  top: 16px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 1300;
  border: 1px solid var(--theme-surface-border);
  background: var(--theme-surface-elevated);
  color: var(--theme-text-primary);
  border-radius: 12px;
  box-shadow: var(--theme-shadow-strong);
  min-width: min(92vw, 420px);
  max-width: min(92vw, 420px);
  padding: 10px 12px;
  display: flex;
  gap: 10px;
  align-items: flex-start;
  cursor: pointer;
}

.instant-popup-icon {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: var(--theme-chip-bg);
  color: var(--theme-chip-text);
  flex-shrink: 0;
}

.instant-popup-copy {
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.instant-popup-copy strong {
  font-size: 0.86rem;
}

.instant-popup-copy small {
  font-size: 0.78rem;
  color: var(--theme-text-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.instant-popup-enter-active,
.instant-popup-leave-active {
  transition: opacity 0.15s ease, transform 0.15s ease;
}

.instant-popup-enter-from,
.instant-popup-leave-to {
  opacity: 0;
  transform: translate(-50%, -8px);
}

.mobile-controls {
  position: fixed;
  right: 14px;
  bottom: 14px;
  z-index: 1100;
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-items: flex-end;
}

.notification-bell {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  border: 1px solid var(--theme-fab-border);
  background: var(--theme-fab-surface);
  color: var(--theme-fab-icon);
  box-shadow: var(--theme-fab-shadow);
  font-size: 1rem;
  cursor: pointer;
  position: relative;
}

.notification-badge {
  position: absolute;
  top: -4px;
  right: -4px;
  min-width: 19px;
  height: 19px;
  padding: 0 5px;
  border-radius: 999px;
  background: var(--theme-danger);
  color: #fff;
  font-size: 0.68rem;
  font-weight: 700;
  line-height: 19px;
}

.fab-container {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.fab {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  border: 1px solid var(--theme-fab-border);
  background: var(--theme-fab-surface);
  color: var(--theme-fab-icon);
  box-shadow: var(--theme-fab-shadow);
  font-size: 1.35rem;
  cursor: pointer;
}

.fab-menu {
  min-width: 182px;
  margin-bottom: 10px;
  border-radius: 14px;
  border: 1px solid var(--theme-surface-border);
  background: var(--theme-menu-surface);
  box-shadow: var(--theme-shadow-soft);
  padding: 8px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.menu-item {
  position: relative;
  width: 100%;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 9px 10px;
  border-radius: 10px;
  text-decoration: none;
  color: var(--theme-text-primary);
  font-size: 0.94rem;
  font-weight: 600;
}

.menu-item-button {
  border: none;
  background: transparent;
  cursor: pointer;
  text-align: left;
}

.menu-item:hover {
  background: var(--theme-menu-item-hover);
}

.menu-item i {
  color: var(--theme-accent);
}

.menu-badge {
  margin-left: auto;
  min-width: 20px;
  height: 20px;
  border-radius: 999px;
  background: var(--theme-danger);
  color: #fff;
  font-size: 0.7rem;
  font-weight: 700;
  line-height: 20px;
  text-align: center;
  padding: 0 6px;
}

.fab-menu-enter-active,
.fab-menu-leave-active {
  transition: all 0.2s ease;
}

.fab-menu-enter-from,
.fab-menu-leave-to {
  opacity: 0;
  transform: translateY(10px) scale(0.97);
}

@media (min-width: 768px) {
  .notification-layer {
    top: 76px;
    right: 20px;
  }
}

@media (max-width: 767px) {
  .notification-layer {
    top: 12px;
    right: 12px;
  }
}
</style>
