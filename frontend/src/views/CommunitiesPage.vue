<template>
  <div class="communities-page">
    <div class="page-shell">
      <header class="header-section">
        <div>
          <h2 class="page-title">School & Study Communities</h2>
          <p class="header-subtitle">Build campus-style networks for internships, collaboration, and shared growth.</p>
        </div>
        <button @click="openCreateCommunityModal" class="create-community-button">
          <i class="fas fa-plus-circle"></i>
          Create Your Community
        </button>
      </header>

      <div v-if="loading" class="loading-message">Loading communities...</div>
      <div v-if="error" class="error-message">{{ error }}</div>

      <div v-if="communities.length === 0 && !loading && !error" class="no-communities-message">
        No communities available yet. Be the first to create one.
      </div>

      <div class="community-grid" v-else>
        <article v-for="community in communities" :key="community.id" class="community-card">
          <div class="community-cover-wrap">
            <img
              v-if="community.imageUrl"
              :src="community.imageUrl"
              :alt="`${community.name} cover`"
              class="community-cover"
              @error="handleCommunityImageError($event, community)"
            />
            <div v-else class="community-cover fallback-cover">
              <i class="fas fa-graduation-cap"></i>
              <span>{{ (community.name || 'C').charAt(0).toUpperCase() }}</span>
            </div>
          </div>

          <div class="community-body">
            <h3 class="community-title">{{ community.name }}</h3>
            <p class="community-description">{{ community.description }}</p>

            <div class="community-meta">
              <span class="meta-chip"><i class="fas fa-users"></i> {{ community.memberCount }} members</span>
              <span class="meta-chip"><i class="fas fa-user-shield"></i> Owner: {{ ownerName(community) }}</span>
            </div>

            <div class="community-tags">
              <span v-for="tag in visibleTags(community.tags)" :key="`${community.id}-${tag}`" class="tag-pill">#{{ tag }}</span>
              <span v-if="hiddenTagCount(community.tags) > 0" class="tag-pill tag-overflow">+{{ hiddenTagCount(community.tags) }}</span>
            </div>
          </div>

          <div class="card-actions">
            <button
              v-if="community.owner?.id === currentUserId || isUserMember(community.id)"
              @click="viewCommunity(community.id)"
              class="action-button view-button"
            >
              View Posts
            </button>
            <button
              v-else
              @click="joinCommunity(community.id)"
              class="action-button join-button"
            >
              Join Community
            </button>
          </div>
        </article>
      </div>
    </div>

    <div v-if="showCreateCommunityModal" class="modal-overlay">
      <div class="modal-content">
        <h3>Create New Community</h3>
        <form @submit.prevent="submitCreateCommunity">
          <div class="form-group">
            <label for="communityName">Community Name</label>
            <input type="text" id="communityName" v-model="newCommunity.name" required maxlength="120" />
          </div>

          <div class="form-group">
            <label for="communityDescription">Description</label>
            <textarea id="communityDescription" v-model="newCommunity.description" required maxlength="1200"></textarea>
          </div>

          <div class="form-group">
            <label for="communityTags">Tags (comma-separated)</label>
            <input type="text" id="communityTags" v-model="newCommunityTagsInput" placeholder="study, internship, design" />
          </div>

          <div class="form-group image-group">
            <label>Community Cover (optional)</label>
            <input
              id="communityImage"
              type="file"
              accept="image/png,image/jpeg,image/webp"
              class="file-input"
              @change="handleCommunityImageSelection"
            />
            <div class="image-controls">
              <label for="communityImage" class="action-button secondary-btn">Choose Image</label>
              <button v-if="imagePreviewUrl" type="button" class="action-button ghost-btn" @click="clearCommunityImage">Remove</button>
            </div>
            <p class="helper-text">PNG, JPG, WEBP up to 10MB recommended.</p>
            <img v-if="imagePreviewUrl" :src="imagePreviewUrl" alt="Community cover preview" class="image-preview" />
          </div>

          <div class="modal-actions">
            <button type="submit" class="action-button create-button">Create Community</button>
            <button type="button" @click="closeCreateCommunityModal" class="action-button cancel-button">Cancel</button>
          </div>
        </form>
      </div>
    </div>

    <AppAlert
      ref="appAlert"
      :title="alertTitle"
      :message="alertMessage"
      @closed="showAlert = false"
    />
  </div>
</template>

<script>
import axios from 'axios';
import AppAlert from '@/components/AppAlert.vue';

const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080').replace(/\/+$/, '');
const DEFAULT_COMMUNITY_COVERS = [
  'https://images.pexels.com/photos/1181671/pexels-photo-1181671.jpeg?auto=compress&cs=tinysrgb&w=1600&h=900&fit=crop',
  'https://images.pexels.com/photos/3756678/pexels-photo-3756678.jpeg?auto=compress&cs=tinysrgb&w=1600&h=900&fit=crop',
  'https://images.pexels.com/photos/1181248/pexels-photo-1181248.jpeg?auto=compress&cs=tinysrgb&w=1600&h=900&fit=crop',
  'https://images.pexels.com/photos/3769138/pexels-photo-3769138.jpeg?auto=compress&cs=tinysrgb&w=1600&h=900&fit=crop',
  'https://images.pexels.com/photos/1108099/pexels-photo-1108099.jpeg?auto=compress&cs=tinysrgb&w=1600&h=900&fit=crop',
  'https://images.pexels.com/photos/3183197/pexels-photo-3183197.jpeg?auto=compress&cs=tinysrgb&w=1600&h=900&fit=crop',
  'https://images.pexels.com/photos/3861969/pexels-photo-3861969.jpeg?auto=compress&cs=tinysrgb&w=1600&h=900&fit=crop',
  'https://images.pexels.com/photos/572056/pexels-photo-572056.jpeg?auto=compress&cs=tinysrgb&w=1600&h=900&fit=crop'
];

export default {
  name: 'CommunitiesPage',
  components: {
    AppAlert,
  },
  data() {
    return {
      communities: [],
      loading: true,
      error: null,
      currentUserId: null,
      userJoinedCommunities: [],
      showCreateCommunityModal: false,
      newCommunity: {
        name: '',
        description: '',
        tags: [],
      },
      newCommunityTagsInput: '',
      selectedImageFile: null,
      imagePreviewUrl: '',
      showAlert: false,
      alertTitle: '',
      alertMessage: '',
    };
  },
  async created() {
    await this.fetchCurrentUserId();
    if (this.currentUserId) {
      await this.fetchCommunities();
      await this.fetchUserCommunities();
    }
  },
  watch: {
    newCommunityTagsInput(newVal) {
      this.newCommunity.tags = newVal
        .split(',')
        .map((tag) => tag.trim())
        .filter((tag) => tag.length > 0);
    },
  },
  methods: {
    normalizeMediaUrl(url) {
      if (!url) return '';
      if (url.startsWith('http://') || url.startsWith('https://')) return url;
      return `${API_BASE_URL}${url.startsWith('/') ? url : `/${url}`}`;
    },
    ownerName(community) {
      return community?.owner?.name || community?.owner?.email || 'Unknown';
    },
    hashSeed(value) {
      const text = String(value || 'community');
      let hash = 0;
      for (let index = 0; index < text.length; index += 1) {
        hash = ((hash << 5) - hash) + text.charCodeAt(index);
        hash |= 0;
      }
      return Math.abs(hash);
    },
    defaultCommunityCover(community) {
      const seed = community?.id ?? community?.name ?? 'community';
      const index = this.hashSeed(seed) % DEFAULT_COMMUNITY_COVERS.length;
      return DEFAULT_COMMUNITY_COVERS[index];
    },
    resolveCommunityCover(imageUrl, community) {
      const normalized = this.normalizeMediaUrl(imageUrl);
      if (normalized) {
        return normalized;
      }
      return this.defaultCommunityCover(community);
    },
    handleCommunityImageError(event, community) {
      const fallback = this.defaultCommunityCover(community);
      if (event?.target && event.target.src !== fallback) {
        event.target.src = fallback;
        return;
      }
      community.imageUrl = '';
    },
    visibleTags(tags) {
      return (Array.isArray(tags) ? tags : []).slice(0, 3);
    },
    hiddenTagCount(tags) {
      const total = Array.isArray(tags) ? tags.length : 0;
      return total > 3 ? total - 3 : 0;
    },
    async fetchCurrentUserId() {
      try {
        const token = localStorage.getItem('token');
        const response = await axios.get(`${API_BASE_URL}/api/auth/me`, {
          headers: { Authorization: `Bearer ${token}` },
        });
        this.currentUserId = response.data.id;
      } catch (error) {
        console.error('Error fetching current user ID:', error);
        this.error = 'Could not load user data. Please try logging in again.';
      }
    },
    async fetchCommunities() {
      this.loading = true;
      try {
        const token = localStorage.getItem('token');
        const response = await axios.get(`${API_BASE_URL}/api/communities`, {
          headers: { Authorization: `Bearer ${token}` },
        });

        const communitiesWithCounts = await Promise.all(
          response.data.map(async (community) => {
            const countResponse = await axios.get(
              `${API_BASE_URL}/api/communities/${community.id}/members/count`,
              {
                headers: { Authorization: `Bearer ${token}` },
              }
            );
            return {
              ...community,
              imageUrl: this.resolveCommunityCover(community.imageUrl, community),
              memberCount: countResponse.data,
            };
          })
        );

        this.communities = communitiesWithCounts;
      } catch (error) {
        this.error = 'Failed to load communities.';
        console.error('Error fetching communities:', error);
      } finally {
        this.loading = false;
      }
    },
    async fetchUserCommunities() {
      if (!this.currentUserId) return;
      try {
        const token = localStorage.getItem('token');
        const response = await axios.get(`${API_BASE_URL}/api/communities/my-communities`, {
          headers: { Authorization: `Bearer ${token}` },
        });
        this.userJoinedCommunities = response.data.map((community) => community.id);
      } catch (error) {
        console.error("Error fetching user's communities:", error);
      }
    },
    isUserMember(communityId) {
      return this.userJoinedCommunities.includes(communityId);
    },
    async joinCommunity(communityId) {
      if (!this.currentUserId) {
        this.showAlertMessage('Login Required', 'Please log in to join communities.');
        return;
      }
      try {
        const token = localStorage.getItem('token');
        await axios.post(`${API_BASE_URL}/api/communities/${communityId}/join`, {}, {
          headers: { Authorization: `Bearer ${token}` },
        });
        this.showAlertMessage('Success', 'Successfully joined community.');
        await this.fetchCommunities();
        await this.fetchUserCommunities();
      } catch (error) {
        console.error('Error joining community:', error);
        this.showAlertMessage('Error', `Failed to join community: ${error.response?.data?.error || error.message}`);
      }
    },
    viewCommunity(communityId) {
      this.$router.push({ name: 'CommunityDetail', params: { communityId } });
    },
    openCreateCommunityModal() {
      this.showCreateCommunityModal = true;
      this.newCommunity = { name: '', description: '', tags: [] };
      this.newCommunityTagsInput = '';
      this.selectedImageFile = null;
      this.imagePreviewUrl = '';
    },
    closeCreateCommunityModal() {
      this.showCreateCommunityModal = false;
    },
    handleCommunityImageSelection(event) {
      const file = event.target.files?.[0];
      if (!file) return;

      if (!file.type.startsWith('image/')) {
        this.showAlertMessage('Invalid File', 'Please select an image file.');
        return;
      }

      const tenMb = 10 * 1024 * 1024;
      if (file.size > tenMb) {
        this.showAlertMessage('File Too Large', 'Image should be 10MB or smaller.');
        return;
      }

      this.selectedImageFile = file;
      const reader = new FileReader();
      reader.onload = (e) => {
        this.imagePreviewUrl = e.target?.result || '';
      };
      reader.readAsDataURL(file);
    },
    clearCommunityImage() {
      this.selectedImageFile = null;
      this.imagePreviewUrl = '';
      const input = document.getElementById('communityImage');
      if (input) input.value = '';
    },
    async submitCreateCommunity() {
      if (!this.currentUserId) {
        this.showAlertMessage('Login Required', 'You must be logged in to create a community.');
        return;
      }

      try {
        const token = localStorage.getItem('token');
        const formData = new FormData();
        formData.append('name', this.newCommunity.name.trim());
        formData.append('description', this.newCommunity.description.trim());
        formData.append('tags', JSON.stringify(this.newCommunity.tags || []));

        if (this.selectedImageFile) {
          formData.append('imageFile', this.selectedImageFile);
        }

        await axios.post(`${API_BASE_URL}/api/communities`, formData, {
          headers: {
            Authorization: `Bearer ${token}`,
            'Content-Type': 'multipart/form-data',
          },
        });

        this.showAlertMessage('Success', 'Community created successfully.');
        this.closeCreateCommunityModal();
        await this.fetchCommunities();
      } catch (error) {
        console.error('Error creating community:', error);
        this.showAlertMessage('Error', `Failed to create community: ${error.response?.data?.error || error.message}`);
      }
    },
    showAlertMessage(title, message) {
      this.alertTitle = title;
      this.alertMessage = message;
      this.$refs.appAlert.show();
    },
  },
};
</script>

<style scoped>
.communities-page {
  min-height: 100vh;
  padding: 24px clamp(12px, 2vw, 26px) 110px;
  background: var(--theme-page-background);
  color: var(--theme-text-primary);
}

.header-section {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 16px;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--theme-divider);
}

.page-title {
  margin: 0;
  font-size: clamp(2rem, 4vw, 3.4rem);
  line-height: 1.05;
  color: var(--theme-heading-color);
}

.header-subtitle {
  margin-top: 8px;
  color: var(--theme-text-secondary);
  font-size: 1.04rem;
  max-width: 760px;
}

.create-community-button {
  border: none;
  border-radius: 999px;
  padding: 12px 20px;
  display: inline-flex;
  align-items: center;
  gap: 10px;
  background: var(--theme-button-primary-bg);
  color: var(--theme-button-primary-text);
  font-size: 1.03rem;
  font-weight: 700;
  box-shadow: var(--theme-button-primary-shadow);
  cursor: pointer;
  transition: transform 0.2s ease, filter 0.2s ease;
}

.create-community-button:hover {
  transform: translateY(-1px);
  filter: brightness(1.03);
}

.loading-message,
.error-message,
.no-communities-message {
  text-align: center;
  color: var(--theme-text-secondary);
  margin: 28px 0;
}

.community-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 18px;
  align-items: stretch;
}

.community-card {
  border: 1px solid var(--theme-surface-border);
  border-radius: 18px;
  background: var(--theme-surface-elevated);
  box-shadow: var(--theme-shadow-soft);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  height: 100%;
}

.community-cover-wrap {
  aspect-ratio: 16 / 8;
  background: var(--theme-surface-1);
}

.community-cover {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.fallback-cover {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  font-size: 1.4rem;
  color: var(--theme-accent);
  background:
    radial-gradient(circle at 20% 20%, color-mix(in srgb, var(--theme-accent) 22%, transparent), transparent 40%),
    var(--theme-surface-1);
}

.community-body {
  padding: 14px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.community-title {
  margin: 0;
  font-size: 1.55rem;
  line-height: 1.2;
  color: var(--theme-heading-color);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: calc(1.2em * 2);
}

.community-description {
  margin: 0;
  color: var(--theme-text-secondary);
  line-height: 1.45;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 0;
}

.community-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  min-height: 36px;
  align-items: flex-start;
}

.meta-chip {
  border-radius: 999px;
  padding: 6px 10px;
  border: 1px solid var(--theme-chip-border);
  background: var(--theme-chip-bg);
  color: var(--theme-chip-text);
  font-size: 0.84rem;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.community-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  min-height: 33px;
  align-content: flex-start;
}

.tag-pill {
  border-radius: 999px;
  padding: 5px 9px;
  border: 1px solid var(--theme-chip-border);
  background: var(--theme-chip-bg);
  color: var(--theme-chip-text);
  font-size: 0.78rem;
  font-weight: 600;
  line-height: 1.1;
}

.tag-overflow {
  opacity: 0.9;
}

.card-actions {
  margin-top: auto;
  padding: 0 14px 14px;
}

.action-button {
  width: 100%;
  border: none;
  border-radius: 12px;
  padding: 11px 14px;
  font-size: 0.94rem;
  font-weight: 700;
  cursor: pointer;
  transition: transform 0.2s ease, filter 0.2s ease;
}

.view-button,
.join-button,
.create-button {
  background: var(--theme-button-primary-bg);
  color: var(--theme-button-primary-text);
  box-shadow: var(--theme-button-primary-shadow);
}

.action-button:hover {
  transform: translateY(-1px);
  filter: brightness(1.03);
}

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(2, 6, 23, 0.58);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1200;
  padding: 16px;
}

.modal-content {
  width: min(620px, 96vw);
  max-height: 90vh;
  overflow-y: auto;
  background: var(--theme-surface-elevated);
  border: 1px solid var(--theme-surface-border);
  border-radius: 18px;
  box-shadow: var(--theme-shadow-strong);
  padding: 22px;
}

.modal-content h3 {
  margin: 0 0 16px;
  color: var(--theme-heading-color);
  font-size: 1.5rem;
}

.form-group {
  margin-bottom: 14px;
}

.form-group label {
  display: block;
  margin-bottom: 6px;
  color: var(--theme-text-secondary);
  font-weight: 600;
}

.form-group input,
.form-group textarea {
  width: 100%;
  box-sizing: border-box;
  border: 1px solid var(--theme-input-border);
  border-radius: 12px;
  background: var(--theme-input-bg);
  color: var(--theme-input-text);
  padding: 11px 12px;
  font-size: 0.95rem;
}

.form-group input:focus,
.form-group textarea:focus {
  outline: none;
  border-color: var(--theme-accent);
}

.form-group textarea {
  min-height: 100px;
  resize: vertical;
}

.file-input {
  display: none;
}

.image-controls {
  display: flex;
  gap: 8px;
}

.secondary-btn,
.ghost-btn,
.cancel-button {
  background: var(--theme-button-secondary-bg);
  color: var(--theme-button-secondary-text);
  border: 1px solid var(--theme-button-secondary-border);
}

.secondary-btn,
.ghost-btn {
  width: auto;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.helper-text {
  color: var(--theme-text-subtle);
  margin-top: 6px;
  font-size: 0.82rem;
}

.image-preview {
  width: 100%;
  max-height: 180px;
  object-fit: cover;
  border-radius: 12px;
  border: 1px solid var(--theme-surface-border);
  margin-top: 10px;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 10px;
}

html[data-theme='futuristic'] .page-title {
  text-shadow: var(--theme-heading-glow);
}

@media (max-width: 900px) {
  .header-section {
    flex-direction: column;
    align-items: stretch;
  }

  .create-community-button {
    width: 100%;
    justify-content: center;
  }
}

@media (max-width: 760px) {
  .communities-page {
    padding: 14px 10px 92px;
  }

  .community-grid {
    grid-template-columns: 1fr;
  }

  .community-title {
    font-size: 1.36rem;
  }

  .modal-actions {
    flex-direction: column;
  }

  .modal-actions .action-button {
    width: 100%;
  }
}
</style>
