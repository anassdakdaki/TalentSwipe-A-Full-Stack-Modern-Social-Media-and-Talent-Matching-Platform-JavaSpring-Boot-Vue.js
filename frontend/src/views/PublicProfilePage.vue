<template>
  <div class="public-profile-page">
    <div v-if="loading" class="state-message">Loading profile...</div>
    <div v-else-if="error" class="state-message error-message">{{ error }}</div>

    <div v-else-if="profile" class="profile-shell page-shell">
      <section class="top-card">
        <div class="cover-banner">
          <div v-if="profile.headline || profile.bio" class="banner-copy">
            <p class="banner-eyebrow">About</p>
            <h2 class="banner-title">{{ profile.headline || profile.bio }}</h2>
            <p v-if="profile.headline && profile.bio" class="banner-subtitle">{{ profile.bio }}</p>
          </div>
        </div>
        <div class="header-body">
          <div class="identity-block">
            <img
              v-if="profile.profilePictureUrl"
              :src="profile.profilePictureUrl"
              alt="Profile picture"
              class="hero-avatar"
            />
            <div v-else class="hero-avatar placeholder">
              <i class="fas fa-user"></i>
            </div>

            <div class="hero-info">
              <h1>{{ profile.name }}</h1>
              <p v-if="profile.headline" class="hero-headline">{{ profile.headline }}</p>
              <p class="hero-meta">
                <span v-if="profile.location"><i class="fas fa-map-marker-alt"></i> {{ profile.location }}</span>
                <span v-if="profile.major"><i class="fas fa-briefcase"></i> {{ profile.major }}</span>
                <span v-if="profile.university"><i class="fas fa-graduation-cap"></i> {{ profile.university }}</span>
              </p>
              <p v-if="profile.openToCollaborate" class="open-to-pill">
                <i class="fas fa-handshake"></i> Open to collaborations
              </p>
            </div>
          </div>

          <div class="hero-actions">
            <button class="secondary-button" @click="goBack">Back</button>
            <button
              class="secondary-button"
              :disabled="profile.isOwner || profile.isConnected || connecting"
              @click="connect"
            >
              {{ profile.isConnected ? 'Connected' : connecting ? 'Connecting...' : 'Connect' }}
            </button>
            <button
              class="primary-button"
              :disabled="!profile.canMessage || openingChat"
              @click="messageUser"
            >
              {{ openingChat ? 'Opening...' : 'Message' }}
            </button>
          </div>
        </div>
      </section>

      <p v-if="statusMessage" class="status-message">{{ statusMessage }}</p>

      <div class="content-grid">
        <main class="main-column">
          <section class="section-card">
            <h2>About</h2>
            <p v-if="profile.bio">{{ profile.bio }}</p>
            <p v-else class="muted">No bio added yet.</p>
          </section>

          <section class="section-card" v-if="profile.experiences && profile.experiences.length">
            <h2>Experience</h2>
            <div class="timeline-list">
              <article v-for="(item, index) in visibleExperiences" :key="`${item.title}-${index}`" class="timeline-item">
                <h3>{{ item.title }}</h3>
                <p class="muted">{{ item.organization }} <span v-if="item.location">- {{ item.location }}</span></p>
                <p class="muted">{{ formatRange(item.startDate, item.endDate, item.current) }}</p>
                <p v-if="item.description">{{ item.description }}</p>
              </article>
            </div>
            <button
              v-if="profile.experiences.length > experiencesLimit && !showAllExperiences"
              class="section-toggle-btn"
              type="button"
              @click="showAllExperiences = true"
            >
              Show all {{ profile.experiences.length }} experiences
            </button>
          </section>

          <section class="section-card" v-if="profile.projects && profile.projects.length">
            <h2>Projects</h2>
            <div class="project-grid">
              <article v-for="(item, index) in visibleProjects" :key="`${item.title}-${index}`" class="project-card">
                <img v-if="item.coverImageUrl" :src="item.coverImageUrl" alt="Project cover" class="project-cover" />
                <h3>{{ item.title }}</h3>
                <p v-if="item.shortDescription" class="muted">{{ item.shortDescription }}</p>
                <p v-if="item.description">{{ item.description }}</p>
                <div v-if="item.techStack && item.techStack.length" class="chip-list">
                  <span v-for="tech in item.techStack.slice(0, 6)" :key="tech" class="chip">{{ tech }}</span>
                </div>
                <div class="link-row">
                  <a v-if="item.projectUrl" :href="item.projectUrl" target="_blank" rel="noreferrer">Project</a>
                  <a v-if="item.repoUrl" :href="item.repoUrl" target="_blank" rel="noreferrer">Repository</a>
                </div>
              </article>
            </div>
            <button
              v-if="profile.projects.length > projectsLimit && !showAllProjects"
              class="section-toggle-btn"
              type="button"
              @click="showAllProjects = true"
            >
              Show all {{ profile.projects.length }} projects
            </button>
          </section>

          <section class="section-card" v-if="profile.collaborations && profile.collaborations.length">
            <h2>Collaborations</h2>
            <article
              v-for="(item, index) in visibleCollaborations"
              :key="`${item.title}-${index}`"
              class="timeline-item"
            >
              <h3>{{ item.title }}</h3>
              <p class="muted">
                {{ item.partnerName || 'Partner' }}
                <span v-if="item.collaborationType">- {{ item.collaborationType }}</span>
              </p>
              <p class="muted">{{ formatRange(item.startDate, item.endDate, false) }}</p>
              <p v-if="item.description">{{ item.description }}</p>
              <p v-if="item.resultSummary" class="muted">{{ item.resultSummary }}</p>
              <a v-if="item.referenceUrl" :href="item.referenceUrl" target="_blank" rel="noreferrer">Reference</a>
            </article>
            <button
              v-if="profile.collaborations.length > collaborationsLimit && !showAllCollaborations"
              class="section-toggle-btn"
              type="button"
              @click="showAllCollaborations = true"
            >
              Show all {{ profile.collaborations.length }} collaborations
            </button>
          </section>

          <section class="section-card" v-if="profile.recentActivity && profile.recentActivity.length">
            <h2>Activity</h2>
            <div class="activity-list">
              <div v-for="(item, index) in visibleRecentActivity" :key="`${item.type}-${item.referenceId}-${index}`" class="activity-item">
                <span>{{ item.title }}</span>
                <small class="muted">{{ formatDateTime(item.timestamp) }}</small>
              </div>
            </div>
            <button
              v-if="profile.recentActivity.length > activityLimit && !showAllActivity"
              class="section-toggle-btn"
              type="button"
              @click="showAllActivity = true"
            >
              Show all {{ profile.recentActivity.length }} activities
            </button>
          </section>
        </main>

        <aside class="side-column">
          <section v-if="profile.stats" class="section-card">
            <h2>Analytics</h2>
            <div class="stats-grid">
              <div class="stat-item"><strong>{{ profile.stats.postsCount }}</strong><span>Posts</span></div>
              <div class="stat-item"><strong>{{ profile.stats.commentsCount }}</strong><span>Comments</span></div>
              <div class="stat-item"><strong>{{ profile.stats.communitiesJoinedCount }}</strong><span>Joined</span></div>
              <div class="stat-item"><strong>{{ profile.stats.communitiesOwnedCount }}</strong><span>Owned</span></div>
              <div class="stat-item"><strong>{{ profile.stats.matchesCount }}</strong><span>Matches</span></div>
            </div>
          </section>

          <section class="section-card" v-if="hasLookingFor || (profile.interests && profile.interests.length) || (profile.languages && profile.languages.length) || hasSocialLinks">
            <h2>Details</h2>

            <div v-if="hasLookingFor" class="subsection">
              <h3>Looking For</h3>
              <div class="chip-list">
                <span v-if="profile.lookingFor.studyPartner" class="chip">Study Partner</span>
                <span v-if="profile.lookingFor.languageExchange" class="chip">Language Exchange</span>
                <span v-if="profile.lookingFor.friendship" class="chip">Friendship</span>
                <span v-if="profile.lookingFor.networking" class="chip">Networking</span>
                <span v-if="profile.lookingFor.community" class="chip">Community</span>
              </div>
            </div>

            <div v-if="profile.interests && profile.interests.length" class="subsection">
              <h3>Interests</h3>
              <div class="chip-list">
                <span v-for="interest in visibleInterests" :key="interest" class="chip">#{{ interest }}</span>
              </div>
              <button
                v-if="profile.interests.length > interestsLimit && !showAllInterests"
                class="section-toggle-btn"
                type="button"
                @click="showAllInterests = true"
              >
                Show all {{ profile.interests.length }} interests
              </button>
            </div>

            <div v-if="profile.languages && profile.languages.length" class="subsection">
              <h3>Languages</h3>
              <ul class="simple-list">
                <li v-for="(item, index) in visibleLanguages" :key="`${item.name}-${index}`">
                  {{ item.name }} ({{ item.level }})
                </li>
              </ul>
              <button
                v-if="profile.languages.length > languagesLimit && !showAllLanguages"
                class="section-toggle-btn"
                type="button"
                @click="showAllLanguages = true"
              >
                Show all {{ profile.languages.length }} languages
              </button>
            </div>

            <div v-if="hasSocialLinks" class="subsection">
              <h3>Social Links</h3>
              <div class="link-row">
                <a v-if="profile.socialLinks.github" :href="profile.socialLinks.github" target="_blank" rel="noreferrer">GitHub</a>
                <a v-if="profile.socialLinks.linkedin" :href="profile.socialLinks.linkedin" target="_blank" rel="noreferrer">LinkedIn</a>
                <a v-if="profile.socialLinks.instagram" :href="profile.socialLinks.instagram" target="_blank" rel="noreferrer">Instagram</a>
              </div>
            </div>
          </section>

          <section v-if="profile.communities && profile.communities.length" class="section-card">
            <h2>Communities</h2>
            <div class="community-list">
              <div v-for="community in visibleCommunities" :key="community.id" class="community-item">
                <span>{{ community.name }}</span>
                <span class="muted">{{ community.role }}</span>
              </div>
            </div>
            <button
              v-if="profile.communities.length > communitiesLimit && !showAllCommunities"
              class="section-toggle-btn"
              type="button"
              @click="showAllCommunities = true"
            >
              Show all {{ profile.communities.length }} communities
            </button>
          </section>
        </aside>
      </div>
    </div>
  </div>
</template>

<script>
import {
  findOrCreateChatRoom,
  getCurrentUser,
  getMatchStatus,
  getPublicProfile,
  normalizeProfileImageUrl,
  swipeLike
} from '@/utils/profileApi'

export default {
  name: 'PublicProfilePage',
  props: {
    userId: {
      type: [String, Number],
      required: true
    }
  },
  data() {
    return {
      profile: null,
      loading: false,
      error: '',
      statusMessage: '',
      currentUserId: null,
      matchId: null,
      connecting: false,
      openingChat: false,
      showAllExperiences: false,
      showAllProjects: false,
      showAllCollaborations: false,
      showAllCommunities: false,
      showAllActivity: false,
      showAllInterests: false,
      showAllLanguages: false,
      experiencesLimit: 3,
      projectsLimit: 2,
      collaborationsLimit: 2,
      communitiesLimit: 5,
      activityLimit: 6,
      interestsLimit: 8,
      languagesLimit: 5
    }
  },
  computed: {
    numericUserId() {
      return Number(this.userId)
    },
    hasSocialLinks() {
      return !!(this.profile?.socialLinks?.github || this.profile?.socialLinks?.linkedin || this.profile?.socialLinks?.instagram)
    },
    hasLookingFor() {
      const lookingFor = this.profile?.lookingFor || {}
      return Object.values(lookingFor).some(Boolean)
    },
    hasProfessionalData() {
      return Boolean(
        (this.profile?.experiences && this.profile.experiences.length) ||
        (this.profile?.projects && this.profile.projects.length) ||
        (this.profile?.collaborations && this.profile.collaborations.length)
      )
    },
    visibleExperiences() {
      const list = this.profile?.experiences || []
      return this.showAllExperiences ? list : list.slice(0, this.experiencesLimit)
    },
    visibleProjects() {
      const list = this.profile?.projects || []
      return this.showAllProjects ? list : list.slice(0, this.projectsLimit)
    },
    visibleCollaborations() {
      const list = this.profile?.collaborations || []
      return this.showAllCollaborations ? list : list.slice(0, this.collaborationsLimit)
    },
    visibleCommunities() {
      const list = this.profile?.communities || []
      return this.showAllCommunities ? list : list.slice(0, this.communitiesLimit)
    },
    visibleRecentActivity() {
      const list = this.profile?.recentActivity || []
      return this.showAllActivity ? list : list.slice(0, this.activityLimit)
    },
    visibleInterests() {
      const list = this.profile?.interests || []
      return this.showAllInterests ? list : list.slice(0, this.interestsLimit)
    },
    visibleLanguages() {
      const list = this.profile?.languages || []
      return this.showAllLanguages ? list : list.slice(0, this.languagesLimit)
    }
  },
  watch: {
    userId: {
      immediate: true,
      handler() {
        this.loadProfile()
      }
    }
  },
  methods: {
    async loadProfile() {
      if (!this.numericUserId) {
        this.error = 'Invalid user profile.'
        return
      }

      this.loading = true
      this.error = ''
      this.statusMessage = ''
      this.matchId = null
      this.showAllExperiences = false
      this.showAllProjects = false
      this.showAllCollaborations = false
      this.showAllCommunities = false
      this.showAllActivity = false
      this.showAllInterests = false
      this.showAllLanguages = false

      try {
        const [profileResult, currentUserResult] = await Promise.allSettled([
          getPublicProfile(this.numericUserId),
          getCurrentUser()
        ])

        if (profileResult.status === 'rejected') {
          this.error = profileResult.reason?.response?.data?.error || 'Could not load profile.'
          return
        }

        if (currentUserResult.status === 'fulfilled') {
          this.currentUserId = currentUserResult.value.data?.id || null
        } else {
          this.currentUserId = null
        }

        const profile = profileResult.value.data
        profile.profilePictureUrl = normalizeProfileImageUrl(profile.profilePictureUrl)
        profile.projects = (profile.projects || []).map(item => ({
          ...item,
          coverImageUrl: normalizeProfileImageUrl(item.coverImageUrl)
        }))

        this.profile = profile
        await this.refreshMatchStatus()
      } catch (err) {
        this.error = err?.response?.data?.error || 'Could not load profile.'
      } finally {
        this.loading = false
      }
    },
    async refreshMatchStatus() {
      if (!this.profile || this.profile.isOwner) {
        this.matchId = null
        return
      }
      try {
        const response = await getMatchStatus(this.numericUserId)
        const matched = Boolean(response.data?.matched)
        this.matchId = matched ? response.data?.matchId || null : null
        this.profile.isConnected = matched
        this.profile.canMessage = matched && !this.profile.isOwner
      } catch (error) {
        this.matchId = null
      }
    },
    async connect() {
      if (!this.profile || this.profile.isOwner || this.profile.isConnected) {
        return
      }
      this.connecting = true
      this.statusMessage = ''
      try {
        const response = await swipeLike(this.numericUserId)
        await this.refreshMatchStatus()
        this.statusMessage = response.data?.match
          ? 'It is a match. You can message now.'
          : 'Connection request sent.'
      } catch (error) {
        this.statusMessage = error?.response?.data?.error || 'Could not send connection request.'
      } finally {
        this.connecting = false
      }
    },
    async messageUser() {
      if (!this.profile || this.profile.isOwner) {
        return
      }
      this.openingChat = true
      this.statusMessage = ''
      try {
        if (!this.currentUserId) {
          const me = await getCurrentUser()
          this.currentUserId = me.data?.id || null
        }
        if (!this.matchId) {
          await this.refreshMatchStatus()
        }
        if (!this.matchId) {
          this.statusMessage = 'You can message this user after a mutual match.'
          return
        }

        const response = await findOrCreateChatRoom({
          user1Id: this.currentUserId,
          user2Id: this.numericUserId,
          studyMatchId: this.matchId
        })
        this.$router.push({ name: 'Chat', params: { chatRoomId: response.data.id } })
      } catch (error) {
        this.statusMessage = error?.response?.data?.error || 'Unable to open chat.'
      } finally {
        this.openingChat = false
      }
    },
    goBack() {
      this.$router.back()
    },
    formatRange(startDate, endDate, isCurrent) {
      const start = startDate ? this.formatDate(startDate) : 'Start'
      const end = isCurrent ? 'Present' : endDate ? this.formatDate(endDate) : 'Present'
      return `${start} - ${end}`
    },
    formatDate(rawDate) {
      try {
        return new Date(rawDate).toLocaleDateString()
      } catch {
        return rawDate
      }
    },
    formatDateTime(rawDate) {
      try {
        return new Date(rawDate).toLocaleString()
      } catch {
        return rawDate
      }
    }
  }
}
</script>

<style scoped>
.public-profile-page {
  min-height: 100vh;
  width: 100%;
  padding: 22px clamp(14px, 2.4vw, 30px) 110px;
  background: var(--theme-page-background);
  color: var(--theme-text-primary);
  font-family: var(--theme-font-body);
}

.profile-shell {
  width: min(var(--page-max-width), 100%);
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.top-card,
.section-card {
  border: 1px solid var(--theme-surface-border);
  background: var(--theme-surface-elevated);
  box-shadow: var(--theme-shadow-soft);
  border-radius: 16px;
}

.top-card {
  overflow: hidden;
}

.cover-banner {
  height: 168px;
  position: relative;
  background:
    linear-gradient(135deg, color-mix(in srgb, var(--theme-accent) 24%, transparent), transparent 60%),
    linear-gradient(140deg, var(--theme-surface-2), var(--theme-surface-1));
  border-bottom: 1px solid var(--theme-surface-border);
}

.banner-copy {
  position: absolute;
  left: 18px;
  bottom: 14px;
  max-width: min(760px, calc(100% - 36px));
  padding: 10px 14px;
  border-radius: 8px;
  border: 1px solid color-mix(in srgb, var(--theme-accent) 28%, transparent);
  background: color-mix(in srgb, var(--theme-surface-elevated) 70%, var(--theme-accent) 30%);
}

.banner-eyebrow {
  margin: 0 0 4px;
  font-size: 0.72rem;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: var(--theme-text-primary);
  opacity: 0.85;
  font-weight: 700;
}

.banner-title {
  margin: 0;
  color: var(--theme-text-primary);
  font-size: clamp(1rem, 1.6vw, 1.36rem);
  font-weight: 800;
  line-height: 1.2;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.banner-subtitle {
  margin: 4px 0 0;
  color: var(--theme-text-primary);
  opacity: 0.88;
  font-size: 0.8rem;
  line-height: 1.3;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.header-body {
  padding: 0 20px 18px;
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
}

.identity-block {
  display: flex;
  align-items: flex-end;
  gap: 16px;
  margin-top: -50px;
  min-width: 0;
}

.hero-avatar {
  width: 112px;
  height: 112px;
  border-radius: 50%;
  object-fit: cover;
  border: 4px solid var(--theme-surface-elevated);
  box-shadow: var(--theme-shadow-soft);
  flex-shrink: 0;
}

.hero-avatar.placeholder {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: var(--theme-surface-1);
  color: var(--theme-text-subtle);
  font-size: 2.2rem;
}

.hero-info h1 {
  margin: 0 0 4px;
  color: var(--theme-heading-color);
  font-family: var(--theme-font-heading);
  font-size: clamp(1.65rem, 2.4vw, 2.25rem);
  line-height: 1.2;
}

.hero-headline {
  margin: 0 0 6px;
  color: var(--theme-text-secondary);
  font-size: 1rem;
}

.hero-meta {
  margin: 0;
  display: flex;
  flex-wrap: wrap;
  gap: 10px 14px;
  color: var(--theme-text-secondary);
  font-size: 0.95rem;
}

.hero-meta i {
  margin-right: 5px;
  color: var(--theme-accent);
}

.open-to-pill {
  margin: 10px 0 0;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  border: 1px solid var(--theme-chip-border);
  background: var(--theme-chip-bg);
  color: var(--theme-chip-text);
  border-radius: 999px;
  padding: 6px 11px;
  font-size: 0.86rem;
  font-weight: 600;
}

.hero-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.primary-button,
.secondary-button {
  border-radius: 10px;
  padding: 10px 16px;
  font-weight: 700;
  cursor: pointer;
  transition: transform 0.2s ease, filter 0.2s ease;
}

.primary-button {
  border: none;
  color: var(--theme-button-primary-text);
  background: var(--theme-button-primary-bg);
  box-shadow: var(--theme-button-primary-shadow);
}

.secondary-button {
  border: 1px solid var(--theme-button-secondary-border);
  color: var(--theme-button-secondary-text);
  background: var(--theme-button-secondary-bg);
}

.primary-button:hover:not(:disabled),
.secondary-button:hover:not(:disabled) {
  transform: translateY(-1px);
  filter: brightness(1.03);
}

button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.section-card h2 {
  margin: 0 0 10px;
  font-family: var(--theme-font-heading);
  color: var(--theme-heading-color);
  font-size: 1.24rem;
}

.section-card h3 {
  margin: 0 0 6px;
  font-size: 1rem;
}

.section-card {
  padding: 16px;
  height: fit-content;
}

.subsection {
  margin-top: 12px;
  padding-top: 10px;
  border-top: 1px solid var(--theme-divider);
}

.subsection h3 {
  margin: 0 0 8px;
}

.content-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 320px;
  gap: 14px;
  align-items: start;
}

.main-column,
.side-column {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.section-toggle-btn {
  margin-top: 8px;
  border: 1px solid var(--theme-button-secondary-border);
  background: var(--theme-button-secondary-bg);
  color: var(--theme-button-secondary-text);
  border-radius: 10px;
  padding: 8px 12px;
  font-size: 0.86rem;
  font-weight: 700;
  cursor: pointer;
}

.muted {
  color: var(--theme-text-secondary);
}

.chip-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.chip {
  padding: 6px 10px;
  border-radius: 999px;
  border: 1px solid var(--theme-chip-border);
  background: var(--theme-chip-bg);
  color: var(--theme-chip-text);
  font-size: 0.88rem;
}

.timeline-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.timeline-item {
  border: 1px solid var(--theme-surface-border);
  background: var(--theme-surface-1);
  border-radius: 12px;
  padding: 12px;
}

.project-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(230px, 1fr));
  gap: 12px;
}

.project-card {
  border: 1px solid var(--theme-surface-border);
  background: var(--theme-surface-1);
  border-radius: 12px;
  padding: 12px;
}

.project-cover {
  width: 100%;
  max-height: 160px;
  object-fit: cover;
  border-radius: 10px;
  margin-bottom: 10px;
}

.community-list,
.activity-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.community-item,
.activity-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
  border: 1px solid var(--theme-surface-border);
  background: var(--theme-surface-1);
  border-radius: 10px;
  padding: 10px 12px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;
}

.stat-item {
  border: 1px solid var(--theme-surface-border);
  background: var(--theme-surface-1);
  border-radius: 10px;
  padding: 10px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-item strong {
  font-size: 1.2rem;
}

.stat-item span {
  color: var(--theme-text-secondary);
  font-size: 0.86rem;
}

.simple-list {
  margin: 0;
  padding-left: 18px;
}

.simple-list li + li {
  margin-top: 6px;
}

.link-row {
  margin-top: 8px;
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.link-row a {
  color: var(--theme-link);
  text-decoration: none;
}

.link-row a:hover {
  text-decoration: underline;
}

.state-message {
  margin-top: 48px;
  text-align: center;
  color: var(--theme-text-secondary);
  font-size: 1.1rem;
}

.error-message {
  color: var(--theme-danger);
}

.status-message {
  margin: 0;
  color: var(--theme-accent);
}

@media (max-width: 1080px) {
  .content-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .public-profile-page {
    padding: 14px 10px 90px;
  }

  .cover-banner {
    height: 112px;
  }

  .banner-copy {
    right: 10px;
    left: 10px;
    bottom: 8px;
    max-width: none;
    padding: 8px 10px;
  }

  .banner-title {
    font-size: 0.92rem;
  }

  .banner-subtitle {
    font-size: 0.76rem;
  }

  .header-body {
    padding: 0 12px 14px;
    flex-direction: column;
    align-items: stretch;
  }

  .identity-block {
    margin-top: -36px;
    align-items: center;
  }

  .hero-avatar {
    width: 88px;
    height: 88px;
  }

  .hero-actions {
    display: grid;
    grid-template-columns: 1fr;
  }

  .project-grid {
    grid-template-columns: 1fr;
  }
}
</style>
