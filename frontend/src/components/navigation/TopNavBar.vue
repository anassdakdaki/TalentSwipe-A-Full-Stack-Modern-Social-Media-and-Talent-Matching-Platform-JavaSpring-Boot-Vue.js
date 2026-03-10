<template>
  <header class="top-nav">
    <form class="search-wrap" @submit.prevent="submitSearch">
      <i class="fas fa-search"></i>
      <input
        v-model.trim="searchQuery"
        type="search"
        placeholder="Search communities, people, posts"
        aria-label="Search"
      />
    </form>

    <div class="top-actions">
      <button
        ref="notificationButton"
        class="icon-action"
        type="button"
        @click="$emit('toggle-notifications')"
        aria-label="Notifications"
      >
        <i class="fas fa-bell"></i>
        <span v-if="unreadNotifications > 0" class="action-badge">
          {{ formatBadgeCount(unreadNotifications) }}
        </span>
      </button>

      <div ref="accountWrap" class="account-wrap">
        <button class="account-button" type="button" @click="isAccountMenuOpen = !isAccountMenuOpen">
          <img v-if="userAvatarUrl" :src="userAvatarUrl" alt="Account avatar" class="account-avatar" />
          <span v-else class="account-avatar placeholder"><i class="fas fa-user"></i></span>
          <span class="account-name">{{ userName || 'Account' }}</span>
          <i class="fas fa-chevron-down caret"></i>
        </button>

        <transition name="menu-fade">
          <div v-if="isAccountMenuOpen" class="account-menu">
            <button class="account-menu-item danger" type="button" @click="logout">
              <i class="fas fa-sign-out-alt"></i>
              Logout
            </button>
          </div>
        </transition>
      </div>
    </div>
  </header>
</template>

<script>
export default {
  name: 'TopNavBar',
  props: {
    unreadNotifications: {
      type: Number,
      default: 0,
    },
    userName: {
      type: String,
      default: '',
    },
    userAvatarUrl: {
      type: String,
      default: '',
    },
  },
  emits: ['search', 'toggle-notifications', 'logout'],
  data() {
    return {
      searchQuery: '',
      isAccountMenuOpen: false,
    };
  },
  mounted() {
    document.addEventListener('pointerdown', this.handleOutsideClick);
  },
  beforeUnmount() {
    document.removeEventListener('pointerdown', this.handleOutsideClick);
  },
  methods: {
    submitSearch() {
      this.$emit('search', this.searchQuery);
    },
    logout() {
      this.isAccountMenuOpen = false;
      this.$emit('logout');
    },
    handleOutsideClick(event) {
      if (!this.isAccountMenuOpen) {
        return;
      }
      const wrap = this.$refs.accountWrap;
      if (!wrap || wrap.contains(event.target)) {
        return;
      }
      this.isAccountMenuOpen = false;
    },
    formatBadgeCount(count) {
      return count > 99 ? '99+' : String(count);
    },
  },
};
</script>

<style scoped>
.top-nav {
  height: 72px;
  padding: 10px 16px;
  border-bottom: 1px solid var(--theme-divider);
  background: color-mix(in srgb, var(--theme-surface-elevated) 92%, transparent);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  position: sticky;
  top: 0;
  z-index: 60;
  backdrop-filter: blur(8px);
}

.search-wrap {
  flex: 1;
  max-width: 560px;
  min-width: 200px;
  border: 1px solid var(--theme-input-border);
  border-radius: 12px;
  background: var(--theme-input-bg);
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 0 10px;
}

.search-wrap i {
  color: var(--theme-text-subtle);
}

.search-wrap input {
  flex: 1;
  border: none;
  background: transparent;
  color: var(--theme-input-text);
  min-height: 40px;
  outline: none;
  font-size: 0.94rem;
}

.search-wrap input::placeholder {
  color: var(--theme-input-placeholder);
}

.top-actions {
  display: inline-flex;
  align-items: center;
  gap: 10px;
}

.icon-action {
  width: 42px;
  height: 42px;
  border-radius: 10px;
  border: 1px solid var(--theme-button-secondary-border);
  background: var(--theme-button-secondary-bg);
  color: var(--theme-button-secondary-text);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  position: relative;
  cursor: pointer;
}

.action-badge {
  position: absolute;
  top: -5px;
  right: -5px;
  min-width: 19px;
  height: 19px;
  border-radius: 999px;
  background: var(--theme-danger);
  color: #fff;
  font-size: 0.68rem;
  font-weight: 700;
  line-height: 19px;
  text-align: center;
  padding: 0 5px;
}

.account-wrap {
  position: relative;
}

.account-button {
  border-radius: 12px;
  border: 1px solid var(--theme-button-secondary-border);
  background: var(--theme-button-secondary-bg);
  color: var(--theme-text-primary);
  min-height: 42px;
  padding: 5px 10px 5px 6px;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.account-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  object-fit: cover;
}

.account-avatar.placeholder {
  background: var(--theme-surface-1);
  border: 1px solid var(--theme-surface-border);
  color: var(--theme-text-subtle);
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.account-name {
  font-weight: 600;
  color: var(--theme-text-primary);
}

.caret {
  font-size: 0.74rem;
  color: var(--theme-text-subtle);
}

.account-menu {
  position: absolute;
  right: 0;
  top: calc(100% + 8px);
  min-width: 176px;
  border-radius: 12px;
  border: 1px solid var(--theme-surface-border);
  background: var(--theme-surface-elevated);
  box-shadow: var(--theme-shadow-soft);
  padding: 6px;
  display: flex;
  flex-direction: column;
  z-index: 80;
}

.account-menu-item {
  border: none;
  width: 100%;
  text-decoration: none;
  text-align: left;
  color: var(--theme-text-primary);
  background: transparent;
  border-radius: 8px;
  padding: 9px 10px;
  display: inline-flex;
  align-items: center;
  gap: 9px;
  cursor: pointer;
  font-size: 0.92rem;
}

.account-menu-item:hover {
  background: var(--theme-menu-item-hover);
}

.account-menu-item.danger {
  color: var(--theme-danger);
}

.menu-fade-enter-active,
.menu-fade-leave-active {
  transition: all 0.15s ease;
}

.menu-fade-enter-from,
.menu-fade-leave-to {
  opacity: 0;
  transform: translateY(-6px);
}

@media (max-width: 980px) {
  .account-name {
    display: none;
  }
}
</style>
