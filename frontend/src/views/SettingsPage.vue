<template>
  <div class="settings-page">
    <div class="page-shell settings-shell">
      <header class="settings-header">
        <h1>Settings</h1>
        <p>Manage app preferences for this device.</p>
      </header>

      <div class="settings-grid">
        <section class="settings-card">
          <h2>Appearance</h2>
          <div class="setting-row">
            <div>
              <h3>Theme</h3>
              <p>Choose how the app looks.</p>
            </div>
            <select v-model="settings.appearance.theme" @change="onThemeChange">
              <option value="futuristic">Futuristic</option>
              <option value="modern">Modern</option>
              <option value="dark">Dark</option>
            </select>
          </div>
          <div class="setting-row">
            <div>
              <h3>Compact Mode</h3>
              <p>Reduce spacing and make layouts denser.</p>
            </div>
            <label class="switch">
              <input
                v-model="settings.appearance.compactMode"
                type="checkbox"
                @change="persistSettings"
              />
              <span class="slider"></span>
            </label>
          </div>
          <div class="setting-row">
            <div>
              <h3>Reduced Motion</h3>
              <p>Limit visual animations and transitions.</p>
            </div>
            <label class="switch">
              <input
                v-model="settings.appearance.reducedMotion"
                type="checkbox"
                @change="persistSettings"
              />
              <span class="slider"></span>
            </label>
          </div>
        </section>

        <section class="settings-card">
          <h2>Language</h2>
          <div class="setting-row">
            <div>
              <h3>App Language</h3>
              <p>Additional languages can be added later.</p>
            </div>
            <select v-model="settings.language.locale" @change="persistSettings">
              <option value="en">English</option>
            </select>
          </div>
        </section>

        <section class="settings-card">
          <h2>Notifications</h2>
          <div class="setting-row">
            <div>
              <h3>Message Alerts</h3>
              <p>Get alerts for new messages.</p>
            </div>
            <label class="switch">
              <input
                v-model="settings.notifications.messageAlerts"
                type="checkbox"
                @change="persistSettings"
              />
              <span class="slider"></span>
            </label>
          </div>
          <div class="setting-row">
            <div>
              <h3>Match Alerts</h3>
              <p>Be notified when you get a new match.</p>
            </div>
            <label class="switch">
              <input
                v-model="settings.notifications.matchAlerts"
                type="checkbox"
                @change="persistSettings"
              />
              <span class="slider"></span>
            </label>
          </div>
          <div class="setting-row">
            <div>
              <h3>Email Updates</h3>
              <p>Receive product and activity updates by email.</p>
            </div>
            <label class="switch">
              <input
                v-model="settings.notifications.emailUpdates"
                type="checkbox"
                @change="persistSettings"
              />
              <span class="slider"></span>
            </label>
          </div>
        </section>

        <section class="settings-card">
          <h2>Privacy</h2>
          <div class="setting-row">
            <div>
              <h3>Profile Visibility</h3>
              <p>Control who can see your profile details.</p>
            </div>
            <select v-model="settings.privacy.profileVisibility" @change="persistSettings">
              <option value="public">Public</option>
              <option value="connections">Connections Only</option>
            </select>
          </div>
          <div class="setting-row">
            <div>
              <h3>Discovery Visibility</h3>
              <p>Allow your profile to appear in matches.</p>
            </div>
            <label class="switch">
              <input
                v-model="settings.privacy.discoveryVisible"
                type="checkbox"
                @change="persistSettings"
              />
              <span class="slider"></span>
            </label>
          </div>
          <div class="setting-row">
            <div>
              <h3>Read Receipts</h3>
              <p>Let others know when you read their messages.</p>
            </div>
            <label class="switch">
              <input
                v-model="settings.privacy.readReceipts"
                type="checkbox"
                @change="persistSettings"
              />
              <span class="slider"></span>
            </label>
          </div>
          <div class="setting-row">
            <div>
              <h3>Online Status</h3>
              <p>Show when you are active in the app.</p>
            </div>
            <label class="switch">
              <input
                v-model="settings.privacy.showOnlineStatus"
                type="checkbox"
                @change="persistSettings"
              />
              <span class="slider"></span>
            </label>
          </div>
        </section>

        <section class="settings-card settings-card-wide">
          <h2>Session</h2>
          <div class="session-actions">
            <button type="button" class="secondary-btn" @click="resetDefaults">
              Reset to Defaults
            </button>
            <button type="button" class="danger-btn" @click="logout">
              Logout
            </button>
          </div>
        </section>
      </div>

      <p v-if="statusMessage" class="status-message">{{ statusMessage }}</p>
    </div>
  </div>
</template>

<script>
import {
  applyTheme,
  loadAppSettings,
  resetAppSettings,
  saveAppSettings,
} from '../utils/appSettings';

export default {
  name: 'SettingsPage',
  data() {
    return {
      settings: loadAppSettings(),
      statusMessage: '',
      statusTimer: null,
    };
  },
  beforeUnmount() {
    if (this.statusTimer) {
      clearTimeout(this.statusTimer);
    }
  },
  methods: {
    showStatus(message) {
      this.statusMessage = message;
      if (this.statusTimer) {
        clearTimeout(this.statusTimer);
      }
      this.statusTimer = setTimeout(() => {
        this.statusMessage = '';
      }, 1800);
    },
    persistSettings() {
      this.settings = saveAppSettings(this.settings);
      this.showStatus('Settings saved');
    },
    onThemeChange() {
      applyTheme(this.settings.appearance.theme);
      this.persistSettings();
    },
    resetDefaults() {
      this.settings = resetAppSettings();
      this.showStatus('Default settings restored');
    },
    logout() {
      localStorage.removeItem('token');
      this.$router.push('/auth/login');
    },
  },
};
</script>

<style scoped>
.settings-page {
  min-height: 100vh;
  width: 100%;
  padding: 28px 16px 110px;
  background: var(--theme-page-background);
  color: var(--theme-text-primary);
}

.settings-shell {
  width: min(var(--page-max-width), 100%);
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.settings-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--page-content-gap);
}

.settings-header {
  padding: 10px 4px 6px;
}

.settings-header h1 {
  font-size: 2.1rem;
  margin-bottom: 6px;
  color: var(--theme-heading-color);
  line-height: 1.1;
}

.settings-header p {
  color: var(--theme-text-secondary);
  font-size: 1rem;
}

.settings-card {
  background: var(--theme-surface-elevated, var(--theme-surface-background));
  border: 1px solid var(--theme-surface-border);
  border-radius: 20px;
  padding: 20px 22px;
  box-shadow: var(--theme-shadow-soft);
}

.settings-card-wide {
  grid-column: 1 / -1;
}

.settings-card h2 {
  font-size: 1.25rem;
  margin-bottom: 12px;
  color: var(--theme-heading-color);
  letter-spacing: 0.01em;
}

.setting-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 18px;
  padding: 14px 0;
  border-top: 1px solid var(--theme-surface-border);
}

.setting-row:first-of-type {
  border-top: none;
  padding-top: 6px;
}

.setting-row h3 {
  font-size: 1.08rem;
  margin-bottom: 3px;
  color: var(--theme-text-primary);
}

.setting-row p {
  font-size: 0.94rem;
  color: var(--theme-text-secondary);
  line-height: 1.45;
}

select {
  min-width: 210px;
  padding: 10px 12px;
  border-radius: 12px;
  border: 1px solid var(--theme-surface-border);
  background: var(--theme-input-bg, rgba(255, 255, 255, 0.12));
  color: var(--theme-text-primary);
  outline: none;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

html[data-theme='modern'] select {
  background: #ffffff;
}

select:focus {
  border-color: var(--theme-accent);
  box-shadow: 0 0 0 3px rgba(15, 118, 110, 0.16);
}

.switch {
  position: relative;
  display: inline-block;
  width: 52px;
  height: 30px;
}

.switch input {
  opacity: 0;
  width: 0;
  height: 0;
}

.slider {
  position: absolute;
  cursor: pointer;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(148, 163, 184, 0.46);
  transition: 0.2s;
  border-radius: 30px;
  border: 1px solid rgba(148, 163, 184, 0.35);
}

.slider::before {
  position: absolute;
  content: '';
  height: 22px;
  width: 22px;
  left: 4px;
  bottom: 4px;
  background: #ffffff;
  transition: 0.2s;
  border-radius: 50%;
}

.switch input:checked + .slider {
  background: var(--theme-accent);
}

.switch input:checked + .slider::before {
  transform: translateX(22px);
}

.session-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-start;
  padding-top: 4px;
}

.secondary-btn,
.danger-btn {
  border: none;
  border-radius: 12px;
  padding: 11px 16px;
  font-weight: 600;
  cursor: pointer;
  transition: transform 0.15s ease, box-shadow 0.2s ease, filter 0.2s ease;
}

.secondary-btn {
  background: rgba(148, 163, 184, 0.2);
  color: var(--theme-text-primary);
  border: 1px solid var(--theme-surface-border);
}

.danger-btn {
  background: #dc2626;
  color: #ffffff;
}

.secondary-btn:hover,
.danger-btn:hover {
  transform: translateY(-1px);
  filter: brightness(1.03);
}

.status-message {
  text-align: right;
  color: var(--theme-accent);
  font-size: 0.9rem;
  font-weight: 600;
}

@media (max-width: 768px) {
  .settings-page {
    padding: 16px 12px 96px;
  }

  .settings-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .settings-card {
    padding: 15px;
    border-radius: 16px;
  }

  .setting-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  select {
    width: 100%;
    min-width: 0;
  }

  .session-actions {
    width: 100%;
    flex-direction: column;
  }

  .secondary-btn,
  .danger-btn {
    width: 100%;
  }
}
</style>
