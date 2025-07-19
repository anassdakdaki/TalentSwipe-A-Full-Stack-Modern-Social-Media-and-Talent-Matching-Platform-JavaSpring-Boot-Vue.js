<template>
  <div class="communities-page">
    <div class="header-section">
      <h2 class="page-title">Discover New Realms</h2>
      <button @click="openCreateCommunityModal" class="create-community-button">
        <i class="fas fa-plus-circle"></i> Create Your Own Community
      </button>
    </div>

    <div v-if="loading" class="loading-message">Loading communities...</div>
    <div v-if="error" class="error-message">{{ error }}</div>

    <div v-if="communities.length === 0 && !loading && !error" class="no-communities-message">
      No communities available yet. Be the first to create one!
    </div>

    <div class="community-grid" v-else>
      <div v-for="community in communities" :key="community.id" class="community-card glass-effect">
        <h3 class="community-title">{{ community.name }}</h3>
        <p class="community-description">{{ community.description }}</p>
        <div class="community-meta">
          <span class="member-count"><i class="fas fa-users neon-glow"></i> {{ community.memberCount }} Members</span>
          <div class="community-tags">
            <span v-for="tag in community.tags" :key="tag" class="tag-pill">#{{ tag }}</span>
          </div>
        </div>
        <div class="card-actions">
          <button 
            v-if="community.owner.id === currentUserId || isUserMember(community.id)"
            @click="viewCommunity(community.id)"
            class="action-button view-button neon-effect"
          >
            View Posts
          </button>
          <button 
            v-else
            @click="joinCommunity(community.id)" 
            class="action-button join-button neon-effect"
          >
            Join
          </button>
        </div>
      </div>
    </div>

    <!-- Create Community Modal -->
    <div v-if="showCreateCommunityModal" class="modal-overlay">
      <div class="modal-content glass-effect">
        <h3>Create New Community</h3>
        <form @submit.prevent="submitCreateCommunity">
          <div class="form-group">
            <label for="communityName">Community Name:</label>
            <input type="text" id="communityName" v-model="newCommunity.name" required>
          </div>
          <div class="form-group">
            <label for="communityDescription">Description:</label>
            <textarea id="communityDescription" v-model="newCommunity.description" required></textarea>
          </div>
          <div class="form-group">
            <label for="communityTags">Tags (comma-separated):</label>
            <input type="text" id="communityTags" v-model="newCommunityTagsInput">
          </div>
          <div class="modal-actions">
            <button type="submit" class="action-button create-button neon-effect">Create</button>
            <button type="button" @click="closeCreateCommunityModal" class="action-button cancel-button">Cancel</button>
          </div>
        </form>
      </div>
    </div>

    <!-- Custom Alert Component -->
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
      currentUserId: null, // To store the authenticated user's ID
      userJoinedCommunities: [], // To store IDs of communities the user is a member of
      showCreateCommunityModal: false,
      newCommunity: {
        name: '',
        description: '',
        tags: [],
      },
      newCommunityTagsInput: '',
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
      this.newCommunity.tags = newVal.split(',').map(tag => tag.trim()).filter(tag => tag.length > 0);
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
        console.error('Error fetching current user ID:', error);
        this.error = 'Could not load user data. Please try logging in again.';
      }
    },
    async fetchCommunities() {
      this.loading = true;
      try {
        const token = localStorage.getItem('token');
        const response = await axios.get('http://localhost:8080/api/communities', {
          headers: { Authorization: `Bearer ${token}` }
        });
        // Fetch member count for each community
        const communitiesWithCounts = await Promise.all(response.data.map(async (community) => {
          const countResponse = await axios.get(`http://localhost:8080/api/communities/${community.id}/members/count`, {
            headers: { Authorization: `Bearer ${token}` }
          });
          return { ...community, memberCount: countResponse.data };
        }));
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
        const response = await axios.get(`http://localhost:8080/api/communities/my-communities`, {
          headers: { Authorization: `Bearer ${token}` }
        });
        this.userJoinedCommunities = response.data.map(community => community.id);
      } catch (error) {
        console.error('Error fetching user\'s communities:', error);
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
        await axios.post(`http://localhost:8080/api/communities/${communityId}/join`, {}, {
          headers: { Authorization: `Bearer ${token}` }
        });
        this.showAlertMessage('Success', 'Successfully joined community!');
        // Re-fetch communities and user communities to update UI
        await this.fetchCommunities();
        await this.fetchUserCommunities();
      } catch (error) {
        console.error('Error joining community:', error);
        this.showAlertMessage('Error', `Failed to join community: ${error.response?.data?.error || error.message}`);
      }
    },
    viewCommunity(communityId) {
      // This will navigate to a detailed view of the community, showing its posts.
      // We will implement this in the next steps.
      this.$router.push({ name: 'CommunityDetail', params: { communityId: communityId } });
    },
    openCreateCommunityModal() {
      this.showCreateCommunityModal = true;
      this.newCommunity.name = '';
      this.newCommunity.description = '';
      this.newCommunityTagsInput = '';
    },
    closeCreateCommunityModal() {
      this.showCreateCommunityModal = false;
    },
    async submitCreateCommunity() {
      if (!this.currentUserId) {
        this.showAlertMessage('Login Required', 'You must be logged in to create a community.');
        return;
      }
      try {
        const token = localStorage.getItem('token');
        await axios.post('http://localhost:8080/api/communities', this.newCommunity, {
          headers: { Authorization: `Bearer ${token}` }
        });
        this.showAlertMessage('Success', 'Community created successfully!');
        this.closeCreateCommunityModal();
        await this.fetchCommunities(); // Refresh list of communities
      } catch (error) {
        console.error('Error creating community:', error);
        this.showAlertMessage('Error', `Failed to create community: ${error.response?.data?.error || error.message}`);
      }
    },
    showAlertMessage(title, message) {
      this.alertTitle = title;
      this.alertMessage = message;
      this.$refs.appAlert.show();
    }
  },
};
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Orbitron:wght@400;700&family=Roboto+Mono:wght@400;700&display=swap');

.communities-page {
  padding: 40px;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%); /* Deep space gradient */
  color: #e0e0e0; /* Light grey for text */
  min-height: 100vh; /* Full viewport height */
  font-family: 'Orbitron', sans-serif; /* Futuristic font */
  display: flex;
  flex-direction: column;
  align-items: center;
}

.header-section {
  width: 100%;
  max-width: 1200px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 40px;
  padding-bottom: 20px;
  border-bottom: 1px solid rgba(100, 255, 218, 0.2); /* Neon border */
}

.page-title {
  font-size: 3rem;
  color: #64ffda; /* Neon green */
  text-shadow: 0 0 15px #64ffda, 0 0 30px rgba(100, 255, 218, 0.5);
  margin: 0;
  font-weight: 700;
}

.create-community-button {
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

.create-community-button:hover {
  background: linear-gradient(45deg, #007bff, #00bfff);
  box-shadow: 0 0 25px rgba(0, 191, 255, 0.6);
  transform: translateY(-2px);
}

.create-community-button i {
  color: #fff;
}

.loading-message,
.error-message,
.no-communities-message {
  text-align: center;
  font-size: 1.5rem;
  color: rgba(224, 224, 224, 0.8);
  margin-top: 50px;
}

.community-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 30px;
  width: 100%;
  max-width: 1200px;
}

.community-card {
  background: rgba(255, 255, 255, 0.05); /* Lighter glass base */
  border: 1px solid rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border-radius: 20px;
  padding: 25px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  transition: all 0.3s ease;
  box-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.37); /* Deeper shadow */
}

.community-card:hover {
  transform: translateY(-10px) scale(1.02);
  box-shadow: 0 12px 40px 0 rgba(31, 38, 135, 0.5); /* Enhanced shadow on hover */
  border-color: #64ffda; /* Neon border on hover */
}

.community-title {
  font-size: 1.8rem;
  color: #64ffda; /* Neon green for titles */
  margin-top: 0;
  margin-bottom: 10px;
  font-weight: 700;
  text-shadow: 0 0 8px rgba(100, 255, 218, 0.4);
}

.community-description {
  font-size: 1rem;
  color: rgba(224, 224, 224, 0.9);
  line-height: 1.6;
  flex-grow: 1;
  margin-bottom: 20px;
}

.community-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: auto; /* Pushes meta to the bottom */
  padding-top: 15px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.member-count {
  font-size: 0.95rem;
  color: rgba(224, 224, 224, 0.7);
  display: flex;
  align-items: center;
  gap: 8px;
}

.member-count i {
  color: #00bfff; /* Blue glow for icons */
  text-shadow: 0 0 5px rgba(0, 191, 255, 0.6);
}

.community-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-pill {
  background: rgba(100, 255, 218, 0.1); /* Subtle green tag background */
  color: #64ffda;
  padding: 5px 12px;
  border-radius: 15px;
  font-size: 0.8rem;
  backdrop-filter: blur(5px);
  border: 1px solid rgba(100, 255, 218, 0.3);
}

.card-actions {
  margin-top: 20px;
  text-align: center;
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

.join-button {
  background: linear-gradient(45deg, #64ffda, #3be8b0); /* Green gradient for join */
  color: #1a1a2e;
}

.join-button:hover {
  background: linear-gradient(45deg, #3be8b0, #64ffda);
  box-shadow: 0 6px 20px rgba(100, 255, 218, 0.5);
  transform: translateY(-2px);
}

.view-button {
  background: linear-gradient(45deg, #00bfff, #007bff); /* Blue gradient for view */
  color: #ffffff;
}

.view-button:hover {
  background: linear-gradient(45deg, #007bff, #00bfff);
  box-shadow: 0 6px 20px rgba(0, 191, 255, 0.5);
  transform: translateY(-2px);
}

/* Modal Styles */
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
  box-shadow: 0 10px 40px 0 rgba(31, 38, 135, 0.45);
  color: #e0e0e0;
  font-family: 'Roboto Mono', monospace; /* Monospaced font for modal inputs */
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
  .communities-page {
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

  .create-community-button {
    width: 100%;
    justify-content: center;
    font-size: 1rem;
    padding: 10px 20px;
  }

  .community-grid {
    grid-template-columns: 1fr;
    gap: 20px;
  }

  .community-card {
    padding: 20px;
  }

  .community-title {
    font-size: 1.5rem;
  }

  .community-description {
    font-size: 0.9rem;
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
}
</style> 