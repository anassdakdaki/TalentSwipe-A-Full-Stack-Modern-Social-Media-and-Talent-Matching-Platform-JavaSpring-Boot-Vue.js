<template>
  <div class="study-groups-container">
    <div class="header">
      <h1>Study Groups</h1>
      <p>Join or create study groups to collaborate with other students</p>
    </div>

    <div class="actions">
      <button @click="showCreateGroupModal = true" class="create-button">
        Create New Group
      </button>
    </div>

    <div class="groups-section">
      <div class="tabs">
        <button 
          :class="['tab-button', { active: activeTab === 'my-groups' }]"
          @click="activeTab = 'my-groups'"
        >
          My Groups
        </button>
        <button 
          :class="['tab-button', { active: activeTab === 'discover' }]"
          @click="activeTab = 'discover'"
        >
          Discover Groups
        </button>
      </div>

      <div class="groups-grid">
        <div v-if="loading" class="loading">
          Loading study groups...
        </div>
        <div v-else-if="filteredGroups.length === 0" class="no-results">
          No study groups found.
        </div>
        <div v-else class="group-cards">
          <div v-for="group in filteredGroups" :key="group.id" class="group-card">
            <div class="group-info">
              <h3>{{ group.name }}</h3>
              <p class="subject">{{ group.subject }}</p>
              <p class="members">{{ group.memberCount }} members</p>
              <p class="schedule">Meets: {{ group.schedule }}</p>
              <p class="description">{{ group.description }}</p>
            </div>
            <div class="group-actions">
              <button 
                v-if="!group.isMember" 
                @click="joinGroup(group)" 
                class="join-button"
              >
                Join Group
              </button>
              <button 
                v-else 
                @click="viewGroup(group)" 
                class="view-button"
              >
                View Group
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Create Group Modal -->
    <div v-if="showCreateGroupModal" class="modal">
      <div class="modal-content">
        <h2>Create New Study Group</h2>
        <form @submit.prevent="createGroup">
          <div class="form-group">
            <label for="groupName">Group Name</label>
            <input
              type="text"
              id="groupName"
              v-model="newGroup.name"
              required
              placeholder="Enter group name"
            />
          </div>
          <div class="form-group">
            <label for="subject">Subject</label>
            <input
              type="text"
              id="subject"
              v-model="newGroup.subject"
              required
              placeholder="Enter subject"
            />
          </div>
          <div class="form-group">
            <label for="schedule">Meeting Schedule</label>
            <input
              type="text"
              id="schedule"
              v-model="newGroup.schedule"
              required
              placeholder="e.g., Every Monday 2-4 PM"
            />
          </div>
          <div class="form-group">
            <label for="description">Description</label>
            <textarea
              id="description"
              v-model="newGroup.description"
              required
              placeholder="Describe your study group"
              rows="4"
            ></textarea>
          </div>
          <div class="modal-actions">
            <button type="button" @click="showCreateGroupModal = false" class="cancel-button">
              Cancel
            </button>
            <button type="submit" class="submit-button" :disabled="loading">
              {{ loading ? 'Creating...' : 'Create Group' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'StudyGroups',
  data() {
    return {
      activeTab: 'my-groups',
      groups: [],
      loading: false,
      showCreateGroupModal: false,
      newGroup: {
        name: '',
        subject: '',
        schedule: '',
        description: ''
      }
    }
  },
  computed: {
    filteredGroups() {
      return this.activeTab === 'my-groups'
        ? this.groups.filter(group => group.isMember)
        : this.groups
    }
  },
  methods: {
    async fetchGroups() {
      this.loading = true
      try {
        const response = await axios.get('http://localhost:8080/api/study-groups', {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        })
        this.groups = response.data
      } catch (error) {
        console.error('Error fetching groups:', error)
        alert('Failed to fetch study groups. Please try again.')
      } finally {
        this.loading = false
      }
    },
    async createGroup() {
      this.loading = true
      try {
        await axios.post('http://localhost:8080/api/study-groups', this.newGroup, {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        })
        this.showCreateGroupModal = false
        this.newGroup = {
          name: '',
          subject: '',
          schedule: '',
          description: ''
        }
        await this.fetchGroups()
        alert('Study group created successfully!')
      } catch (error) {
        console.error('Error creating group:', error)
        alert('Failed to create study group. Please try again.')
      } finally {
        this.loading = false
      }
    },
    async joinGroup(group) {
      try {
        await axios.post(`http://localhost:8080/api/study-groups/${group.id}/join`, {}, {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        })
        group.isMember = true
        alert('Successfully joined the study group!')
      } catch (error) {
        console.error('Error joining group:', error)
        alert('Failed to join study group. Please try again.')
      }
    },
    viewGroup(group) {
      this.$router.push(`/study/groups/${group.id}`)
    }
  },
  mounted() {
    this.fetchGroups()
  }
}
</script>

<style scoped>
.study-groups-container {
  max-width: 1920px;
  margin: 0 auto;
  padding: 2rem;
}

.header {
  text-align: center;
  margin-bottom: 3rem;
}

.header h1 {
  font-size: 2.5rem;
  color: #2c3e50;
  font-weight: 600;
  margin-bottom: 1rem;
}

.header p {
  font-size: 1.2rem;
  color: #666;
}

.actions {
  margin-bottom: 2rem;
  text-align: right;
}

.create-button {
  padding: 1rem 2rem;
  background-color: #4CAF50;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 1.1rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.create-button:hover {
  background-color: #45a049;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.groups-section {
  background: white;
  padding: 2rem;
  border-radius: 12px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.tabs {
  display: flex;
  gap: 1rem;
  margin-bottom: 2rem;
}

.tab-button {
  padding: 1rem 2rem;
  background-color: #f8f9fa;
  color: #666;
  border: none;
  border-radius: 8px;
  font-size: 1.1rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.tab-button.active {
  background-color: #4CAF50;
  color: white;
}

.tab-button:hover:not(.active) {
  background-color: #e9ecef;
}

.groups-grid {
  margin-top: 2rem;
}

.loading, .no-results {
  text-align: center;
  padding: 2rem;
  font-size: 1.2rem;
  color: #666;
}

.group-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 2rem;
}

.group-card {
  background: white;
  padding: 2rem;
  border-radius: 12px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.group-info h3 {
  font-size: 1.5rem;
  color: #2c3e50;
  margin-bottom: 0.5rem;
}

.group-info p {
  color: #666;
  margin-bottom: 0.5rem;
}

.group-info .subject {
  font-weight: 500;
  color: #4CAF50;
}

.group-info .members {
  color: #666;
}

.group-info .schedule {
  color: #666;
}

.group-info .description {
  color: #666;
  line-height: 1.5;
}

.group-actions {
  margin-top: auto;
}

.join-button, .view-button {
  width: 100%;
  padding: 1rem;
  border: none;
  border-radius: 8px;
  font-size: 1.1rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.join-button {
  background-color: #4CAF50;
  color: white;
}

.join-button:hover {
  background-color: #45a049;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.view-button {
  background-color: #f8f9fa;
  color: #2c3e50;
  border: 2px solid #ddd;
}

.view-button:hover {
  background-color: #e9ecef;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  padding: 2rem;
  border-radius: 12px;
  width: 100%;
  max-width: 600px;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-content h2 {
  font-size: 1.8rem;
  color: #2c3e50;
  margin-bottom: 2rem;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  color: #666;
  font-size: 1.1rem;
  font-weight: 500;
}

.form-group input,
.form-group textarea {
  width: 100%;
  padding: 1rem;
  border: 2px solid #ddd;
  border-radius: 8px;
  font-size: 1.1rem;
  transition: all 0.3s ease;
}

.form-group input:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #4CAF50;
  box-shadow: 0 0 0 3px rgba(76, 175, 80, 0.1);
}

.modal-actions {
  display: flex;
  gap: 1rem;
  margin-top: 2rem;
}

.cancel-button,
.submit-button {
  flex: 1;
  padding: 1rem;
  border: none;
  border-radius: 8px;
  font-size: 1.1rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.cancel-button {
  background-color: #f8f9fa;
  color: #666;
  border: 2px solid #ddd;
}

.cancel-button:hover {
  background-color: #e9ecef;
}

.submit-button {
  background-color: #4CAF50;
  color: white;
}

.submit-button:hover:not(:disabled) {
  background-color: #45a049;
}

.submit-button:disabled {
  background-color: #cccccc;
  cursor: not-allowed;
}

@media (max-width: 1200px) {
  .study-groups-container {
    padding: 1.5rem;
  }
}

@media (max-width: 768px) {
  .study-groups-container {
    padding: 1rem;
  }

  .header h1 {
    font-size: 2rem;
  }

  .groups-section {
    padding: 1.5rem;
  }

  .group-card {
    padding: 1.5rem;
  }

  .modal-content {
    padding: 1.5rem;
    margin: 1rem;
  }
}

@media (max-width: 480px) {
  .header h1 {
    font-size: 1.75rem;
  }

  .groups-section {
    padding: 1.25rem;
  }

  .group-card {
    padding: 1.25rem;
  }

  .modal-content {
    padding: 1.25rem;
  }

  .modal-actions {
    flex-direction: column;
  }
}
</style> 