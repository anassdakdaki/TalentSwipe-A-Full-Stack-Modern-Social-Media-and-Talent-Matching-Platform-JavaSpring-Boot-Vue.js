<template>
  <aside :class="['desktop-sidebar', { collapsed }]">
    <div class="sidebar-brand" :title="collapsed ? 'Biblo' : ''">
      <img src="/favicon.ico" alt="Biblo logo" class="brand-mark" />
      <span v-if="!collapsed" class="brand-name">Biblo</span>
    </div>

    <nav class="sidebar-nav" aria-label="Primary">
      <router-link
        v-for="item in navItems"
        :key="item.label"
        :to="item.to"
        :class="['nav-item', { active: isActive(item) }]"
        :title="collapsed ? item.label : ''"
      >
        <span class="nav-icon">
          <i :class="['fas', item.icon]"></i>
        </span>
        <span v-if="!collapsed" class="nav-label">{{ item.label }}</span>
        <span v-if="item.badgeKey === 'chat' && chatUnreadCount > 0" class="nav-badge">
          {{ formatBadgeCount(chatUnreadCount) }}
        </span>
      </router-link>
    </nav>
  </aside>
</template>

<script>
export default {
  name: 'DesktopSidebar',
  props: {
    collapsed: {
      type: Boolean,
      default: false,
    },
    currentPath: {
      type: String,
      default: '',
    },
    chatUnreadCount: {
      type: Number,
      default: 0,
    },
  },
  data() {
    return {
      navItems: [
        { label: 'Feed', to: '/authenticated/feed', icon: 'fa-stream', matchPrefix: '/authenticated/feed' },
        { label: 'Communities', to: '/authenticated/communities', icon: 'fa-users', matchPrefix: '/authenticated/communities' },
        { label: 'Connect', to: '/authenticated/matches', icon: 'fa-user-plus', matchPrefix: '/authenticated/matches' },
        { label: 'Chat', to: '/authenticated/chat', icon: 'fa-comments', matchPrefix: '/authenticated/chat', badgeKey: 'chat' },
        { label: 'Profile', to: '/authenticated/profile', icon: 'fa-user', matchPrefix: '/authenticated/profile' },
        { label: 'Settings', to: '/authenticated/settings', icon: 'fa-cog', matchPrefix: '/authenticated/settings' },
      ],
    };
  },
  methods: {
    isActive(item) {
      if (!this.currentPath) {
        return false;
      }
      return this.currentPath.startsWith(item.matchPrefix);
    },
    formatBadgeCount(count) {
      return count > 99 ? '99+' : String(count);
    },
  },
};
</script>

<style scoped>
.desktop-sidebar {
  width: 248px;
  height: 100vh;
  position: sticky;
  top: 0;
  padding: 18px 12px;
  border-right: 1px solid var(--theme-surface-border);
  background: var(--theme-surface-elevated);
  box-shadow: inset -1px 0 0 var(--theme-divider);
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.desktop-sidebar.collapsed {
  width: 84px;
  padding-inline: 10px;
}

.sidebar-brand {
  min-height: 52px;
  border-radius: 14px;
  border: 1px solid var(--theme-surface-border);
  background: var(--theme-surface-1);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 0 12px;
}

.brand-mark {
  width: 30px;
  height: 30px;
  border-radius: 0;
  object-fit: cover;
  border: 0;
  box-shadow: none;
}

.brand-name {
  font-weight: 800;
  letter-spacing: 0.02em;
  color: var(--theme-heading-color);
}

.sidebar-nav {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.nav-item {
  position: relative;
  border-radius: 12px;
  text-decoration: none;
  color: var(--theme-text-secondary);
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  min-height: 44px;
  transition: background 0.18s ease, color 0.18s ease;
}

.desktop-sidebar.collapsed .nav-item {
  justify-content: center;
  padding-inline: 8px;
}

.nav-item:hover {
  background: var(--theme-menu-item-hover);
  color: var(--theme-text-primary);
}

.nav-item.active {
  background: color-mix(in srgb, var(--theme-accent) 16%, transparent);
  color: var(--theme-text-primary);
  border: 1px solid color-mix(in srgb, var(--theme-accent) 38%, var(--theme-surface-border));
}

.nav-icon {
  width: 18px;
  text-align: center;
  color: var(--theme-accent);
}

.nav-label {
  font-weight: 600;
}

.nav-badge {
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

.desktop-sidebar.collapsed .nav-badge {
  position: absolute;
  top: 4px;
  right: 4px;
  margin-left: 0;
}
</style>
