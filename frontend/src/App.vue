<template>
  <div id="app">
    <router-view></router-view>

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

    <div v-if="isAuthenticated" class="notification-container">
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

    <div v-if="isAuthenticated" class="fab-container">
      <button class="fab" @click="toggleFabMenu">
        <i :class="['fas', isFabMenuOpen ? 'fa-times animate-spin-once' : 'fa-bars']"></i>
      </button>

      <transition name="fab-menu">
        <div v-if="isFabMenuOpen" class="fab-menu">
          <router-link to="/authenticated/feed" class="menu-item" @click="toggleFabMenu">
            <i class="fas fa-stream"></i>
            <span>Feed</span>
          </router-link>
          <router-link to="/authenticated/profile" class="menu-item" @click="toggleFabMenu">
            <i class="fas fa-user"></i>
            <span>Profile</span>
          </router-link>
          <router-link to="/authenticated/communities" class="menu-item" @click="toggleFabMenu">
            <i class="fas fa-users"></i>
            <span>Communities</span>
          </router-link>
          <router-link to="/authenticated/matches" class="menu-item" @click="toggleFabMenu">
             <i class="fas fa-user-plus"></i>
            <span>Connet</span>
          </router-link>
          <router-link to="/authenticated/chat" class="menu-item" @click="toggleFabMenu">
             <i class="fas fa-comments"></i>
            <span>Chat</span>
          </router-link>
          <button ref="notificationsMenuButton" class="menu-item menu-item-button" @click="openNotificationsFromMenu">
            <i class="fas fa-bell"></i>
            <span>Notifications</span>
          </button>
          <router-link to="/authenticated/settings" class="menu-item" @click="toggleFabMenu">
            <i class="fas fa-cog"></i>
            <span>Settings</span>
          </router-link>
        </div>
      </transition>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: 'App',
  data() {
    return {
      isFabMenuOpen: false,
      isNotificationsOpen: false,
      notifications: [],
      unreadCount: 0,
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
    };
  },
  computed: {
    isAuthenticated() {
      return !!localStorage.getItem('token');
    },
  },
  created() {
    if (this.isAuthenticated) {
      this.fetchNotifications();
      this.startNotificationPolling();
    }
  },
  mounted() {
    document.addEventListener('pointerdown', this.handleGlobalPointerDown);
  },
  beforeUnmount() {
    document.removeEventListener('pointerdown', this.handleGlobalPointerDown);
    this.stopNotificationPolling();
    this.clearInstantPopupTimer();
  },
  watch: {
    '$route.fullPath'() {
      if (!this.isAuthenticated) {
        this.notifications = [];
        this.unreadCount = 0;
        this.knownNotificationIds = [];
        this.hasLoadedNotificationSnapshot = false;
        this.isFabMenuOpen = false;
        this.isNotificationsOpen = false;
        this.instantPopup.visible = false;
        this.clearInstantPopupTimer();
        this.stopNotificationPolling();
        return;
      }

      if (!this.notificationPollHandle) {
        this.fetchNotifications();
        this.startNotificationPolling();
      }
    },
  },
  methods: {
    toggleFabMenu() {
      this.isFabMenuOpen = !this.isFabMenuOpen;
    },
    toggleNotifications() {
      this.isNotificationsOpen = !this.isNotificationsOpen;
      if (this.isNotificationsOpen) {
        this.markNotificationsSeen();
      }
    },
    handleGlobalPointerDown(event) {
      if (!this.isNotificationsOpen) {
        return;
      }

      const panel = this.$refs.notificationPanel;
      const menuButton = this.$refs.notificationsMenuButton;
      const target = event.target;

      if (panel && panel.contains(target)) {
        return;
      }
      if (menuButton && menuButton.contains(target)) {
        return;
      }

      this.isNotificationsOpen = false;
    },
    openNotificationsFromMenu() {
      this.isFabMenuOpen = false;
      this.isNotificationsOpen = true;
      this.markNotificationsSeen();
      this.fetchNotifications();
    },
    async fetchNotifications() {
      if (!this.isAuthenticated) {
        return;
      }

      this.notificationsLoading = true;
      try {
        const token = localStorage.getItem('token');
        const response = await axios.get('http://localhost:8080/api/notifications', {
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
        this.unreadCount = response.data.unreadCount || 0;
        this.knownNotificationIds = fetchedNotifications
          .map((item) => item?.id)
          .filter((id) => typeof id === 'string' && id.length > 0);
        this.hasLoadedNotificationSnapshot = true;
      } catch (error) {
        console.error('Failed to fetch notifications:', error);
      } finally {
        this.notificationsLoading = false;
      }
    },
    markNotificationsSeen() {
      this.lastSeenAt = new Date().toISOString();
      localStorage.setItem('biblo.notifications.lastSeenAt', this.lastSeenAt);
      this.unreadCount = 0;
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
  },
}
</script>

<style>
i.fas::before {
  font-family: 'Font Awesome 5 Free';
  font-weight: 900 !important;
}

i.fab::before {
  font-family: 'Font Awesome 5 Brands';
  font-weight: 900 !important;
}

#app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  width: 100%;
  position: relative;
}

#app > div:not(.fab-container):not(.notification-container) {
  flex-grow: 1;
}

.fab-container {
  position: fixed;
  right: 20px;
  bottom: 20px;
  z-index: 1000;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.fab {
  width: 68px;
  height: 68px;
  border-radius: 50%;
  border: 1px solid var(--theme-fab-border);
  background: var(--theme-fab-surface);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  color: var(--theme-fab-icon);
  box-shadow: var(--theme-fab-shadow);
  font-size: 1.7rem;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin-top: 10px;
  transition: transform 0.2s ease, background 0.2s ease, box-shadow 0.2s ease;
}

.fab:hover {
  background: var(--theme-fab-surface-hover);
  transform: translateY(-1px);
}

.fab:active {
  transform: scale(0.97);
}

.fab i {
  color: var(--theme-fab-icon);
}

.fab-menu {
  min-width: 196px;
  padding: 12px;
  border-radius: 14px;
  border: 1px solid var(--theme-surface-border);
  background: var(--theme-menu-surface);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  box-shadow: var(--theme-shadow-soft);
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-bottom: 12px;
}

.menu-item {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 10px;
  text-decoration: none;
  color: var(--theme-text-primary);
  font-size: 1rem;
  font-weight: 600;
  transition: background-color 0.2s ease, color 0.2s ease;
}

.menu-item-button {
  border: none;
  background: transparent;
  cursor: pointer;
  text-align: left;
}

.menu-item:hover {
  background: var(--theme-menu-item-hover);
  color: var(--theme-text-primary);
}

.menu-item i {
  color: var(--theme-accent);
}

.fab-menu-enter-active,
.fab-menu-leave-active {
  transition: all 0.22s ease;
}

.fab-menu-enter-from,
.fab-menu-leave-to {
  opacity: 0;
  transform: translateY(10px) scale(0.97);
}

.notification-container {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 1100;
}

.notification-bell {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  border: 1px solid var(--theme-fab-border);
  background: var(--theme-fab-surface);
  color: var(--theme-fab-icon);
  box-shadow: var(--theme-fab-shadow);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  font-size: 1.2rem;
  cursor: pointer;
  position: relative;
  transition: transform 0.2s ease, background 0.2s ease;
}

.notification-bell:hover {
  background: var(--theme-fab-surface-hover);
  transform: translateY(-1px);
}

.notification-badge {
  position: absolute;
  top: -4px;
  right: -4px;
  min-width: 22px;
  height: 22px;
  padding: 0 6px;
  border-radius: 999px;
  background: var(--theme-danger);
  color: #fff;
  font-size: 0.74rem;
  font-weight: 700;
  line-height: 22px;
}

.notification-panel {
  width: min(92vw, 390px);
  max-height: min(72vh, 530px);
  margin-top: 12px;
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
  top: 18px;
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

@keyframes spinOnce {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(180deg); }
}

.animate-spin-once {
  animation: spinOnce 0.5s ease-in-out;
}

@media (max-width: 600px) {
  .fab-container {
    right: 14px;
    bottom: 14px;
  }

  .notification-container {
    top: 12px;
    right: 12px;
  }

  .instant-popup {
    top: 10px;
  }

  .notification-bell {
    width: 48px;
    height: 48px;
    font-size: 1rem;
  }

  .fab {
    width: 54px;
    height: 54px;
    font-size: 1.35rem;
  }

  .fab-menu {
    min-width: 172px;
    padding: 8px;
  }

  .menu-item {
    font-size: 0.95rem;
    padding: 8px 10px;
  }
}
</style>
