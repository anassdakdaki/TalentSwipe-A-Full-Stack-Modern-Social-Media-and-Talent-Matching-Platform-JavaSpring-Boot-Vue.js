<template>
  <div id="app">
    <!-- Render the view component based on the current route -->
    <router-view></router-view>

    <!-- Floating Action Button Container -->
    <div class="fab-container">
      <!-- The FAB button -->
      <button class="fab" @click="toggleFabMenu">
        <i :class="['fas', isFabMenuOpen ? 'fa-times animate-spin-once' : 'fa-bars']"></i>
      </button>

      <!-- Futuristic Expanding Menu -->
      <transition name="fab-menu">
        <div v-if="isFabMenuOpen" class="fab-menu">
          <router-link to="/authenticated/profile" class="menu-item" @click="toggleFabMenu">
            <i class="fas fa-user"></i>
            <span>Profile</span>
          </router-link>
          <router-link to="/authenticated/communities" class="menu-item" @click="toggleFabMenu">
            <i class="fas fa-users"></i>
            <span>Communities</span>
          </router-link>
          <router-link to="/authenticated/matches" class="menu-item" @click="toggleFabMenu">
             <i class="fas fa-heart"></i>
            <span>Matches</span>
          </router-link>
          <router-link to="/authenticated/chat" class="menu-item" @click="toggleFabMenu">
             <i class="fas fa-comments"></i>
            <span>Chat</span>
          </router-link>
        </div>
      </transition>
    </div>
  </div>
</template>

<script>
// Removed the import and usage of AppLayout here
// The router will handle rendering the layout component

export default {
  name: 'App',
  data() {
    return {
      isFabMenuOpen: false,
    };
  },
  methods: {
    toggleFabMenu() {
      this.isFabMenuOpen = !this.isFabMenuOpen;
    },
  },
  // Removed computed and methods related to authentication/layout from here
  // Authentication logic is handled in the router guard and login view
}
</script>

<style>
/* Reset and base styles */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

/* Global Font Awesome Fix */
i.fas::before {
  font-family: "Font Awesome 5 Free";
  font-weight: 900 !important;
}

i.fab::before {
  font-family: "Font Awesome 5 Brands";
  font-weight: 900 !important;
}

body {
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, Verdana, sans-serif;
  line-height: 1.6;
  color: #2c3e50;
  /* Remove background color from here */
  /* background-color: #f5f7fa; */
  /* Prevent scroll on body due to FAB positioning */
}

#app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  width: 100%; /* Ensure #app takes full width */
  flex-grow: 1; /* Ensure #app grows in flex parent (body) */
  position: relative; /* Needed for FAB absolute/fixed positioning */
  /* Prevent scroll on app container */
}

/* Ensure router views take available space */
#app > div {
  flex-grow: 1;
}

/* Keep relevant styles or move them to specific components if necessary */
/* Removed layout-specific styles that are now in AppLayout.vue */

/* Example: Basic padding for content */
/* .main-content styles can be moved into the layout component's content div */

/* Media queries from previous version might need review */

/* FAB Container */
.fab-container {
  position: fixed;
  bottom: 20px;
  right: 20px;
  z-index: 1000; /* Ensure it's on top */
  display: flex;
  flex-direction: column;
  align-items: flex-end; /* Align items to the right */
}

/* The FAB Button */
.fab {
  background: rgba(255, 255, 255, 0.15); /* Glassmorphism background */
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px); /* Safari support */
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
  width: 70px; /* Slightly larger */
  height: 70px; /* Slightly larger */
  border-radius: 50%; /* Circular shape */
  color: #64ffda; /* Accent color */
  font-size: 1.8rem; /* Larger icon */
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  transition: all 0.3s ease;
  outline: none;
  margin-top: 10px; /* Space above menu when open */
  animation: pulse 2s infinite cubic-bezier(0.66, 0, 0.33, 1); /* Add a pulse animation */
}

.fab i {
  font-family: "Font Awesome 5 Free"; /* Ensure Font Awesome font is used */
  font-weight: 900; /* For solid icons (fas) - forcefully apply */
  color: #ffffff; /* Explicitly set icon color to white for visibility test */
}

.fab i::before {
  font-weight: 900; /* Ensure the pseudo-element also has the correct font-weight */
}

.fab:hover {
  background: rgba(255, 255, 255, 0.25);
  box-shadow: 0 6px 25px rgba(100, 255, 218, 0.4); /* Glowing effect */
  animation: none; /* Stop pulse on hover */
}

.fab:active {
    transform: scale(0.95); /* Press effect */
}

/* FAB Menu Styling */
.fab-menu {
  display: flex;
  flex-direction: column; /* Vertical menu */
  align-items: flex-end; /* Align items to the right */
  margin-bottom: 15px; /* Space between menu and FAB */
  background: rgba(255, 255, 255, 0.1); /* Glassmorphism background */
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 15px;
  padding: 15px; /* Increased padding */
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
  min-width: 180px; /* Give it a minimum width for content */
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 12px 18px; /* Increased padding */
  color: #e0e0e0; /* Light text color */
  text-decoration: none;
  font-size: 1.2rem; /* Slightly larger text */
  width: 100%; /* Full width within the menu */
  transition: all 0.3s ease;
  border-radius: 10px;
}

.menu-item:hover {
  background: rgba(255, 255, 255, 0.1); /* Subtle hover background */
  color: #ffffff; /* White on hover */
}

.menu-item i {
  margin-right: 10px; /* Space between icon and text */
  color: #64ffda; /* Accent color for icons */
}

.menu-item i::before {
  font-family: "Font Awesome 5 Free"; /* Ensure Font Awesome font is used */
  font-weight: 900; /* Force solid icon weight */
}

/* Animations */
.fab-menu-enter-active,
.fab-menu-leave-active {
  transition: all 0.4s ease-out; /* Slightly longer and smoother */
}

.fab-menu-enter-from,
.fab-menu-leave-to {
  opacity: 0;
  transform: translateY(30px) scale(0.95); /* Slide up and slightly shrink */
}

/* FAB icon rotation animation */
@keyframes spinOnce {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(180deg); }
}

.animate-spin-once {
  animation: spinOnce 0.5s ease-in-out;
}

/* Pulse animation for FAB */
@keyframes pulse {
  0% { box-shadow: 0 0 0 0 rgba(100, 255, 218, 0.4); }
  70% { box-shadow: 0 0 0 20px rgba(100, 255, 218, 0); }
  100% { box-shadow: 0 0 0 0 rgba(100, 255, 218, 0); }
}

/* Responsive adjustments */
@media screen and (max-width: 600px) {
  .fab-container {
    bottom: 15px;
    right: 15px;
  }

  .fab {
    width: 50px;
    height: 50px;
    font-size: 1.3rem;
  }

  .fab-menu {
      padding: 8px;
  }

  .menu-item {
    padding: 8px 12px;
    font-size: 1rem;
  }
}

</style>
