<template>
  <transition name="fade">
    <div v-if="isVisible" class="app-alert-overlay">
      <div class="app-alert-content glass-effect">
        <h3 class="alert-title">{{ title }}</h3>
        <p class="alert-message">{{ message }}</p>
        <button @click="close" class="alert-button neon-effect">OK</button>
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
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.8); /* Darker overlay */
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 2000; /* Higher than other modals */
}

.app-alert-content {
  background: rgba(255, 255, 255, 0.08); /* Lighter glass base for modal */
  border: 1px solid rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(20px); /* More blur for a prominent effect */
  -webkit-backdrop-filter: blur(20px);
  border-radius: 20px;
  padding: 30px;
  width: 90%;
  max-width: 400px;
  box-shadow: 0 15px 50px 0 rgba(31, 38, 135, 0.5); /* Stronger shadow */
  color: #e0e0e0;
  font-family: 'Roboto Mono', monospace;
  text-align: center;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.alert-title {
  color: #64ffda; /* Neon green */
  font-size: 1.8rem;
  margin-bottom: 10px;
  text-shadow: 0 0 10px rgba(100, 255, 218, 0.6);
  font-family: 'Orbitron', sans-serif;
}

.alert-message {
  font-size: 1.1rem;
  color: rgba(224, 224, 224, 0.9);
  line-height: 1.5;
}

.alert-button {
  background: linear-gradient(45deg, #00bfff, #007bff); /* Blue gradient */
  color: #ffffff;
  padding: 12px 25px;
  border: none;
  border-radius: 25px;
  font-size: 1rem;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 0 15px rgba(0, 191, 255, 0.4);
  text-transform: uppercase;
  letter-spacing: 0.5px;
  align-self: center; /* Center the button */
}

.alert-button:hover {
  background: linear-gradient(45deg, #007bff, #00bfff);
  box-shadow: 0 0 25px rgba(0, 191, 255, 0.6);
  transform: translateY(-2px);
}

/* Transition styles */
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s ease, transform 0.3s ease;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
  transform: translateY(-20px);
}
</style> 