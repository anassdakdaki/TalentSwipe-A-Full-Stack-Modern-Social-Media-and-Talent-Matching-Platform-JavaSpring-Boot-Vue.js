<template>
  <div class="community-detail-page">
    <div class="header-section">
      <h2 class="page-title">Community Posts</h2>
      <button @click="openCreatePostModal" class="create-post-button">
        <i class="fas fa-plus-circle"></i> Create New Post
      </button>
    </div>

    <p v-if="loadingPosts" class="loading-message">Loading posts...</p>
    <p v-if="postError" class="error-message">{{ postError }}</p>

    <div v-if="posts.length === 0 && !loadingPosts && !postError" class="no-posts-message">
      No posts yet. Be the first to create one!
    </div>

    <div class="posts-grid" v-else>
      <div v-for="post in posts" :key="post.id" class="post-card glass-effect">
        <div class="post-header">
          <i class="fas fa-user-circle post-avatar"></i>
          <div class="author-info">
            <span class="post-author">{{ post.authorName }}</span>
            <span class="post-date">{{ new Date(post.createdAt).toLocaleString() }}</span>
          </div>
        </div>
        <p class="post-content">{{ post.content }}</p>
        <img v-if="post.imageUrl" :src="`http://localhost:8080${post.imageUrl}`" alt="Post Image" class="post-image"/>
        <div class="post-hashtags">
          <span v-for="hashtag in post.hashtags" :key="hashtag" class="tag-pill">#{{ hashtag }}</span>
        </div>
        <div class="post-actions">
          <button @click="toggleLike(post)" class="action-button minimal-button">
            <i :class="[post.isLiked ? 'fas' : 'far', 'fa-heart']" :style="{ color: post.isLiked ? '#00bfff' : '#64ffda' }"></i> 
            Like <span v-if="post.likesCount > 0">({{ post.likesCount }})</span>
          </button>
          <button @click="toggleComments(post)" class="action-button minimal-button"><i class="far fa-comment"></i> Comment</button>
          <button class="action-button minimal-button"><i class="fas fa-share-alt"></i> Share</button>
          <!-- Delete Button (only visible to post author) -->
          <button v-if="currentUserId === post.authorId" @click="confirmDeletePost(post)" class="action-button minimal-button delete-button">
            <i class="fas fa-trash-alt"></i> Delete
          </button>
        </div>

        <!-- Comments Section -->
        <div v-if="post.showComments" class="comments-section">
          <div class="comment-list">
            <!-- Individual comments will be displayed here -->
            <div v-if="post.comments && post.comments.length > 0">
              <div v-for="comment in post.comments" :key="comment.id" class="comment-item">
                <span class="comment-author">{{ comment.authorName }}: </span>
                <span class="comment-content">{{ comment.content }}</span>
                <span class="comment-date">{{ new Date(comment.createdAt).toLocaleString() }}</span>
              </div>
            </div>
            <p v-else class="no-comments-message">No comments yet.</p>
          </div>
          <div class="comment-input-area">
            <input type="text" v-model="post.newCommentContent" @keyup.enter="addComment(post)" placeholder="Write a comment..." class="comment-input">
            <button @click="addComment(post)" class="action-button post-comment-button neon-effect">Post</button>
          </div>
        </div>
      </div>
    </div>

    <!-- Create Post Modal -->
    <div v-if="showCreatePostModal" class="modal-overlay">
      <div class="modal-content glass-effect">
        <h3>Create New Post</h3>
        <form @submit.prevent="submitCreatePost">
          <div class="form-group">
            <label for="postContent">Content:</label>
            <textarea id="postContent" v-model="newPost.content" required></textarea>
          </div>
          <div class="form-group">
            <label for="postImageUrl">Upload Image (optional):</label>
            <input type="file" id="postImageUrl" @change="handleImageUpload" accept="image/*">
            <img v-if="imagePreviewUrl" :src="imagePreviewUrl" alt="Image Preview" class="image-preview">
          </div>
          <div class="form-group">
            <label for="postHashtags">Hashtags (comma-separated):</label>
            <input type="text" id="postHashtags" v-model="newPostHashtagsInput">
          </div>
          <div class="modal-actions">
            <button type="submit" class="action-button create-button neon-effect">Create Post</button>
            <button type="button" @click="closeCreatePostModal" class="action-button cancel-button">Cancel</button>
          </div>
        </form>
      </div>
    </div>

    <!-- Custom Alert Component for this page -->
    <AppAlert 
      ref="postAlert"
      :title="postAlertTitle"
      :message="postAlertMessage"
      @closed="postAlertVisible = false" 
    />
  </div>
</template>

<script>
import axios from 'axios';
import AppAlert from '@/components/AppAlert.vue'; // Ensure AppAlert is available

export default {
  name: 'CommunityDetailPage',
  components: {
    AppAlert,
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
      currentUserId: null, // To store the authenticated user's ID
    };
  },
  async created() {
    await this.fetchCurrentUserId();
    if (this.currentUserId) {
      await this.fetchCommunityDetails();
      await this.fetchPosts();
    }
  },
  watch: {
    newPostHashtagsInput(newVal) {
      this.newPost.hashtags = newVal.split(',').map(tag => tag.trim()).filter(tag => tag.length > 0);
    },
  },
  methods: {
    async fetchCurrentUserId() {
      try {
        const token = localStorage.getItem('token');
        const response = await axios.get('http://localhost:8080/api/auth/me', {
          headers: { Authorization: `Bearer ${token}` }
        });
        this.currentUserId = response.data.id;
      } catch (error) {
        console.error('Error fetching current user ID for CommunityDetailPage:', error);
        // Handle error, e.g., redirect to login or show a message
      }
    },
    async fetchCommunityDetails() {
      this.loadingCommunity = true;
      try {
        const token = localStorage.getItem('token');
        const response = await axios.get(`http://localhost:8080/api/communities/${this.communityId}`, {
          headers: { Authorization: `Bearer ${token}` }
        });
        this.community = response.data;
      } catch (error) {
        console.error('Error fetching community details:', error);
        // Handle error, e.g., show a message
      } finally {
        this.loadingCommunity = false;
      }
    },
    async fetchPosts() {
      this.loadingPosts = true;
      try {
        const token = localStorage.getItem('token');
        const response = await axios.get(`http://localhost:8080/api/posts/community/${this.communityId}`, {
          headers: { Authorization: `Bearer ${token}` }
        });
        // Initialize each post with showComments and newCommentContent properties
        this.posts = response.data.map(post => ({
          ...post,
          showComments: true, // Show comments by default
          newCommentContent: '',
          // likesCount and isLiked are now provided by the backend in PostResponse
        }));
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
      this.imagePreviewUrl = null; // Clear image preview
    },
    closeCreatePostModal() {
      this.showCreatePostModal = false;
    },
    handleImageUpload(event) {
      const file = event.target.files[0];
      this.selectedImageFile = file;

      if (file) {
        const reader = new FileReader();
        reader.onload = (e) => {
          this.imagePreviewUrl = e.target.result; // Set preview URL
        };
        reader.readAsDataURL(file);
      } else {
        this.imagePreviewUrl = null;
      }
    },
    async submitCreatePost() {
      if (!this.currentUserId) {
        this.showPostAlert('Login Required', 'You must be logged in to create a post.');
        return;
      }

      const formData = new FormData();
      formData.append('communityId', this.communityId);
      formData.append('content', this.newPost.content);
      formData.append('hashtags', JSON.stringify(Array.from(this.newPost.hashtags))); // Send hashtags as JSON string

      if (this.selectedImageFile) {
        formData.append('imageFile', this.selectedImageFile);
      }

      try {
        const token = localStorage.getItem('token');
        await axios.post('http://localhost:8080/api/posts', formData, {
          headers: {
            Authorization: `Bearer ${token}`,
            'Content-Type': 'multipart/form-data', // Important for file uploads
          }
        });
        this.showPostAlert('Success', 'Post created successfully!');
        this.closeCreatePostModal();
        await this.fetchPosts(); // Refresh list of posts
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
    toggleComments(post) {
      // No longer toggling, comments are visible by default. This method can now be removed or repurposed if needed.
      // For now, I'll keep it but it won't do anything.
      // If you want to keep the toggle functionality, let me know.
      // post.showComments = !post.showComments;
      // if (post.showComments && post.comments.length === 0) {
      //   this.fetchCommentsForPost(post); 
      // }
    },
    async fetchCommentsForPost(post) {
      if (!this.currentUserId) {
        this.showPostAlert('Login Required', 'You must be logged in to view comments.');
        return;
      }
      try {
        const token = localStorage.getItem('token');
        const response = await axios.get(`http://localhost:8080/api/comments/post/${post.id}`, {
          headers: { Authorization: `Bearer ${token}` }
        });
        post.comments = response.data; // Directly assign comments as they now contain authorName
      } catch (error) {
        console.error(`Error fetching comments for post ${post.id}:`, error);
        this.showPostAlert('Error', `Failed to load comments for post: ${error.response?.data?.error || error.message}`);
      }
    },
    async toggleLike(post) {
      if (!this.currentUserId) {
        this.showPostAlert('Login Required', 'You must be logged in to like a post.');
        return;
      }

      try {
        const token = localStorage.getItem('token');
        // Send API request to toggle like
        await axios.post(`http://localhost:8080/api/likes/post/${post.id}`, {}, {
          headers: { Authorization: `Bearer ${token}` }
        });

        // Re-fetch posts to get the accurate like count and status from backend
        await this.fetchPosts();

      } catch (error) {
        console.error('Error toggling like:', error);
        this.showPostAlert('Error', `Failed to toggle like: ${error.response?.data?.error || error.message}`);
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
        const commentData = {
          postId: post.id,
          content: post.newCommentContent,
        };
        await axios.post('http://localhost:8080/api/comments', commentData, {
          headers: { Authorization: `Bearer ${token}` }
        });
        post.newCommentContent = ''; // Clear input
        await this.fetchCommentsForPost(post); // Refresh comments for the post
      } catch (error) {
        console.error('Error adding comment:', error);
        this.showPostAlert('Error', `Failed to add comment: ${error.response?.data?.error || error.message}`);
      }
    },
  },
};
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Orbitron:wght@400;700&family=Roboto+Mono:wght@400;700&display=swap');

.community-detail-page {
  padding: 40px;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
  color: #e0e0e0;
  min-height: 100vh;
  font-family: 'Orbitron', sans-serif;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.header-section {
  width: 100%;
  max-width: 1000px; /* Adjust max-width as needed */
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 40px;
  padding-bottom: 20px;
  border-bottom: 1px solid rgba(100, 255, 218, 0.2); /* Neon border */
}

.page-title {
  font-size: 3rem;
  color: #64ffda;
  text-shadow: 0 0 15px #64ffda, 0 0 30px rgba(100, 255, 218, 0.5);
  margin: 0;
  font-weight: 700;
}

.create-post-button {
  background: linear-gradient(45deg, #00bfff, #007bff); /* Blue gradient */
  color: #ffffff;
  padding: 12px 25px;
  border: none;
  border-radius: 30px;
  font-size: 1.1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 0 15px rgba(0, 191, 255, 0.4); /* Subtle blue glow */
  display: flex;
  align-items: center;
  gap: 10px;
}

.create-post-button:hover {
  background: linear-gradient(45deg, #007bff, #00bfff);
  box-shadow: 0 0 25px rgba(0, 191, 255, 0.6);
  transform: translateY(-2px);
}

.create-post-button i {
  color: #fff;
}

.loading-message,
.error-message,
.no-posts-message {
  text-align: center;
  font-size: 1.5rem;
  color: rgba(224, 224, 224, 0.8);
  margin-top: 50px;
}

.posts-grid {
  display: flex; /* Change to flexbox for easier vertical stacking */
  flex-direction: column; /* Stack items vertically */
  gap: 30px;
  width: 100%;
  max-width: 600px; /* Adjust max-width for a single, centered column of posts */
}

.post-card {
  width: 100%; /* Ensure each post card takes full width of the grid */
  background: rgba(255, 255, 255, 0.05); /* Lighter glass base */
  border: 1px solid rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border-radius: 12px; /* Slightly less rounded for a modern look */
  padding: 20px;
  display: flex;
  flex-direction: column;
  transition: all 0.2s ease-in-out;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3); /* Softer, more spread shadow */
}

.post-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.4); /* Enhanced shadow on hover */
  border-color: #00bfff; /* Subtle blue glow on hover */
}

.post-header {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08); /* Lighter separator */
}

.post-avatar {
  font-size: 2.2rem;
  color: #64ffda; /* Neon green for avatar */
  margin-right: 12px;
}

.author-info {
  display: flex;
  flex-direction: column;
  text-align: left;
}

.post-author {
  font-size: 1.1rem;
  color: #00bfff; /* Blue for author name */
  font-weight: bold;
  margin-bottom: 2px;
}

.post-date {
  font-size: 0.85rem;
  color: rgba(224, 224, 224, 0.6);
}

.post-content {
  font-size: 1rem;
  color: rgba(224, 224, 224, 0.9);
  line-height: 1.6;
  margin-bottom: 15px;
  white-space: pre-wrap; /* Preserve whitespace and line breaks */
  word-wrap: break-word;
}

.post-image {
  max-width: 100%;
  border-radius: 8px;
  margin-top: 10px;
  margin-bottom: 15px;
  object-fit: cover;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.post-hashtags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: auto; /* Push hashtags to bottom if content is short */
  margin-bottom: 15px; /* Space before actions */
}

.post-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  justify-content: flex-start;
  align-items: center;
  padding-top: 15px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
}

.action-button.minimal-button {
  min-width: 110px;
  flex: 1 1 110px;
  box-sizing: border-box;
  text-align: center;
}

.minimal-button {
  background: none;
  border: none;
  color: #64ffda; /* Neon green for icons/text */
  font-size: 0.95rem;
  font-weight: 600;
  cursor: pointer;
  transition: color 0.2s ease, transform 0.2s ease;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border-radius: 6px;
}

.minimal-button:hover {
  color: #00bfff; /* Blue on hover */
  transform: translateY(-2px);
  background: rgba(100, 255, 218, 0.05); /* Very subtle background on hover */
}

.minimal-button i {
  font-size: 1.1rem;
}

/* Reuse tag-pill styling from CommunitiesPage.vue or define here if needed */
.tag-pill {
  background: rgba(100, 255, 218, 0.1); /* Subtle green tag background */
  color: #64ffda;
  padding: 5px 12px;
  border-radius: 15px;
  font-size: 0.8rem;
  backdrop-filter: blur(5px);
  border: 1px solid rgba(100, 255, 218, 0.3);
}

/* Responsive adjustments for smaller screens */
@media (max-width: 768px) {
  .community-detail-page {
    padding: 20px;
  }

  .header-section {
    flex-direction: column;
    align-items: center;
    gap: 20px;
    margin-bottom: 30px;
  }

  .page-title {
    font-size: 2.2rem;
    text-align: center;
  }

  .create-post-button {
    width: 100%;
    justify-content: center;
    font-size: 1rem;
    padding: 10px 20px;
  }

  .modal-content {
    padding: 25px;
  }

  .modal-content h3 {
    font-size: 1.5rem;
    margin-bottom: 20px;
  }

  .form-group label {
    font-size: 1rem;
  }

  .form-group input,
  .form-group textarea {
    padding: 10px;
    font-size: 0.9rem;
  }

  .modal-actions {
    flex-direction: column;
    gap: 10px;
  }

  .action-button {
    width: 100%;
    padding: 10px 20px;
    font-size: 0.9rem;
  }

  .posts-grid {
    grid-template-columns: 1fr;
    gap: 20px;
  }

  .post-card {
    padding: 15px;
  }

  .post-author {
    font-size: 1rem;
  }

  .post-date {
    font-size: 0.75rem;
  }

  .post-content {
    font-size: 0.9rem;
  }

  .post-actions {
    flex-direction: column;
    gap: 10px;
  }

  .minimal-button {
    width: 100%;
    justify-content: center;
    font-size: 0.9rem;
    padding: 10px;
  }

  .comments-section {
    padding-top: 15px;
  }

  .comment-list {
    max-height: 150px;
  }

  .comment-item {
    padding: 8px 12px;
  }

  .comment-author,
  .comment-content {
    font-size: 0.9rem;
  }

  .comment-date {
    font-size: 0.7rem;
  }

  .comment-input-area {
    flex-direction: column;
    gap: 8px;
  }

  .comment-input,
  .post-comment-button {
    width: 100%;
  }
}

@media (max-width: 600px) {
  .post-actions {
    flex-direction: column;
    gap: 10px;
    align-items: stretch;
  }
  .action-button.minimal-button {
    width: 100%;
    min-width: 0;
  }
}

.comments-section {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.comment-list {
  margin-bottom: 15px;
  max-height: 200px; /* Limit height for scrollability */
  overflow-y: auto;
  padding-right: 10px;
}

.comment-item {
  background: rgba(255, 255, 255, 0.03);
  border-radius: 8px;
  padding: 10px 15px;
  margin-bottom: 10px;
  display: flex;
  flex-wrap: wrap;
  align-items: baseline;
  gap: 5px;
}

.comment-author {
  font-weight: bold;
  color: #00bfff;
  font-size: 0.95rem;
}

.comment-content {
  color: rgba(224, 224, 224, 0.9);
  font-size: 0.95rem;
  flex-grow: 1; /* Allow content to take up space */
}

.comment-date {
  font-size: 0.75rem;
  color: rgba(224, 224, 224, 0.6);
  margin-left: auto; /* Push date to the right */
}

.no-comments-message {
  color: rgba(224, 224, 224, 0.7);
  font-style: italic;
  font-size: 0.9rem;
  text-align: center;
  padding: 10px;
}

.comment-input-area {
  display: flex;
  gap: 10px;
  margin-top: 15px;
}

.comment-input {
  flex-grow: 1;
  padding: 10px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.1);
  color: #e0e0e0;
  font-size: 0.95rem;
  outline: none;
  transition: border-color 0.3s ease, box-shadow 0.3s ease;
}

.comment-input:focus {
  border-color: #00bfff;
  box-shadow: 0 0 8px rgba(0, 191, 255, 0.4);
}

.post-comment-button {
  background: linear-gradient(45deg, #64ffda, #3be8b0);
  color: #1a1a2e;
  padding: 10px 15px;
  border-radius: 8px;
  font-size: 0.95rem;
  font-weight: bold;
  box-shadow: 0 2px 10px rgba(100, 255, 218, 0.3);
}

.post-comment-button:hover {
  background: linear-gradient(45deg, #3be8b0, #64ffda);
  box-shadow: 0 4px 15px rgba(100, 255, 218, 0.5);
  transform: translateY(-1px);
}

/* Override default minimal-button hover for like button when active */
.minimal-button i.fas.fa-heart {
  color: #00bfff !important; /* Force blue color for liked state */
}

.minimal-button i.fas.fa-heart:hover {
  transform: scale(1.1); /* Slightly enlarge on hover when liked */
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.7); /* Dark semi-transparent overlay */
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: rgba(255, 255, 255, 0.08); /* Lighter glass base for modal */
  border: 1px solid rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(15px);
  -webkit-backdrop-filter: blur(15px);
  border-radius: 20px;
  padding: 40px;
  width: 90%;
  max-width: 500px;
  max-height: 90vh; /* Set a maximum height for the modal */
  overflow-y: auto; /* Enable vertical scrolling */
  box-shadow: 0 10px 40px 0 rgba(31, 38, 135, 0.45);
  color: #e0e0e0;
  font-family: 'Roboto Mono', monospace;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.modal-content h3 {
  color: #64ffda;
  font-size: 2rem;
  margin-bottom: 30px;
  text-align: center;
  text-shadow: 0 0 10px rgba(100, 255, 218, 0.6);
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-size: 1.1rem;
  color: #64ffda; /* Neon green for labels */
}

.form-group input,
.form-group textarea {
  width: calc(100% - 24px); /* Account for padding */
  padding: 12px;
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.1);
  color: #e0e0e0;
  font-size: 1rem;
  outline: none;
  transition: border-color 0.3s ease, box-shadow 0.3s ease;
}

.form-group input:focus,
.form-group textarea:focus {
  border-color: #00bfff; /* Blue glow on focus */
  box-shadow: 0 0 10px rgba(0, 191, 255, 0.5);
}

.image-preview {
  max-width: 100%;
  height: auto;
  border-radius: 8px;
  margin-top: 15px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 0 10px rgba(0, 191, 255, 0.3);
}

.form-group textarea {
  resize: vertical;
  min-height: 100px;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 15px;
  margin-top: 30px;
}

.action-button {
  padding: 12px 30px;
  border: none;
  border-radius: 25px;
  cursor: pointer;
  font-size: 1rem;
  font-weight: bold;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.3);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.create-button {
  background: linear-gradient(45deg, #64ffda, #3be8b0); /* Green gradient for create */
  color: #1a1a2e;
}

.create-button:hover {
  background: linear-gradient(45deg, #3be8b0, #64ffda);
  box-shadow: 0 6px 20px rgba(100, 255, 218, 0.5);
  transform: translateY(-2px);
}

.cancel-button {
  background: rgba(255, 255, 255, 0.1); /* Subtle cancel button */
  color: #e0e0e0;
  box-shadow: none;
}

.cancel-button:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: translateY(-2px);
}

/* Responsive adjustments for smaller screens */
@media (max-width: 768px) {
  .modal-content {
    padding: 25px;
  }

  .modal-content h3 {
    font-size: 1.5rem;
    margin-bottom: 20px;
  }

  .form-group label {
    font-size: 1rem;
  }

  .form-group input,
  .form-group textarea {
    padding: 10px;
    font-size: 0.9rem;
  }

  .modal-actions {
    flex-direction: column;
    gap: 10px;
  }

  .action-button {
    width: 100%;
    padding: 10px 20px;
    font-size: 0.9rem;
  }
}
</style> 