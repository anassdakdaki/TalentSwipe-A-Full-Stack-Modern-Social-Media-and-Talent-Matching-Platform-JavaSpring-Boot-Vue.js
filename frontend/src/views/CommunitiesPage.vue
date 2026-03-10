<template>
  <div class="communities-page">
    <div class="page-shell">
      <header class="page-header">
        <h1 class="page-title">Communities</h1>
        <p class="page-subtitle">Find active circles, join discussions, and build your network.</p>
      </header>

      <div class="communities-layout">
        <main class="communities-main">
          <section class="toolbar-card">
            <div class="search-box">
              <i class="fas fa-search"></i>
              <input
                v-model.trim="searchQuery"
                type="search"
                placeholder="Search communities..."
                aria-label="Search communities"
              />
            </div>

            <div class="toolbar-controls">
              <select v-model="sortBy" class="sort-select" aria-label="Sort communities">
                <option value="popular">Most members</option>
                <option value="newest">Newest first</option>
                <option value="name">Name A-Z</option>
              </select>

              <div class="filter-group">
                <button
                  type="button"
                  class="filter-pill"
                  :class="{ active: filterMode === 'all' }"
                  @click="filterMode = 'all'"
                >
                  All
                </button>
                <button
                  type="button"
                  class="filter-pill"
                  :class="{ active: filterMode === 'joined' }"
                  @click="filterMode = 'joined'"
                >
                  Joined
                </button>
                <button
                  type="button"
                  class="filter-pill"
                  :class="{ active: filterMode === 'owned' }"
                  @click="filterMode = 'owned'"
                >
                  Owned
                </button>
              </div>
            </div>
          </section>

          <p v-if="loading" class="state-message">Loading communities...</p>
          <p v-else-if="error" class="state-message error-message">{{ error }}</p>
          <p v-else-if="displayedCommunities.length === 0" class="state-message">
            No communities match your filters.
          </p>

          <section v-else class="community-list">
            <article
              v-for="community in displayedCommunities"
              :key="community.id"
              class="community-row"
            >
              <div class="row-vote">
                <i class="fas fa-users"></i>
                <strong>{{ community.memberCount }}</strong>
                <small>members</small>
              </div>

              <button type="button" class="row-content" @click="viewCommunity(community.id)">
                <p class="row-meta">
                  <span>Owner: {{ ownerName(community) }}</span>
                  <span
                    v-if="community.owner?.id === currentUserId || isUserMember(community.id)"
                    class="joined-pill"
                  >
                    <i class="fas fa-check-circle"></i>
                    Joined
                  </span>
                </p>
                <h3 class="community-title">{{ community.name }}</h3>
                <p class="community-description">{{ community.description }}</p>
                <div class="tags-row" v-if="community.tags && community.tags.length">
                  <span
                    v-for="tag in visibleTags(community.tags)"
                    :key="`${community.id}-${tag}`"
                    class="tag-pill"
                  >
                    {{ tag }}
                  </span>
                  <span v-if="hiddenTagCount(community.tags) > 0" class="tag-pill tag-overflow">
                    +{{ hiddenTagCount(community.tags) }}
                  </span>
                </div>
              </button>

              <div class="row-side">
                <img
                  v-if="community.imageUrl"
                  :src="community.imageUrl"
                  :alt="`${community.name} cover`"
                  class="row-cover"
                  @error="handleCommunityImageError($event, community)"
                />
                <div v-else class="row-cover cover-fallback">
                  <i class="fas fa-graduation-cap"></i>
                </div>

                <button
                  v-if="community.owner?.id === currentUserId || isUserMember(community.id)"
                  type="button"
                  class="action-button view-button"
                  @click="viewCommunity(community.id)"
                >
                  View
                </button>
                <button
                  v-else
                  type="button"
                  class="action-button join-button"
                  @click="joinCommunity(community.id)"
                >
                  Join
                </button>
              </div>
            </article>
          </section>
        </main>

        <aside class="communities-side">
          <section class="side-card create-card">
            <h3>Create Community</h3>
            <p>Start your own focused group for study, career, and collaboration.</p>
            <button @click="openCreateCommunityModal" class="create-community-button">
              <i class="fas fa-plus-circle"></i>
              Create Community
            </button>
          </section>

          <section class="side-card">
            <h3>Community Snapshot</h3>
            <div class="snapshot-grid">
              <div class="snapshot-item">
                <strong>{{ communities.length }}</strong>
                <span>Total</span>
              </div>
              <div class="snapshot-item">
                <strong>{{ userJoinedCommunities.length }}</strong>
                <span>Joined</span>
              </div>
              <div class="snapshot-item">
                <strong>{{ ownedCount }}</strong>
                <span>Owned</span>
              </div>
            </div>
          </section>

          <section class="side-card" v-if="topTags.length">
            <h3>Top Topics</h3>
            <div class="top-tags">
              <button
                v-for="tag in topTags"
                :key="tag"
                type="button"
                class="tag-pill side-tag"
                @click="searchQuery = tag"
              >
                {{ tag }}
              </button>
            </div>
          </section>
        </aside>
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
      searchQuery: '',
      sortBy: 'popular',
      filterMode: 'all',
    };
  },
  computed: {
    filteredCommunities() {
      const query = this.searchQuery.toLowerCase();
      return this.communities.filter((community) => {
        const matchesSearch =
          !query ||
          (community.name || '').toLowerCase().includes(query) ||
          (community.description || '').toLowerCase().includes(query) ||
          (community.tags || []).some((tag) => String(tag).toLowerCase().includes(query));

        if (!matchesSearch) {
          return false;
        }

        if (this.filterMode === 'joined') {
          return this.isUserMember(community.id);
        }
        if (this.filterMode === 'owned') {
          return community.owner?.id === this.currentUserId;
        }
        return true;
      });
    },
    displayedCommunities() {
      const list = [...this.filteredCommunities];
      if (this.sortBy === 'name') {
        return list.sort((a, b) => (a.name || '').localeCompare(b.name || ''));
      }
      if (this.sortBy === 'newest') {
        return list.sort((a, b) => Number(b.id || 0) - Number(a.id || 0));
      }
      return list.sort((a, b) => Number(b.memberCount || 0) - Number(a.memberCount || 0));
    },
    topTags() {
      const counts = new Map();
      this.communities.forEach((community) => {
        (community.tags || []).forEach((tag) => {
          const value = String(tag || '').trim();
          if (!value) return;
          counts.set(value, (counts.get(value) || 0) + 1);
        });
      });
      return [...counts.entries()]
        .sort((a, b) => b[1] - a[1])
        .slice(0, 8)
        .map(([tag]) => tag);
    },
    ownedCount() {
      return this.communities.filter((community) => community.owner?.id === this.currentUserId).length;
    },
  },
  async created() {
    this.searchQuery = String(this.$route.query.q || '');
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
    '$route.query.q'(value) {
      this.searchQuery = String(value || '');
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
      return (Array.isArray(tags) ? tags : []).slice(0, 4);
    },
    hiddenTagCount(tags) {
      const total = Array.isArray(tags) ? tags.length : 0;
      return total > 4 ? total - 4 : 0;
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
      this.error = null;
      try {
        const token = localStorage.getItem('token');
        const response = await axios.get(`${API_BASE_URL}/api/communities`, {
          headers: { Authorization: `Bearer ${token}` },
        });

        const communitiesWithCounts = await Promise.all(
          (response.data || []).map(async (community) => {
            let count = 0;
            try {
              const countResponse = await axios.get(
                `${API_BASE_URL}/api/communities/${community.id}/members/count`,
                {
                  headers: { Authorization: `Bearer ${token}` },
                }
              );
              count = Number(countResponse.data || 0);
            } catch (countError) {
              count = 0;
            }

            return {
              ...community,
              imageUrl: this.resolveCommunityCover(community.imageUrl, community),
              memberCount: count,
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
        this.userJoinedCommunities = (response.data || []).map((community) => community.id);
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
  padding: 18px clamp(12px, 2vw, 24px) 90px;
  background: var(--theme-page-background);
  color: var(--theme-text-primary);
}

.page-header {
  margin-bottom: 14px;
}

.page-title {
  margin: 0;
  font-size: clamp(1.7rem, 2.8vw, 2.4rem);
  color: var(--theme-heading-color);
}

.page-subtitle {
  margin: 6px 0 0;
  color: var(--theme-text-secondary);
}

.communities-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 300px;
  gap: 14px;
  align-items: start;
}

.communities-main {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.toolbar-card,
.side-card,
.community-row {
  border: 1px solid var(--theme-surface-border);
  background: var(--theme-surface-elevated);
  border-radius: 14px;
  box-shadow: var(--theme-shadow-soft);
}

.toolbar-card {
  padding: 10px;
  display: grid;
  grid-template-columns: minmax(180px, 1fr) auto;
  gap: 10px;
  align-items: center;
}

.search-box {
  border: 1px solid var(--theme-input-border);
  background: var(--theme-input-bg);
  border-radius: 10px;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 0 10px;
}

.search-box i {
  color: var(--theme-text-subtle);
}

.search-box input {
  border: none;
  outline: none;
  min-height: 38px;
  width: 100%;
  background: transparent;
  color: var(--theme-input-text);
}

.toolbar-controls {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.sort-select {
  border: 1px solid var(--theme-input-border);
  background: var(--theme-input-bg);
  color: var(--theme-input-text);
  border-radius: 10px;
  min-height: 38px;
  padding: 0 10px;
}

.filter-group {
  display: inline-flex;
  gap: 6px;
}

.filter-pill {
  border: 1px solid var(--theme-button-secondary-border);
  background: var(--theme-button-secondary-bg);
  color: var(--theme-button-secondary-text);
  border-radius: 999px;
  padding: 7px 12px;
  font-size: 0.84rem;
  font-weight: 700;
  cursor: pointer;
}

.filter-pill.active {
  background: color-mix(in srgb, var(--theme-accent) 18%, var(--theme-button-secondary-bg));
  color: var(--theme-text-primary);
  border-color: color-mix(in srgb, var(--theme-accent) 48%, var(--theme-button-secondary-border));
}

.community-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.community-row {
  display: grid;
  grid-template-columns: 78px minmax(0, 1fr) 166px;
  gap: 10px;
  padding: 10px;
  align-items: stretch;
}

.row-vote {
  border-radius: 10px;
  border: 1px solid var(--theme-divider);
  background: var(--theme-surface-1);
  color: var(--theme-text-secondary);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 2px;
  font-size: 0.8rem;
}

.row-vote i {
  color: var(--theme-accent);
}

.row-vote strong {
  font-size: 1rem;
  color: var(--theme-text-primary);
}

.row-content {
  border: none;
  background: transparent;
  text-align: left;
  padding: 2px 2px 2px 0;
  cursor: pointer;
  color: inherit;
}

.row-meta {
  margin: 0;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  color: var(--theme-text-subtle);
  font-size: 0.8rem;
}

.joined-pill {
  border-radius: 8px;
  border: 1px solid color-mix(in srgb, var(--theme-accent) 54%, var(--theme-surface-border));
  background: color-mix(in srgb, var(--theme-accent) 22%, var(--theme-surface-1));
  color: var(--theme-text-primary);
  padding: 3px 8px;
  font-size: 0.7rem;
  font-weight: 700;
  display: inline-flex;
  align-items: center;
  gap: 5px;
  letter-spacing: 0.02em;
  text-transform: uppercase;
}

.joined-pill i {
  color: var(--theme-accent);
  font-size: 0.72rem;
}

.community-title {
  margin: 5px 0 4px;
  font-size: 1.12rem;
  color: var(--theme-heading-color);
  line-height: 1.28;
}

.community-description {
  margin: 0;
  color: var(--theme-text-secondary);
  line-height: 1.45;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.tags-row {
  margin-top: 8px;
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.tag-pill {
  border-radius: 999px;
  border: 1px solid var(--theme-chip-border);
  background: var(--theme-chip-bg);
  color: var(--theme-chip-text);
  padding: 4px 8px;
  font-size: 0.74rem;
  font-weight: 700;
  line-height: 1.1;
}

.tag-overflow {
  opacity: 0.86;
}

.row-side {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.row-cover {
  width: 100%;
  aspect-ratio: 16 / 9;
  border-radius: 10px;
  border: 1px solid var(--theme-surface-border);
  object-fit: cover;
}

.cover-fallback {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: var(--theme-accent);
  background: var(--theme-surface-1);
}

.action-button {
  border: none;
  border-radius: 10px;
  min-height: 36px;
  padding: 8px 12px;
  font-size: 0.85rem;
  font-weight: 700;
  cursor: pointer;
}

.join-button,
.view-button,
.create-button {
  background: var(--theme-button-primary-bg);
  color: var(--theme-button-primary-text);
  box-shadow: var(--theme-button-primary-shadow);
}

.communities-side {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
  position: sticky;
  top: 84px;
}

.side-card {
  padding: 12px;
}

.side-card h3 {
  margin: 0 0 8px;
  color: var(--theme-heading-color);
  font-size: 1rem;
}

.side-card p {
  margin: 0;
  color: var(--theme-text-secondary);
  line-height: 1.45;
  font-size: 0.9rem;
}

.create-community-button {
  width: 100%;
  margin-top: 10px;
  border: none;
  border-radius: 10px;
  min-height: 38px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  background: var(--theme-button-primary-bg);
  color: var(--theme-button-primary-text);
  font-weight: 700;
  cursor: pointer;
}

.snapshot-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;
}

.snapshot-item {
  border: 1px solid var(--theme-divider);
  border-radius: 10px;
  background: var(--theme-surface-1);
  padding: 8px;
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.snapshot-item strong {
  font-size: 1.06rem;
}

.snapshot-item span {
  color: var(--theme-text-subtle);
  font-size: 0.78rem;
}

.top-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.side-tag {
  cursor: pointer;
}

.state-message {
  text-align: center;
  color: var(--theme-text-secondary);
  padding: 18px 10px;
}

.error-message {
  color: var(--theme-danger);
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
  font-size: 1.4rem;
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
  font-size: 0.94rem;
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

@media (max-width: 1180px) {
  .communities-layout {
    grid-template-columns: 1fr;
  }

  .communities-side {
    position: static;
    display: grid;
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 10px;
  }
}

@media (max-width: 920px) {
  .toolbar-card {
    grid-template-columns: 1fr;
  }

  .toolbar-controls {
    flex-wrap: wrap;
  }

  .community-row {
    grid-template-columns: 58px minmax(0, 1fr);
  }

  .row-side {
    grid-column: 1 / -1;
    flex-direction: row;
    align-items: center;
  }

  .row-cover {
    width: 126px;
    flex-shrink: 0;
  }

  .action-button {
    min-width: 88px;
  }
}

@media (max-width: 760px) {
  .communities-page {
    padding: 12px 10px 82px;
  }

  .communities-side {
    grid-template-columns: 1fr;
  }

  .row-side {
    flex-wrap: wrap;
  }

  .modal-actions {
    flex-direction: column;
  }

  .modal-actions .action-button {
    width: 100%;
  }
}
</style>
