<template>
  <transition name="fade">
    <div v-if="isVisible" class="app-alert-overlay">
      <div class="app-alert-content" :class="toneClass" role="dialog" aria-modal="true" aria-live="polite">
        <div class="alert-icon" aria-hidden="true">
          <i :class="iconClass"></i>
        </div>
        <h3 class="alert-title">{{ title }}</h3>
        <p class="alert-message">{{ message }}</p>
        <div class="alert-actions">
          <button @click="close" class="alert-button">OK</button>
        </div>
      </div>
    </div>
  </transition>
</template>

<script>
export default {
  name: 'AppAlert',
  props: {
    title: {
      type: String,
      default: 'Notification',
    },
    message: {
      type: String,
      default: 'Something happened.',
    },
  },
  data() {
    return {
      isVisible: false,
    };
  },
  computed: {
    toneClass() {
      const label = String(this.title || '').toLowerCase();
      if (label.includes('error') || label.includes('fail') || label.includes('invalid')) {
        return 'tone-error';
      }
      if (label.includes('warning')) {
        return 'tone-warning';
      }
      if (label.includes('success')) {
        return 'tone-success';
      }
      return 'tone-info';
    },
    iconClass() {
      if (this.toneClass === 'tone-error') return 'fas fa-circle-exclamation';
      if (this.toneClass === 'tone-warning') return 'fas fa-triangle-exclamation';
      if (this.toneClass === 'tone-success') return 'fas fa-circle-check';
      return 'fas fa-circle-info';
    },
  },
  methods: {
    show() {
      this.isVisible = true;
    },
    close() {
      this.isVisible = false;
      this.$emit('closed'); // Emit an event when the alert is closed
    },
  },
};
</script>

<style scoped>
.app-alert-overlay {
  position: fixed;
  inset: 0;
  background: color-mix(in srgb, var(--theme-page-background) 52%, #000 48%);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 2000;
  padding: 16px;
  backdrop-filter: blur(4px);
}

.app-alert-content {
  width: min(440px, 95vw);
  border-radius: 16px;
  padding: 20px 20px 16px;
  border: 1px solid var(--theme-surface-border);
  background: var(--theme-surface-elevated);
  box-shadow: var(--theme-shadow-strong);
  text-align: center;
  display: flex;
  flex-direction: column;
  gap: 10px;
  color: var(--theme-text-primary);
}

.alert-icon {
  width: 54px;
  height: 54px;
  margin: 2px auto 4px;
  border-radius: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 1.4rem;
  border: 1px solid var(--theme-surface-border);
  background: var(--theme-surface-1);
}

.alert-title {
  color: var(--theme-heading-color);
  font-size: 1.45rem;
  margin: 0;
  font-family: var(--theme-font-heading);
  font-weight: 700;
}

.alert-message {
  margin: 0;
  font-size: 1rem;
  color: var(--theme-text-secondary);
  line-height: 1.5;
}

.alert-actions {
  margin-top: 6px;
  display: flex;
  justify-content: center;
}

.alert-button {
  background: var(--theme-button-primary-bg);
  color: var(--theme-button-primary-text);
  border: none;
  border-radius: 10px;
  min-width: 100px;
  height: 40px;
  padding: 0 16px;
  font-size: 0.95rem;
  font-weight: 700;
  cursor: pointer;
  transition: transform 0.15s ease, filter 0.15s ease;
  box-shadow: var(--theme-button-primary-shadow);
}

.alert-button:hover {
  filter: brightness(1.04);
  transform: translateY(-2px);
}

.tone-success .alert-icon {
  color: var(--theme-accent);
  border-color: color-mix(in srgb, var(--theme-accent) 45%, var(--theme-surface-border));
  background: color-mix(in srgb, var(--theme-accent) 14%, var(--theme-surface-1));
}

.tone-error .alert-icon {
  color: var(--theme-danger);
  border-color: color-mix(in srgb, var(--theme-danger) 45%, var(--theme-surface-border));
  background: color-mix(in srgb, var(--theme-danger) 14%, var(--theme-surface-1));
}

.tone-warning .alert-icon {
  color: var(--theme-warning);
  border-color: color-mix(in srgb, var(--theme-warning) 45%, var(--theme-surface-border));
  background: color-mix(in srgb, var(--theme-warning) 14%, var(--theme-surface-1));
}

.tone-info .alert-icon {
  color: var(--theme-link);
  border-color: color-mix(in srgb, var(--theme-link) 45%, var(--theme-surface-border));
  background: color-mix(in srgb, var(--theme-link) 14%, var(--theme-surface-1));
}

.fade-enter-active, .fade-leave-active {
  transition: opacity 0.18s ease, transform 0.18s ease;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
  transform: translateY(-8px) scale(0.98);
}
</style> 
