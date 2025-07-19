<template>
  <div class="discovery-page futuristic-theme">
    <div v-if="loading" class="loading-message">Loading users...</div>
    <div v-if="error" class="error-message">{{ error }}</div>

    <div v-if="profiles && profiles.length" class="centered-card-container">
      <div class="glass-card">
        <div class="profile-header">
          <img 
            v-if="profiles[0].profilePictureUrl"
            :src="profiles[0].profilePictureUrl"
            alt="Profile Picture"
            class="profile-picture-small"
          />
           <div v-else class="placeholder-picture-small">
              <i class="fas fa-user"></i>
            </div>
          <h1 class="user-name">{{ profiles[0].name }}</h1>
        </div>

        <div class="profile-details">
           <div class="detail-item" v-if="profiles[0].age">
              <i class="fas fa-birthday-cake icon"></i>
              <span>{{ profiles[0].age }} years old</span>
           </div>
           <div class="detail-item" v-if="profiles[0].gender">
               <i class="fas fa-venus-mars icon"></i>
               <span>{{ profiles[0].gender }}</span>
           </div>
           <div class="detail-item" v-if="profiles[0].location">
             <i class="fas fa-map-marker-alt icon"></i>
             <span>{{ profiles[0].location }}</span>
           </div>
           <div class="detail-item" v-if="profiles[0].major">
              <i class="fas fa-book icon"></i>
              <span>{{ profiles[0].major }}</span>
           </div>
           <div class="detail-item" v-if="profiles[0].university">
             <i class="fas fa-graduation-cap icon"></i>
             <span>{{ profiles[0].university }}</span>
           </div>

           <div v-if="profiles[0].bio" class="info-section bio-section">
             <h4>About Me</h4>
             <p>{{ profiles[0].bio }}</p>
          </div>

          <div v-if="profiles[0].lookingFor && Object.values(profiles[0].lookingFor).some(val => val)" class="info-section goals-section">
             <h4>Looking For</h4>
             <div class="goals-badges">
                <span v-if="profiles[0].lookingFor.studyPartner" class="goal-badge">🧠 Study Partner</span>
                <span v-if="profiles[0].lookingFor.languageExchange" class="goal-badge">🗣️ Language Exchange</span>
                <span v-if="profiles[0].lookingFor.friendship" class="goal-badge">💬 Friendship</span>
                <span v-if="profiles[0].lookingFor.networking" class="goal-badge">🤝 Networking</span>
                <span v-if="profiles[0].lookingFor.community" class="goal-badge">🌍 Community</span>
             </div>
          </div>

          <div v-if="profiles[0].interests && profiles[0].interests.length" class="info-section interests-section">
             <h4>Interests</h4>
             <div class="tags">
               <span v-for="interest in profiles[0].interests" :key="interest" class="tag">#{{ interest }}</span>
             </div>
          </div>

          <div v-if="profiles[0].languages && profiles[0].languages.length" class="info-section languages-section">
              <h4>Languages</h4>
              <ul>
                <li v-for="lang in profiles[0].languages" :key="lang.name">
                  <i class="fas fa-language icon"></i> {{ lang.name }} ({{ lang.level }})
                </li>
              </ul>
           </div>

          <div v-if="profiles[0].socialLinks && (profiles[0].socialLinks.github || profiles[0].socialLinks.linkedin || profiles[0].socialLinks.instagram)" class="info-section social-links-section">
             <h4>Social Links</h4>
             <p v-if="profiles[0].socialLinks.github"><a :href="profiles[0].socialLinks.github" target="_blank"><i class="fab fa-github icon"></i> GitHub</a></p>
             <p v-if="profiles[0].socialLinks.linkedin"><a :href="profiles[0].socialLinks.linkedin" target="_blank"><i class="fab fa-linkedin icon"></i> LinkedIn</a></p>
             <p v-if="profiles[0].socialLinks.instagram"><a :href="profiles[0].socialLinks.instagram" target="_blank"><i class="fab fa-instagram icon"></i> Instagram</a></p>
          </div>

        </div>

         <!-- Swipe Action Buttons - Positioned within the card for better responsiveness on smaller screens -->
        <div v-if="profiles && profiles.length" class="card-actions-buttons">
           <button @click="triggerLeftSwipe" class="action-button skip-button">❌ Skip</button>
           <button @click="triggerRightSwipe" class="action-button connect-button">✅ Connect</button>
         </div>

      </div>
    </div>

    <div v-else-if="!loading && !error" class="no-users-message empty-state">
      <i class="fas fa-search"></i>
      <p>Nothing to discover right now. Come back later!</p>
    </div>

     <!-- Swipe Instruction - Outside the card -->
     <div v-if="profiles && profiles.length" class="swipe-instruction">
         Swipe or use &larr; / &rarr; keys
     </div>

  </div>
</template>

<script>
import axios from 'axios';
// Removed Swipeable import as we might not use it for full-screen

export default {
  name: 'DiscoveryPage',
  // Removed Swipeable component
  data() {
    return {
      profiles: [],
      loading: true,
      error: null,
      // Removed cardContainerRef as it's not needed with this structure
    };
  },
  created() {
    this.fetchProfiles();
  },
  mounted() {
      // Add keyboard listeners
      window.addEventListener('keydown', this.handleKeyPress);
  },
  beforeDestroy() {
      // Remove keyboard listeners
      window.removeEventListener('keydown', this.handleKeyPress);
  },
  methods: {
    async fetchProfiles() {
      try {
        const token = localStorage.getItem('token');
        const response = await axios.get('http://localhost:8080/api/profiles/discover', {
          headers: {
            Authorization: `Bearer ${token}`
          }
        });
        // Assuming the backend returns an array of profiles
        this.profiles = response.data.map(profile => {
           if (profile.profilePictureUrl && !profile.profilePictureUrl.startsWith('http')) {
               profile.profilePictureUrl = `http://localhost:8080${profile.profilePictureUrl}`;
           }
            // Ensure nested objects are present for template structure
            if (!profile.lookingFor) profile.lookingFor = {};
            if (!profile.socialLinks) profile.socialLinks = {};
            if (!profile.interests) profile.interests = [];
            if (!profile.languages) profile.languages = [];

           return profile;
        });

         // For this design, we only display one profile at a time.
         // The array will be shifted as users are swiped.

        this.loading = false;
      } catch (error) {
        this.error = 'Failed to fetch users for discovery.';
        console.error('Error fetching discovery profiles:', error);
        this.loading = false;
      }
    },
     handleKeyPress(event) {
        if (this.profiles.length === 0 || this.loading) return; // Do nothing if no profiles or loading

        if (event.key === 'ArrowLeft') {
            this.triggerLeftSwipe();
        } else if (event.key === 'ArrowRight') {
            this.triggerRightSwipe();
        }
    },
    async handleSwipe(direction) { // Simplified handleSwipe for single card
      console.log(`Swiped ${direction}`);

      const swipedUserId = this.profiles[0]?.user.id; // Always get the ID of the first user
      if (!swipedUserId) {
        console.error('Could not get swiped user ID');
        // Even if no user ID, remove the card visually
        this.removeTopCard();
        return; 
      }

      const swipeType = direction.toUpperCase(); // Get swipe type (LEFT or RIGHT)
      if (swipeType !== 'LEFT' && swipeType !== 'RIGHT') {
         console.error('Invalid swipe direction:', direction);
         this.removeTopCard();
         return; 
      }

      // No manual animation needed for this design, let Vue handle removal

      try {
          const token = localStorage.getItem('token');
          const response = await axios.post('http://localhost:8080/api/matches/swipe', {
              swipedUserId: swipedUserId,
              swipeType: swipeType === 'RIGHT' ? 'LIKE' : 'DISLIKE' // Map direction to backend SwipeType enum
          }, {
              headers: {
                  Authorization: `Bearer ${token}`
              }
          });
          console.log(`Sent ${direction} swipe to backend for user ${swipedUserId}`);

          if (response.data.match) {
              console.log('It\'s a match!');
              // Optionally, show a match notification or animation here
          }

           // Remove the profile from the array AFTER the backend call is successful
           // This will trigger Vue to remove the element from the DOM, which is enough for this design
           this.removeTopCard();
           console.log(`Removed top profile after backend call`);

      } catch (error) {
          console.error(`Error sending ${direction} swipe to backend:`, error);
          // Optionally, show an error message to the user
          // No need to reset card position as no animation is manually applied
      }
    },
    triggerLeftSwipe() {
       console.log('triggerLeftSwipe called');
       this.handleSwipe('left');
    },
    triggerRightSwipe() {
       console.log('triggerRightSwipe called');
       this.handleSwipe('right');
    },
     removeTopCard() {
         // Removes the first profile from the array, triggering Vue to update the view
         this.profiles.shift();
     }
  }
};
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;600;700&display=swap');

/* Basic Futuristic Theme */
.discovery-page.futuristic-theme {
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%); /* Dark gradient background */
  color: #e0e0e0; /* Light text */
  position: absolute; /* Position absolutely to fill the parent */
  top: 0;
  left: 0;
  width: 100%; /* Fill parent width */
  height: 100%; /* Fill parent height */
  display: flex; /* Make this a flex container */
  flex-direction: column; /* Stack children vertically */
  align-items: center; /* Center content horizontally */
  justify-content: center; /* Center content vertically */
  /* Removed padding from here to prevent container overflow */
  box-sizing: border-box; /* Include padding and border in the element's total width and height */
  overflow: hidden; /* Prevent this element from causing scrollbars */
  font-family: 'Poppins', sans-serif; /* Futuristic sans-serif font */
}

.loading-message,
.error-message,
.no-users-message {
    color: #ffffff;
    font-size: 1.5rem;
    margin-top: 50px;
    text-align: center;
}

.centered-card-container {
    display: flex;
    justify-content: center;
    align-items: center;
    width: 100%; /* Ensure it fills the discovery page width */
    max-width: 1200px;
    flex-grow: 0; /* Prevent this from growing and potentially pushing content */
    /* Padding handled by the glass card */
    box-sizing: border-box;
    overflow: hidden; /* Prevent overflow within this container */
    /* This container will handle vertical and horizontal centering of the card */
}

.glass-card {
  display: flex;
  flex-direction: column;
  width: 95%;
  max-width: 700px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border-radius: 20px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.37);
  padding: 30px; /* Apply padding here instead of parent */
  color: white;
  overflow-y: auto; /* Allow vertical scrolling WITHIN the card */
  max-height: 80vh;
  position: relative;

  opacity: 0;
  transform: translateY(20px);
  animation: fadeInScale 0.5s ease-out forwards;
}

@keyframes fadeInScale {
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.profile-header {
  display: flex;
  align-items: center;
  margin-bottom: 25px;
  padding-bottom: 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.profile-picture-small {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  object-fit: cover;
  margin-right: 20px;
  border: 3px solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 0 15px rgba(100, 255, 218, 0.3);
}

.placeholder-picture-small {
   width: 80px;
   height: 80px;
   border-radius: 50%;
   background-color: rgba(255, 255, 255, 0.1);
   display: flex;
   justify-content: center;
   align-items: center;
   font-size: 2rem;
   color: rgba(255, 255, 255, 0.4);
   margin-right: 20px;
   border: 3px solid rgba(255, 255, 255, 0.3);
}

.user-name {
  font-size: 2.2rem;
  font-weight: 700;
  margin: 0;
  background: linear-gradient(45deg, #fff, #a8b2d1);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.profile-details {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.info-section {
  margin-top: 20px;
  padding-top: 15px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.info-section:first-of-type {
  border-top: none;
  margin-top: 0;
  padding-top: 0;
}

.info-section h4 {
  color: #64ffda;
  margin-top: 0;
  margin-bottom: 10px;
  font-size: 1.2rem;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 1rem;
  color: #e0e0e0;
}

.detail-item i.icon {
  color: #64ffda;
  font-size: 1.1rem;
}

.goals-badges,
.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 8px;
}

.goal-badge,
.tag {
  background: rgba(255, 255, 255, 0.15);
  color: #ffffff;
  padding: 6px 12px;
  border-radius: 15px;
  font-size: 0.9rem;
  backdrop-filter: blur(5px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.languages-section ul {
    list-style: none;
    padding: 0;
    margin: 0;
}

.languages-section li {
    margin-bottom: 5px;
    color: #e0e0e0;
    font-size: 1rem;
}

.languages-section li i.icon {
    color: #64ffda;
    margin-right: 8px;
}

.social-links-section a {
    color: #64ffda;
    text-decoration: none;
    transition: color 0.3s ease;
    display: inline-flex;
    align-items: center;
    gap: 8px;
}

.social-links-section a:hover {
    text-decoration: underline;
    color: #ffffff;
}

.card-actions-buttons {
    display: flex;
    justify-content: center;
    gap: 20px;
    margin-top: 30px;
    padding-top: 20px;
    border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.action-button {
  padding: 12px 25px;
  font-size: 1rem;
  cursor: pointer;
  border: none;
  border-radius: 25px;
  color: white;
  transition: background-color 0.3s ease, transform 0.2s ease, box-shadow 0.3s ease;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.3);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.skip-button {
  background: linear-gradient(45deg, #ff4d4f, #e02e30);
}

.skip-button:hover {
    background: linear-gradient(45deg, #e02e30, #ff4d4f);
    box-shadow: 0 6px 20px rgba(255, 77, 79, 0.4);
}

.connect-button {
    background: linear-gradient(45deg, #52c41a, #419e15);
}

.connect-button:hover {
    background: linear-gradient(45deg, #419e15, #52c41a);
    box-shadow: 0 6px 20px rgba(82, 196, 26, 0.4);
}

.action-button:active {
  transform: scale(0.95);
}

.swipe-instruction {
  text-align: center;
  color: rgba(255, 255, 255, 0.7);
  font-size: 1rem;
  margin-top: 20px;
  z-index: 1;
}

.empty-state {
  text-align: center;
  color: #b0b0e0;
  margin-top: 50px;
  font-size: 1.2rem;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.empty-state i {
    color: #64ffda;
    font-size: 4rem;
    margin-bottom: 20px;
}

/* Responsive adjustments */
@media screen and (max-width: 768px) {
  .centered-card-container {
      padding: 10px;
  }

  .glass-card {
    width: 100%;
    padding: 20px;
    max-height: 90vh;
  }

  .profile-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .profile-picture-small,
  .placeholder-picture-small {
    margin-right: 0;
    margin-bottom: 15px;
  }

  .user-name {
      font-size: 1.8rem;
  }

  .profile-details {
      gap: 12px;
  }

  .info-section {
      margin-top: 15px;
      padding-top: 10px;
  }

  .info-section h4 {
      font-size: 1.1rem;
  }

  .detail-item {
      font-size: 0.95rem;
  }

  .goal-badge,
  .tag {
      font-size: 0.85rem;
  }

  .card-actions-buttons {
      flex-direction: row;
      gap: 15px;
      margin-top: 20px;
      padding-top: 15px;
  }

  .action-button {
      flex: 1;
      padding: 10px 20px;
      font-size: 0.9rem;
  }

   .swipe-instruction {
       margin-top: 15px;
       font-size: 0.9rem;
   }

   .loading-message,
   .error-message,
   .no-users-message {
       margin-top: 30px;
       font-size: 1.2rem;
   }
}

</style> 