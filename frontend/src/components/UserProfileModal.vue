<template>
  <div v-if="visible" class="user-profile-modal-overlay" @click.self="closeModal">
    <div class="user-profile-modal">
      <button class="profile-close-button" @click="closeModal" aria-label="Close profile">&times;</button>

      <div v-if="loading" class="profile-modal-state">Loading profile...</div>
      <div v-else-if="error" class="profile-modal-state error-message">{{ error }}</div>

      <div v-else-if="profile" class="profile-content">
        <div class="profile-badge-row">
          <img v-if="profile.profilePictureUrl" :src="profile.profilePictureUrl" alt="Profile picture" class="profile-photo" />
          <div v-else class="profile-photo-placeholder">
            <i class="fas fa-user"></i>
          </div>

          <div class="profile-intro">
            <h3 class="profile-name">{{ profile.name || 'Community Member' }}</h3>
            <p v-if="profile.headline" class="meta-line">{{ profile.headline }}</p>
            <p v-if="profile.location" class="meta-line">
              <i class="fas fa-map-marker-alt"></i>
              {{ profile.location }}
            </p>
            <p v-if="profile.university || profile.major" class="meta-line">
              <i class="fas fa-graduation-cap"></i>
              {{ profile.major || '' }} <span v-if="profile.major && profile.university">-</span> {{ profile.university || '' }}
            </p>
          </div>
        </div>

        <section v-if="profile.bio" class="profile-section">
          <h4>About</h4>
          <p>{{ profile.bio }}</p>
        </section>

        <section v-if="profile.topInterests && profile.topInterests.length" class="profile-section">
          <h4>Top Interests</h4>
          <div class="tag-list">
            <span v-for="interest in profile.topInterests" :key="interest" class="chip">#{{ interest }}</span>
          </div>
        </section>

        <section v-if="profile.stats" class="profile-section">
          <h4>Activity Snapshot</h4>
          <div class="stats-grid">
            <div class="stat"><strong>{{ profile.stats.postsCount }}</strong><span>Posts</span></div>
            <div class="stat"><strong>{{ profile.stats.commentsCount }}</strong><span>Comments</span></div>
            <div class="stat"><strong>{{ profile.stats.communitiesJoinedCount }}</strong><span>Joined</span></div>
            <div class="stat"><strong>{{ profile.stats.matchesCount }}</strong><span>Matches</span></div>
          </div>
        </section>

        <p v-if="infoMessage" class="profile-info-message">{{ infoMessage }}</p>

        <div class="profile-actions">
          <button class="action-button secondary-button" @click="viewFullProfile">
            <i class="fas fa-id-card"></i>
            View Full Profile
          </button>
          <button class="action-button message-button" :disabled="!canMessage" @click="requestChat">
            <i :class="['fas', canMessage ? 'fa-paper-plane' : 'fa-lock']"></i>
            {{ canMessage ? 'Message' : 'Connect to message' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { getMatchStatus, getProfileSummary, normalizeProfileImageUrl } from '@/utils/profileApi'

export default {
  name: 'UserProfileModal',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    userId: {
      type: [Number, String],
      default: null
    },
    currentUserId: {
      type: [Number, String],
      default: null
    }
  },
  emits: ['close', 'open-chat-requested'],
  data() {
    return {
      profile: null,
      loading: false,
      error: '',
      matchStatus: {
        matched: false,
        matchId: null
      },
      infoMessage: ''
    }
  },
  computed: {
    selectedUserId() {
      return this.userId ? Number(this.userId) : null
    },
    isSelf() {
      return !!(this.currentUserId && this.selectedUserId && Number(this.currentUserId) === this.selectedUserId)
    },
    canMessage() {
      return Boolean(this.matchStatus.matched && this.matchStatus.matchId && !this.isSelf)
    }
  },
  watch: {
    visible(nextVisible) {
      if (!nextVisible) {
        this.reset()
        return
      }
      if (this.selectedUserId) {
        this.fetchProfile()
      }
    },
    userId(newUserId, oldUserId) {
      if (!this.visible || newUserId === oldUserId) return
      if (newUserId) {
        this.fetchProfile()
      } else {
        this.profile = null
      }
    }
  },
  methods: {
    closeModal() {
      this.$emit('close')
    },
    async fetchProfile() {
      if (!this.selectedUserId) return

      this.loading = true
      this.error = ''
      this.matchStatus = { matched: false, matchId: null }
      this.infoMessage = ''

      try {
        const [profileResult, matchResult] = await Promise.allSettled([
          getProfileSummary(this.selectedUserId),
          this.currentUserId ? getMatchStatus(this.selectedUserId) : Promise.resolve({ data: { matched: false, matchId: null } })
        ])

        if (profileResult.status === 'rejected') {
          this.error = profileResult.reason?.response?.data?.error || profileResult.reason?.message || 'Could not load profile.'
          return
        }

        this.profile = profileResult.value.data || null
        if (this.profile?.profilePictureUrl) {
          this.profile.profilePictureUrl = normalizeProfileImageUrl(this.profile.profilePictureUrl)
        }

        if (matchResult.status === 'fulfilled') {
          this.matchStatus = {
            matched: Boolean(matchResult.value.data?.matched),
            matchId: matchResult.value.data?.matchId || null
          }
        } else {
          this.matchStatus = { matched: false, matchId: null }
        }
      } catch (err) {
        this.error = err.response?.data?.error || err.message || 'Could not load profile.'
      } finally {
        this.loading = false
      }
    },
    viewFullProfile() {
      if (!this.selectedUserId) {
        return
      }
      this.closeModal()
      this.$router.push({ name: 'PublicProfile', params: { userId: this.selectedUserId } })
    },
    requestChat() {
      this.infoMessage = ''
      if (this.isSelf) {
        this.infoMessage = 'You can not message yourself.'
        return
      }
      if (!this.canMessage) {
        this.infoMessage = 'You can message this person only after a mutual match.'
        return
      }
      if (!this.selectedUserId || !this.matchStatus.matchId) return

      this.$emit('open-chat-requested', {
        userId: this.selectedUserId,
        matchId: this.matchStatus.matchId
      })
    },
    reset() {
      this.profile = null
      this.loading = false
      this.error = ''
      this.infoMessage = ''
      this.matchStatus = { matched: false, matchId: null }
    }
  }
}
</script>

<style scoped>
.user-profile-modal-overlay {
  position: fixed;
  inset: 0;
  z-index: 1200;
  background: rgba(5, 8, 20, 0.72);
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 16px;
}

.user-profile-modal {
  width: min(960px, 96vw);
  max-height: 90vh;
  overflow-y: auto;
  background: var(--theme-surface-elevated);
  border: 1px solid var(--theme-surface-border);
  border-radius: 20px;
  box-shadow: var(--theme-shadow-strong);
  color: var(--theme-text-primary);
  padding: 22px;
  position: relative;
}

.profile-content {
  display: grid;
  grid-template-columns: minmax(0, 1.35fr) minmax(0, 1fr);
  gap: 12px;
  align-items: start;
}

.profile-close-button {
  position: absolute;
  top: 12px;
  right: 12px;
  width: 34px;
  height: 34px;
  border-radius: 999px;
  border: 1px solid var(--theme-surface-border);
  color: var(--theme-text-primary);
  background: var(--theme-button-secondary-bg);
  cursor: pointer;
}

.profile-modal-state {
  min-height: 220px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--theme-text-secondary);
}

.error-message {
  color: var(--theme-danger);
}

.profile-badge-row {
  display: flex;
  gap: 16px;
  align-items: center;
  grid-column: 1 / -1;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--theme-divider);
}

.profile-photo,
.profile-photo-placeholder {
  width: 82px;
  height: 82px;
  border-radius: 50%;
  object-fit: cover;
}

.profile-photo-placeholder {
  border: 1px solid var(--theme-surface-border);
  background: var(--theme-surface-1);
  color: var(--theme-text-subtle);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2rem;
}

.profile-intro {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 0;
}

.profile-name {
  margin: 0;
  font-size: 1.8rem;
  color: var(--theme-heading-color);
  text-shadow: var(--theme-heading-glow);
  word-break: break-word;
}

.meta-line {
  margin: 0;
  color: var(--theme-text-secondary);
  display: flex;
  align-items: center;
  gap: 7px;
}

.profile-section {
  margin-top: 0;
  border: 1px solid var(--theme-surface-border);
  background: var(--theme-surface-1);
  border-radius: 12px;
  padding: 12px;
}

.profile-section h4 {
  margin: 0 0 10px;
  color: var(--theme-heading-color);
  font-size: 1.2rem;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.chip {
  padding: 6px 12px;
  border-radius: 999px;
  border: 1px solid var(--theme-chip-border);
  background: var(--theme-chip-bg);
  color: var(--theme-chip-text);
  font-size: 0.9rem;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 8px;
}

.stat {
  border: 1px solid var(--theme-surface-border);
  background: var(--theme-surface-1);
  border-radius: 10px;
  padding: 10px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat span {
  font-size: 0.8rem;
  color: var(--theme-text-secondary);
}

.profile-info-message {
  margin-top: 0;
  color: var(--theme-warning);
  grid-column: 1 / -1;
}

.profile-actions {
  margin-top: 0;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  grid-column: 1 / -1;
}

.action-button {
  border-radius: 999px;
  padding: 10px 16px;
  font-size: 0.96rem;
  font-weight: 700;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.secondary-button {
  border: 1px solid var(--theme-button-secondary-border);
  background: var(--theme-button-secondary-bg);
  color: var(--theme-button-secondary-text);
}

.message-button {
  border: 1px solid var(--theme-button-secondary-border);
  background: var(--theme-button-primary-bg);
  color: var(--theme-button-primary-text);
  box-shadow: var(--theme-button-primary-shadow);
}

.action-button:disabled {
  cursor: not-allowed;
  opacity: 0.65;
}

@media (max-width: 768px) {
  .user-profile-modal {
    padding: 16px;
  }

  .profile-content {
    grid-template-columns: 1fr;
  }

  .profile-name {
    font-size: 1.4rem;
  }
}
</style>
