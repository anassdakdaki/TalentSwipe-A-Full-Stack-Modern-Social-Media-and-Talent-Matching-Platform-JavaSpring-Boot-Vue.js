<template>
  <div class="discovery-page">
    <div class="page-shell">
      <div v-if="loading" class="state-message">Loading users...</div>
      <div v-else-if="error" class="state-message error-message">{{ error }}</div>

      <template v-else-if="currentProfile">
        <article class="match-card">
          <header class="match-header">
            <div class="identity-row">
              <img
                v-if="currentProfile.profilePictureUrl"
                :src="currentProfile.profilePictureUrl"
                alt="Profile picture"
                class="profile-picture"
              />
              <div v-else class="profile-placeholder">
                <i class="fas fa-user"></i>
              </div>

              <div>
                <h1 class="user-name">{{ currentProfile.name || 'Community Member' }}</h1>
                <p class="identity-meta" v-if="currentProfile.location">
                  <i class="fas fa-map-marker-alt"></i>
                  {{ currentProfile.location }}
                </p>
              </div>
            </div>

            <button class="view-profile-btn" @click="openPublicProfile">
              <i class="fas fa-id-card"></i>
              View Full Profile
            </button>
          </header>

          <div class="identity-facts" v-if="currentProfile.age || currentProfile.gender">
            <span class="fact-chip" v-if="currentProfile.age">
              <i class="fas fa-birthday-cake"></i>
              {{ currentProfile.age }} years old
            </span>
            <span class="fact-chip" v-if="currentProfile.gender">
              <i class="fas fa-venus-mars"></i>
              {{ currentProfile.gender }}
            </span>
          </div>

          <div class="context-row" v-if="currentProfile.major || currentProfile.university">
            <span class="context-item" v-if="currentProfile.major">
              <i class="fas fa-briefcase"></i>
              {{ currentProfile.major }}
            </span>
            <span class="context-item" v-if="currentProfile.university">
              <i class="fas fa-graduation-cap"></i>
              {{ currentProfile.university }}
            </span>
          </div>

          <section v-if="currentProfile.bio" class="info-section">
            <h3>About</h3>
            <p>{{ currentProfile.bio }}</p>
          </section>

          <section v-if="hasLookingFor" class="info-section">
            <h3>Looking For</h3>
            <div class="chip-list">
              <span v-if="currentProfile.lookingFor.studyPartner" class="chip">Study Partner</span>
              <span v-if="currentProfile.lookingFor.languageExchange" class="chip">Language Exchange</span>
              <span v-if="currentProfile.lookingFor.friendship" class="chip">Friendship</span>
              <span v-if="currentProfile.lookingFor.networking" class="chip">Networking</span>
              <span v-if="currentProfile.lookingFor.community" class="chip">Community</span>
            </div>
          </section>

          <div class="compact-info-grid">
            <section v-if="currentProfile.interests && currentProfile.interests.length" class="info-section">
              <h3>Interests</h3>
              <div class="chip-list">
                <span v-for="interest in currentProfile.interests" :key="interest" class="chip">#{{ interest }}</span>
              </div>
            </section>

            <section v-if="currentProfile.languages && currentProfile.languages.length" class="info-section">
              <h3>Languages</h3>
              <ul class="clean-list">
                <li v-for="lang in currentProfile.languages" :key="`${lang.name}-${lang.level}`">
                  {{ lang.name }} ({{ lang.level }})
                </li>
              </ul>
            </section>

            <section v-if="hasSocialLinks" class="info-section info-section-wide">
              <h3>Social Links</h3>
              <div class="social-links">
                <a v-if="currentProfile.socialLinks.github" :href="currentProfile.socialLinks.github" target="_blank" rel="noopener noreferrer">GitHub</a>
                <a v-if="currentProfile.socialLinks.linkedin" :href="currentProfile.socialLinks.linkedin" target="_blank" rel="noopener noreferrer">LinkedIn</a>
                <a v-if="currentProfile.socialLinks.instagram" :href="currentProfile.socialLinks.instagram" target="_blank" rel="noopener noreferrer">Instagram</a>
              </div>
            </section>
          </div>
        </article>

        <p class="swipe-tip">
          Tip: use <kbd>&larr;</kbd> to Skip and <kbd>&rarr;</kbd> to Connect
        </p>

        <p v-if="feedbackMessage" class="feedback-message">{{ feedbackMessage }}</p>

        <div class="floating-match-actions">
          <button @click="triggerLeftSwipe" class="match-action skip-button">
            <i class="fas fa-forward"></i>
            <span>Skip</span>
          </button>
          <button @click="triggerRightSwipe" class="match-action connect-button">
            <i class="fas fa-handshake"></i>
            <span>Connect</span>
          </button>
        </div>
      </template>

      <div v-else class="empty-state">
        <i class="fas fa-search"></i>
        <p>Nothing to discover right now. Come back later.</p>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080').replace(/\/+$/, '');

export default {
  name: 'DiscoveryPage',
  data() {
    return {
      profiles: [],
      loading: true,
      error: null,
      feedbackMessage: '',
    };
  },
  computed: {
    currentProfile() {
      return this.profiles.length ? this.profiles[0] : null;
    },
    hasLookingFor() {
      if (!this.currentProfile?.lookingFor) return false;
      return Object.values(this.currentProfile.lookingFor).some(Boolean);
    },
    hasSocialLinks() {
      const links = this.currentProfile?.socialLinks || {};
      return Boolean(links.github || links.linkedin || links.instagram);
    },
  },
  created() {
    this.fetchProfiles();
  },
  mounted() {
    window.addEventListener('keydown', this.handleKeyPress);
  },
  beforeUnmount() {
    window.removeEventListener('keydown', this.handleKeyPress);
  },
  methods: {
    normalizeMediaUrl(url) {
      if (!url) return '';
      if (url.startsWith('http://') || url.startsWith('https://')) return url;
      return `${API_BASE_URL}${url.startsWith('/') ? url : `/${url}`}`;
    },
    getSwipedUserId(profile) {
      return profile?.user?.id || profile?.userId || profile?.id || null;
    },
    async fetchProfiles() {
      this.loading = true;
      this.error = null;
      try {
        const token = localStorage.getItem('token');
        const response = await axios.get(`${API_BASE_URL}/api/profiles/discover`, {
          headers: { Authorization: `Bearer ${token}` },
        });

        this.profiles = (response.data || []).map((profile) => ({
          ...profile,
          profilePictureUrl: this.normalizeMediaUrl(profile.profilePictureUrl),
          lookingFor: profile.lookingFor || {},
          socialLinks: profile.socialLinks || {},
          interests: profile.interests || [],
          languages: profile.languages || [],
        }));
      } catch (error) {
        this.error = 'Failed to fetch users for discovery.';
        console.error('Error fetching discovery profiles:', error);
      } finally {
        this.loading = false;
      }
    },
    handleKeyPress(event) {
      if (this.loading || !this.currentProfile) return;
      if (event.key === 'ArrowLeft') this.triggerLeftSwipe();
      if (event.key === 'ArrowRight') this.triggerRightSwipe();
    },
    async handleSwipe(direction) {
      if (!this.currentProfile) return;

      const swipedUserId = this.getSwipedUserId(this.currentProfile);
      if (!swipedUserId) {
        this.removeTopCard();
        return;
      }

      const swipeType = direction === 'right' ? 'LIKE' : 'DISLIKE';

      try {
        const token = localStorage.getItem('token');
        const response = await axios.post(
          `${API_BASE_URL}/api/matches/swipe`,
          { swipedUserId, swipeType },
          { headers: { Authorization: `Bearer ${token}` } }
        );

        if (response.data?.match) {
          this.feedbackMessage = 'Mutual match! You can message each other now.';
        } else if (swipeType === 'LIKE') {
          this.feedbackMessage = 'Connection request sent.';
        } else {
          this.feedbackMessage = '';
        }

        this.removeTopCard();
      } catch (error) {
        this.feedbackMessage = error.response?.data?.error || 'Could not process swipe right now.';
      }
    },
    triggerLeftSwipe() {
      this.handleSwipe('left');
    },
    triggerRightSwipe() {
      this.handleSwipe('right');
    },
    removeTopCard() {
      this.profiles.shift();
    },
    openPublicProfile() {
      const userId = this.getSwipedUserId(this.currentProfile);
      if (!userId) return;
      this.$router.push({ name: 'PublicProfile', params: { userId } });
    },
  },
};
</script>

<style scoped>
.discovery-page {
  min-height: 100vh;
  padding: 24px clamp(12px, 2vw, 26px) 170px;
  background: var(--theme-page-background);
  color: var(--theme-text-primary);
}

.match-card {
  border: 1px solid var(--theme-surface-border);
  background: var(--theme-surface-elevated);
  border-radius: 22px;
  box-shadow: var(--theme-shadow-strong);
  padding: clamp(16px, 1.6vw, 22px);
  width: min(980px, 100%);
  margin: 0 auto;
}

.match-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 12px;
}

.identity-row {
  display: flex;
  gap: 12px;
  align-items: center;
}

.profile-picture,
.profile-placeholder {
  width: 82px;
  height: 82px;
  border-radius: 50%;
  object-fit: cover;
}

.profile-placeholder {
  border: 1px solid var(--theme-surface-border);
  background: var(--theme-surface-1);
  color: var(--theme-text-subtle);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 1.8rem;
}

.user-name {
  margin: 0;
  color: var(--theme-heading-color);
  font-size: clamp(1.9rem, 2.8vw, 2.8rem);
  line-height: 1.08;
}

.identity-meta {
  margin-top: 6px;
  color: var(--theme-text-secondary);
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.view-profile-btn {
  border-radius: 11px;
  border: 1px solid var(--theme-button-secondary-border);
  background: var(--theme-button-secondary-bg);
  color: var(--theme-button-secondary-text);
  padding: 10px 12px;
  font-weight: 700;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.identity-facts {
  margin: 10px 0 8px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.fact-chip {
  border: 1px solid var(--theme-surface-border);
  background: var(--theme-surface-1);
  border-radius: 999px;
  padding: 6px 12px;
  color: var(--theme-text-secondary);
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 0.92rem;
  font-weight: 600;
}

.fact-chip i,
.context-item i {
  color: var(--theme-accent);
}

.context-row {
  border: 1px solid var(--theme-surface-border);
  background: var(--theme-surface-1);
  border-radius: 10px;
  padding: 9px 12px;
  margin-bottom: 12px;
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
}

.context-item {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: var(--theme-text-secondary);
  font-weight: 500;
}

.info-section {
  border-top: 1px solid var(--theme-divider);
  margin-top: 12px;
  padding-top: 12px;
}

.compact-info-grid {
  margin-top: 12px;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.compact-info-grid .info-section {
  margin-top: 0;
  border-top: 1px solid var(--theme-divider);
  background: transparent;
  border-radius: 0;
  padding: 10px 0 0;
}

.info-section-wide {
  grid-column: 1 / -1;
}

.info-section h3 {
  margin: 0 0 8px;
  font-size: 1.08rem;
  color: var(--theme-heading-color);
}

.info-section p {
  margin: 0;
  color: var(--theme-text-secondary);
  line-height: 1.55;
}

.chip-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.chip {
  border-radius: 999px;
  border: 1px solid var(--theme-chip-border);
  background: var(--theme-chip-bg);
  color: var(--theme-chip-text);
  padding: 6px 10px;
  font-size: 0.86rem;
  font-weight: 600;
}

.clean-list {
  margin: 0;
  padding-left: 18px;
  color: var(--theme-text-secondary);
}

.clean-list li + li {
  margin-top: 6px;
}

.social-links {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.social-links a {
  color: var(--theme-link);
  text-decoration: none;
  font-weight: 600;
}

.social-links a:hover {
  text-decoration: underline;
}

.swipe-tip {
  margin-top: 14px;
  color: var(--theme-text-secondary);
  text-align: center;
}

.swipe-tip kbd {
  display: inline-block;
  padding: 2px 7px;
  border-radius: 7px;
  border: 1px solid var(--theme-surface-border);
  background: var(--theme-surface-1);
  color: var(--theme-text-primary);
  font-size: 0.88rem;
}

.feedback-message {
  margin-top: 10px;
  text-align: center;
  color: var(--theme-accent);
  font-weight: 600;
}

.floating-match-actions {
  position: fixed;
  left: 50%;
  transform: translateX(-50%);
  bottom: 80px;
  z-index: 900;
  display: flex;
  gap: 10px;
}

.match-action {
  border-radius: 999px;
  padding: 12px 20px;
  min-width: 170px;
  font-size: 1rem;
  font-weight: 800;
  border: 1px solid transparent;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 10px;
  transition: transform 0.16s ease, filter 0.16s ease, box-shadow 0.2s ease;
}

.match-action:hover {
  transform: translateY(-1px);
  filter: brightness(1.02);
}

.skip-button {
  background: var(--theme-button-danger-bg);
  color: var(--theme-button-danger-text);
  border-color: transparent;
  box-shadow: 0 8px 16px color-mix(in srgb, var(--theme-button-danger-bg) 32%, transparent);
}

.connect-button {
  background: var(--theme-accent);
  color: var(--theme-button-primary-text);
  border-color: color-mix(in srgb, var(--theme-accent) 60%, var(--theme-surface-border));
  box-shadow: none;
}

.state-message {
  margin-top: 42px;
  text-align: center;
  color: var(--theme-text-secondary);
}

.error-message {
  color: var(--theme-danger);
}

.empty-state {
  border: 1px solid var(--theme-surface-border);
  border-radius: 18px;
  background: var(--theme-surface-elevated);
  box-shadow: var(--theme-shadow-soft);
  padding: 36px 16px;
  text-align: center;
  color: var(--theme-text-secondary);
}

.empty-state i {
  font-size: 2.2rem;
  margin-bottom: 10px;
  color: var(--theme-accent);
}

html[data-theme='futuristic'] .user-name {
  text-shadow: var(--theme-heading-glow);
}

@media (max-width: 1020px) {
  .compact-info-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 760px) {
  .discovery-page {
    padding: 14px 10px 138px;
  }

  .match-header {
    flex-direction: column;
  }

  .view-profile-btn {
    width: 100%;
    justify-content: center;
  }

  .floating-match-actions {
    width: calc(100vw - 20px);
    bottom: 80px;
    gap: 8px;
  }

  .match-action {
    flex: 1;
    min-width: 0;
    justify-content: center;
    padding: 10px 8px;
    font-size: 0.92rem;
  }
}
</style>
