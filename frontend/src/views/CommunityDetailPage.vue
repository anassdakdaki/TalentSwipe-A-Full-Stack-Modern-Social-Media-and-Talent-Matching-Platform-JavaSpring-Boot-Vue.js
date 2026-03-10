<template>
  <div class="community-detail-page">
    <div class="page-shell">
      <p v-if="loadingCommunity" class="loading-message">Loading community details...</p>

      <section v-else-if="community" class="community-hero">
        <div class="hero-cover-wrap">
          <img v-if="community.imageUrl" :src="community.imageUrl" :alt="`${community.name} cover`" class="hero-cover" />
          <div v-else class="hero-cover hero-fallback">
            <i class="fas fa-graduation-cap"></i>
          </div>
        </div>

        <div class="hero-content">
          <h1>{{ community.name }}</h1>
          <p class="hero-description">{{ community.description }}</p>
          <div class="hero-meta">
            <span><i class="fas fa-users"></i> {{ memberCount }} members</span>
            <span><i class="fas fa-user-shield"></i> Owner: {{ ownerName }}</span>
          </div>
          <div class="hero-tags" v-if="community.tags && community.tags.length">
            <span v-for="tag in community.tags" :key="tag" class="tag-pill">#{{ tag }}</span>
          </div>
        </div>
      </section>

      <section class="posts-header">
        <h2 class="page-title">Community Posts</h2>
        <button @click="openCreatePostModal" class="create-post-button">
          <i class="fas fa-plus-circle"></i>
          Create New Post
        </button>
      </section>

      <p v-if="loadingPosts" class="loading-message">Loading posts...</p>
      <p v-if="postError" class="error-message">{{ postError }}</p>

      <div v-if="posts.length === 0 && !loadingPosts && !postError" class="no-posts-message">
        No posts yet. Be the first to create one.
      </div>

      <div class="posts-grid" v-else>
        <article v-for="post in posts" :key="post.id" class="post-card">
          <div class="post-header">
            <button type="button" class="author-button" @click="openProfileModal(post.authorId)">
              <img
                v-if="post.authorProfilePictureUrl"
                :src="normalizeMediaUrl(post.authorProfilePictureUrl)"
                alt="Post author avatar"
                class="post-avatar-image"
              />
              <div v-else class="post-avatar placeholder">
                <i class="fas fa-user"></i>
              </div>
              <div class="author-info">
                <span class="post-author">{{ post.authorName }}</span>
                <span class="post-date">{{ new Date(post.createdAt).toLocaleString() }}</span>
              </div>
            </button>
          </div>

          <p class="post-content">{{ post.content }}</p>
          <img v-if="post.imageUrl" :src="normalizeMediaUrl(post.imageUrl)" alt="Post image" class="post-image" />

          <div class="post-hashtags" v-if="post.hashtags && post.hashtags.length">
            <span v-for="hashtag in post.hashtags" :key="hashtag" class="tag-pill">#{{ hashtag }}</span>
          </div>

          <div class="post-actions">
            <button
              @click="toggleLike(post)"
              class="action-button minimal-button"
              :disabled="post.liking"
            >
              <i :class="[post.isLiked ? 'fas' : 'far', 'fa-heart']" />
              <span>{{ post.isLiked ? 'Liked' : 'Like' }} ({{ post.likesCount }})</span>
            </button>
            <button @click="ensureCommentsLoaded(post)" class="action-button minimal-button">
              <i class="far fa-comment"></i>
              <span>Comments ({{ post.commentsCount }})</span>
            </button>
            <button class="action-button minimal-button"><i class="fas fa-share-alt"></i> Share</button>
            <button
              v-if="currentUserId === post.authorId"
              @click="confirmDeletePost(post)"
              class="action-button minimal-button delete-button"
            >
              <i class="fas fa-trash-alt"></i>
              Delete
            </button>
          </div>

          <div class="comments-section">
            <div class="comment-list">
              <p v-if="post.commentsLoading" class="no-comments-message">Loading comments...</p>
              <p v-else-if="post.commentsError" class="comments-error">{{ post.commentsError }}</p>
              <div v-else-if="post.comments && post.comments.length > 0">
                <div v-for="comment in post.comments" :key="comment.id" class="comment-item">
                  <button
                    type="button"
                    class="comment-author-button"
                    @click="openProfileModal(comment.authorId)"
                  >
                    <img
                      v-if="comment.authorProfilePictureUrl"
                      :src="normalizeMediaUrl(comment.authorProfilePictureUrl)"
                      alt="Comment author avatar"
                      class="comment-avatar"
                    />
                    <div v-else class="comment-avatar placeholder">
                      <i class="fas fa-user"></i>
                    </div>
                  </button>
                  <div class="comment-main">
                    <div class="comment-meta">
                      <button
                        type="button"
                        class="comment-author-link"
                        @click="openProfileModal(comment.authorId)"
                      >
                        {{ comment.authorName }}
                      </button>
                      <span class="comment-date">{{ new Date(comment.createdAt).toLocaleString() }}</span>
                    </div>
                    <span class="comment-content">{{ comment.content }}</span>
                  </div>
                </div>
              </div>
              <p v-else class="no-comments-message">No comments yet.</p>
            </div>
          </div>
          <div class="comment-input-area">
            <img
              v-if="currentUserProfilePictureUrl"
              :src="normalizeMediaUrl(currentUserProfilePictureUrl)"
              alt="Your avatar"
              class="comment-avatar"
            />
            <div v-else class="comment-avatar placeholder">
              <i class="fas fa-user"></i>
            </div>
            <input
              type="text"
              v-model="post.newCommentContent"
              @focus="ensureCommentsLoaded(post)"
              @keyup.enter="addComment(post)"
              placeholder="Write a comment..."
              class="comment-input"
            />
            <button @click="addComment(post)" class="action-button post-comment-button">Post</button>
          </div>
        </article>
      </div>
    </div>

    <div v-if="showCreatePostModal" class="modal-overlay">
      <div class="modal-content">
        <h3>Create New Post</h3>
        <form @submit.prevent="submitCreatePost">
          <div class="form-group">
            <label for="postContent">Content</label>
            <textarea id="postContent" v-model="newPost.content" required></textarea>
          </div>

          <div class="form-group image-group">
            <label for="postImageUrl">Upload Image (optional)</label>
            <input type="file" id="postImageUrl" class="file-input" @change="handleImageUpload" accept="image/*" />
            <div class="image-controls">
              <label for="postImageUrl" class="action-button secondary-btn">Choose Image</label>
              <button v-if="imagePreviewUrl" type="button" class="action-button ghost-btn" @click="clearImageSelection">Remove</button>
            </div>
            <img v-if="imagePreviewUrl" :src="imagePreviewUrl" alt="Image preview" class="image-preview" />
          </div>

          <div class="form-group">
            <label for="postHashtags">Hashtags (comma-separated)</label>
            <input type="text" id="postHashtags" v-model="newPostHashtagsInput" />
          </div>

          <div class="modal-actions">
            <button type="submit" class="action-button create-button">Create Post</button>
            <button type="button" @click="closeCreatePostModal" class="action-button cancel-button">Cancel</button>
          </div>
        </form>
      </div>
    </div>

    <AppAlert
      ref="postAlert"
      :title="postAlertTitle"
      :message="postAlertMessage"
      @closed="postAlertVisible = false"
    />

    <UserProfileModal
      :visible="profileModalVisible"
      :user-id="selectedProfileUserId"
      :current-user-id="currentUserId"
      @close="closeProfileModal"
      @open-chat-requested="openChatFromModal"
    />
  </div>
</template>

<script>
import axios from 'axios';
import AppAlert from '@/components/AppAlert.vue';
import UserProfileModal from '@/components/UserProfileModal.vue';
import { findOrCreateChatRoom, getCurrentUser } from '@/utils/profileApi';

const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080').replace(/\/+$/, '');

export default {
  name: 'CommunityDetailPage',
  components: {
    AppAlert,
    UserProfileModal,
  },
  props: {
    communityId: {
      type: String,
      required: true,
    },
  },
  data() {
    return {
      community: null,
      memberCount: 0,
      posts: [],
      loadingCommunity: true,
      loadingPosts: true,
      postError: null,
      showCreatePostModal: false,
      newPost: {
        content: '',
        imageUrl: '',
        hashtags: [],
      },
      newPostHashtagsInput: '',
      selectedImageFile: null,
      imagePreviewUrl: null,
      postAlertTitle: '',
      postAlertMessage: '',
      postAlertVisible: false,
      currentUserId: null,
      currentUserProfilePictureUrl: '',
      profileModalVisible: false,
      selectedProfileUserId: null,
    };
  },
  computed: {
    ownerName() {
      return this.community?.owner?.name || this.community?.owner?.email || 'Unknown';
    },
  },
  async created() {
    await this.fetchCurrentUserId();
    if (this.currentUserId) {
      await Promise.all([this.fetchCommunityDetails(), this.fetchCommunityMemberCount(), this.fetchPosts()]);
    }
  },
  watch: {
    newPostHashtagsInput(newVal) {
      this.newPost.hashtags = newVal.split(',').map((tag) => tag.trim()).filter((tag) => tag.length > 0);
    },
  },
  methods: {
    normalizeMediaUrl(url) {
      if (!url) return '';
      if (url.startsWith('http://') || url.startsWith('https://')) return url;
      return `${API_BASE_URL}${url.startsWith('/') ? url : `/${url}`}`;
    },
    async fetchCurrentUserId() {
      try {
        const token = localStorage.getItem('token');
        const response = await axios.get(`${API_BASE_URL}/api/auth/me`, {
          headers: { Authorization: `Bearer ${token}` },
        });
        this.currentUserId = response.data.id;
        this.currentUserProfilePictureUrl = this.normalizeMediaUrl(response.data.profilePictureUrl || '');
      } catch (error) {
        console.error('Error fetching current user ID for CommunityDetailPage:', error);
        this.currentUserId = null;
        this.currentUserProfilePictureUrl = '';
      }
    },
    async fetchCommunityDetails() {
      this.loadingCommunity = true;
      try {
        const token = localStorage.getItem('token');
        const response = await axios.get(`${API_BASE_URL}/api/communities/${this.communityId}`, {
          headers: { Authorization: `Bearer ${token}` },
        });
        this.community = {
          ...response.data,
          imageUrl: this.normalizeMediaUrl(response.data?.imageUrl),
        };
      } catch (error) {
        console.error('Error fetching community details:', error);
      } finally {
        this.loadingCommunity = false;
      }
    },
    async fetchCommunityMemberCount() {
      try {
        const token = localStorage.getItem('token');
        const response = await axios.get(`${API_BASE_URL}/api/communities/${this.communityId}/members/count`, {
          headers: { Authorization: `Bearer ${token}` },
        });
        this.memberCount = Number(response.data || 0);
      } catch (error) {
        this.memberCount = 0;
      }
    },
    async fetchPosts() {
      this.loadingPosts = true;
      this.postError = null;
      try {
        const token = localStorage.getItem('token');
        const response = await axios.get(`${API_BASE_URL}/api/posts/community/${this.communityId}`, {
          headers: { Authorization: `Bearer ${token}` },
        });
        this.posts = (response.data || []).map((post) => {
          const comments = Array.isArray(post.comments) ? post.comments : [];
          return {
            ...post,
            likesCount: Number(post.likesCount || 0),
            isLiked: Boolean(post.isLiked),
            comments,
            commentsCount: comments.length,
            commentsLoaded: true,
            commentsLoading: false,
            commentsError: '',
            newCommentContent: '',
            commentSubmitting: false,
            liking: false,
          };
        });
      } catch (error) {
        this.postError = 'Failed to load posts.';
        console.error('Error fetching posts:', error);
      } finally {
        this.loadingPosts = false;
      }
    },
    openCreatePostModal() {
      this.showCreatePostModal = true;
      this.newPost.content = '';
      this.newPost.imageUrl = '';
      this.newPostHashtagsInput = '';
      this.selectedImageFile = null;
      this.imagePreviewUrl = null;
    },
    closeCreatePostModal() {
      this.showCreatePostModal = false;
    },
    clearImageSelection() {
      this.selectedImageFile = null;
      this.imagePreviewUrl = null;
      const input = document.getElementById('postImageUrl');
      if (input) input.value = '';
    },
    handleImageUpload(event) {
      const file = event.target.files[0];
      if (!file) {
        this.selectedImageFile = null;
        this.imagePreviewUrl = null;
        return;
      }

      if (!file.type.startsWith('image/')) {
        this.showPostAlert('Invalid File', 'Please choose an image file.');
        return;
      }

      const tenMb = 10 * 1024 * 1024;
      if (file.size > tenMb) {
        this.showPostAlert('File Too Large', 'Image should be 10MB or smaller.');
        return;
      }

      this.selectedImageFile = file;

      const reader = new FileReader();
      reader.onload = (e) => {
        this.imagePreviewUrl = e.target.result;
      };
      reader.readAsDataURL(file);
    },
    async submitCreatePost() {
      if (!this.currentUserId) {
        this.showPostAlert('Login Required', 'You must be logged in to create a post.');
        return;
      }

      const formData = new FormData();
      formData.append('communityId', this.communityId);
      formData.append('content', this.newPost.content);
      formData.append('hashtags', JSON.stringify(Array.from(this.newPost.hashtags)));

      if (this.selectedImageFile) {
        formData.append('imageFile', this.selectedImageFile);
      }

      try {
        const token = localStorage.getItem('token');
        await axios.post(`${API_BASE_URL}/api/posts`, formData, {
          headers: {
            Authorization: `Bearer ${token}`,
            'Content-Type': 'multipart/form-data',
          },
        });
        this.showPostAlert('Success', 'Post created successfully.');
        this.closeCreatePostModal();
        await this.fetchPosts();
      } catch (error) {
        console.error('Error creating post:', error);
        this.showPostAlert('Error', `Failed to create post: ${error.response?.data?.error || error.message}`);
      }
    },
    showPostAlert(title, message) {
      this.postAlertTitle = title;
      this.postAlertMessage = message;
      this.$refs.postAlert.show();
    },
    async ensureCommentsLoaded(post) {
      if (post.commentsLoaded || post.commentsLoading) {
        return;
      }
      await this.fetchCommentsForPost(post);
    },
    async fetchCommentsForPost(post) {
      if (!this.currentUserId) {
        this.showPostAlert('Login Required', 'You must be logged in to view comments.');
        return;
      }
      post.commentsLoading = true;
      post.commentsError = '';
      try {
        const token = localStorage.getItem('token');
        const response = await axios.get(`${API_BASE_URL}/api/comments/post/${post.id}`, {
          headers: { Authorization: `Bearer ${token}` },
        });
        post.comments = Array.isArray(response.data) ? response.data : [];
        post.commentsCount = post.comments.length;
        post.commentsLoaded = true;
      } catch (error) {
        console.error(`Error fetching comments for post ${post.id}:`, error);
        post.commentsError = error.response?.data?.error || error.message || 'Failed to load comments.';
      } finally {
        post.commentsLoading = false;
      }
    },
    async toggleLike(post) {
      if (!this.currentUserId) {
        this.showPostAlert('Login Required', 'You must be logged in to like a post.');
        return;
      }

      if (post.liking) {
        return;
      }

      post.liking = true;
      try {
        const token = localStorage.getItem('token');
        const response = await axios.post(`${API_BASE_URL}/api/likes/post/${post.id}`, {}, {
          headers: { Authorization: `Bearer ${token}` },
        });
        const newIsLiked = Boolean(response?.data?.isLiked);
        if (newIsLiked !== post.isLiked) {
          post.likesCount = Math.max(0, post.likesCount + (newIsLiked ? 1 : -1));
        }
        post.isLiked = newIsLiked;
      } catch (error) {
        console.error('Error toggling like:', error);
        this.showPostAlert('Error', `Failed to toggle like: ${error.response?.data?.error || error.message}`);
      } finally {
        post.liking = false;
      }
    },
    async addComment(post) {
      if (!post.newCommentContent.trim()) {
        this.showPostAlert('Warning', 'Comment cannot be empty.');
        return;
      }
      if (!this.currentUserId) {
        this.showPostAlert('Login Required', 'You must be logged in to comment.');
        return;
      }
      try {
        const token = localStorage.getItem('token');
        await axios.post(`${API_BASE_URL}/api/comments`, {
          postId: post.id,
          content: post.newCommentContent,
        }, {
          headers: { Authorization: `Bearer ${token}` },
        });
        post.newCommentContent = '';
        await this.fetchCommentsForPost(post);
      } catch (error) {
        console.error('Error adding comment:', error);
        this.showPostAlert('Error', `Failed to add comment: ${error.response?.data?.error || error.message}`);
      }
    },
    async confirmDeletePost(post) {
      if (!post?.id) return;
      const shouldDelete = window.confirm('Delete this post? This action cannot be undone.');
      if (!shouldDelete) return;

      try {
        const token = localStorage.getItem('token');
        await axios.delete(`${API_BASE_URL}/api/posts/${post.id}`, {
          headers: { Authorization: `Bearer ${token}` },
        });
        this.showPostAlert('Success', 'Post deleted.');
        await this.fetchPosts();
      } catch (error) {
        this.showPostAlert('Error', error.response?.data?.error || 'Failed to delete post.');
      }
    },
    openProfileModal(userId) {
      const numericId = Number(userId) || null;
      if (!numericId) return;
      this.selectedProfileUserId = numericId;
      this.profileModalVisible = true;
    },
    closeProfileModal() {
      this.profileModalVisible = false;
      this.selectedProfileUserId = null;
    },
    async openChatFromModal(payload) {
      try {
        if (!this.currentUserId) {
          const me = await getCurrentUser();
          this.currentUserId = me.data?.id || null;
        }
        if (!this.currentUserId) return;

        const response = await findOrCreateChatRoom({
          user1Id: this.currentUserId,
          user2Id: Number(payload.userId),
          studyMatchId: Number(payload.matchId),
        });

        this.closeProfileModal();
        this.$router.push({ name: 'Chat', params: { chatRoomId: response.data.id } });
      } catch (error) {
        this.showPostAlert('Error', error?.response?.data?.error || 'Unable to open chat.');
      }
    },
  },
};
</script>

<style scoped>
.community-detail-page {
  min-height: 100vh;
  padding: 24px clamp(12px, 2vw, 26px) 110px;
  background: var(--theme-page-background);
  color: var(--theme-text-primary);
}

.community-hero {
  border: 1px solid var(--theme-surface-border);
  background: var(--theme-surface-elevated);
  border-radius: 20px;
  box-shadow: var(--theme-shadow-soft);
  overflow: hidden;
  display: grid;
  grid-template-columns: minmax(260px, 34%) minmax(0, 1fr);
  margin-bottom: 20px;
}

.hero-cover-wrap {
  min-height: 220px;
  background: var(--theme-surface-1);
}

.hero-cover {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.hero-fallback {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2.2rem;
  color: var(--theme-accent);
}

.hero-content {
  padding: 18px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.hero-content h1 {
  margin: 0;
  font-size: clamp(1.8rem, 3vw, 2.6rem);
  color: var(--theme-heading-color);
}

.hero-description {
  margin: 0;
  color: var(--theme-text-secondary);
  line-height: 1.6;
}

.hero-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.hero-meta span {
  border: 1px solid var(--theme-chip-border);
  background: var(--theme-chip-bg);
  color: var(--theme-chip-text);
  border-radius: 999px;
  padding: 6px 10px;
  font-size: 0.85rem;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.hero-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.posts-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 18px;
}

.page-title {
  margin: 0;
  font-size: clamp(1.8rem, 3vw, 2.6rem);
}

.create-post-button {
  border: none;
  border-radius: 999px;
  padding: 11px 18px;
  display: inline-flex;
  align-items: center;
  gap: 10px;
  background: var(--theme-button-primary-bg);
  color: var(--theme-button-primary-text);
  font-weight: 700;
  box-shadow: var(--theme-button-primary-shadow);
  cursor: pointer;
}

.posts-grid {
  width: min(620px, 100%);
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.post-card {
  border: 1px solid var(--theme-surface-border);
  background: var(--theme-surface-elevated);
  border-radius: 14px;
  box-shadow: var(--theme-shadow-soft);
  padding: 14px;
}

.post-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.author-button {
  border: none;
  background: transparent;
  color: inherit;
  padding: 0;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  text-align: left;
}

.author-button:hover .post-author {
  text-decoration: underline;
}

.post-avatar-image,
.post-avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
}

.post-avatar-image {
  object-fit: cover;
}

.post-avatar.placeholder {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: var(--theme-text-subtle);
  border: 1px solid var(--theme-surface-border);
  background: var(--theme-surface-1);
}

.author-info {
  display: flex;
  flex-direction: column;
  gap: 1px;
}

.post-author {
  font-weight: 700;
  font-size: 0.98rem;
  color: var(--theme-heading-color);
}

.post-date {
  color: var(--theme-text-subtle);
  font-size: 0.8rem;
}

.post-content {
  margin: 0 0 8px;
  color: var(--theme-text-secondary);
  white-space: pre-wrap;
  line-height: 1.45;
  font-size: 0.97rem;
}

.post-image {
  width: 100%;
  aspect-ratio: 16 / 9;
  max-height: 320px;
  object-fit: cover;
  border-radius: 10px;
  border: 1px solid var(--theme-surface-border);
}

.post-hashtags {
  margin-top: 8px;
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.tag-pill {
  border-radius: 999px;
  padding: 6px 10px;
  border: 1px solid var(--theme-chip-border);
  background: var(--theme-chip-bg);
  color: var(--theme-chip-text);
  font-size: 0.84rem;
  font-weight: 600;
}

.post-hashtags .tag-pill {
  padding: 4px 8px;
  font-size: 0.78rem;
}

.post-actions {
  margin-top: 8px;
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.action-button {
  border: 1px solid var(--theme-button-secondary-border);
  background: var(--theme-button-secondary-bg);
  color: var(--theme-button-secondary-text);
  border-radius: 11px;
  padding: 9px 12px;
  font-weight: 700;
  cursor: pointer;
}

.post-card .action-button {
  border-radius: 10px;
  padding: 7px 10px;
  font-size: 0.9rem;
}

.post-card .action-button:disabled {
  opacity: 0.72;
  cursor: not-allowed;
}

.minimal-button {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.minimal-button i {
  color: var(--theme-accent);
}

.minimal-button .fas.fa-heart {
  color: var(--theme-accent-strong);
}

.delete-button {
  color: var(--theme-danger);
  border-color: color-mix(in srgb, var(--theme-danger) 50%, var(--theme-button-secondary-border));
}

.delete-button i {
  color: currentColor;
}

.comments-section {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid var(--theme-divider);
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
  max-height: none;
  overflow: visible;
}

.comment-item {
  border: 1px solid var(--theme-surface-border);
  border-radius: 10px;
  background: var(--theme-surface-1);
  padding: 7px 9px;
  display: flex;
  align-items: flex-start;
  gap: 7px;
}

.comment-author-button {
  border: none;
  background: transparent;
  padding: 0;
  cursor: pointer;
  line-height: 0;
}

.comment-avatar {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  object-fit: cover;
  flex: 0 0 30px;
}

.comment-avatar.placeholder {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: 1px solid var(--theme-surface-border);
  background: var(--theme-surface-elevated);
  color: var(--theme-text-subtle);
}

.comment-main {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.comment-meta {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.comment-author {
  font-weight: 700;
  color: var(--theme-heading-color);
}

.comment-author-link {
  border: none;
  background: transparent;
  color: var(--theme-heading-color);
  font-weight: 700;
  padding: 0;
  cursor: pointer;
}

.comment-author-link:hover {
  text-decoration: underline;
}

.comment-content {
  color: var(--theme-text-primary);
  line-height: 1.4;
}

.comment-date {
  color: var(--theme-text-subtle);
  font-size: 0.8rem;
}

.no-comments-message {
  color: var(--theme-text-subtle);
  text-align: center;
}

.comments-error {
  color: var(--theme-danger);
  text-align: center;
}

.comment-input-area {
  margin-top: 8px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.comment-input {
  flex: 1;
  border: 1px solid var(--theme-input-border);
  border-radius: 10px;
  background: var(--theme-input-bg);
  color: var(--theme-input-text);
  padding: 8px 10px;
}

.comment-input:focus {
  outline: none;
  border-color: var(--theme-accent);
}

.post-comment-button,
.create-button {
  border: none;
  background: var(--theme-button-primary-bg);
  color: var(--theme-button-primary-text);
  box-shadow: var(--theme-button-primary-shadow);
}

.loading-message,
.error-message,
.no-posts-message {
  text-align: center;
  color: var(--theme-text-secondary);
  margin: 20px 0;
}

.modal-overlay {
  position: fixed;
  inset: 0;
  z-index: 1200;
  background: rgba(2, 6, 23, 0.58);
  display: flex;
  align-items: center;
  justify-content: center;
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
  margin: 0 0 14px;
  color: var(--theme-heading-color);
}

.form-group {
  margin-bottom: 12px;
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
  padding: 10px 12px;
}

.form-group textarea {
  min-height: 110px;
  resize: vertical;
}

.form-group input:focus,
.form-group textarea:focus {
  outline: none;
  border-color: var(--theme-accent);
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
  border: 1px solid var(--theme-button-secondary-border);
  background: var(--theme-button-secondary-bg);
  color: var(--theme-button-secondary-text);
}

.secondary-btn,
.ghost-btn {
  width: auto;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.image-preview {
  margin-top: 10px;
  width: 100%;
  max-height: 220px;
  object-fit: cover;
  border-radius: 12px;
  border: 1px solid var(--theme-surface-border);
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 8px;
}

html[data-theme='futuristic'] .page-title,
html[data-theme='futuristic'] .hero-content h1 {
  text-shadow: var(--theme-heading-glow);
}

@media (max-width: 980px) {
  .community-hero {
    grid-template-columns: 1fr;
  }

  .hero-cover-wrap {
    min-height: 180px;
  }

  .posts-header {
    flex-direction: column;
    align-items: stretch;
  }

  .create-post-button {
    justify-content: center;
  }
}

@media (max-width: 760px) {
  .community-detail-page {
    padding: 14px 10px 92px;
  }

  .posts-grid {
    width: 100%;
  }

  .post-image {
    max-height: 240px;
  }

  .post-actions,
  .comment-input-area,
  .modal-actions {
    flex-direction: column;
  }

  .action-button {
    width: 100%;
  }
}
</style>
