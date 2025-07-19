<template>
  <div class="home-container">
    <div class="dashboard-header">
      <h1>Welcome to Biblov</h1>
      <p class="subtitle">Connect with like-minded people</p>
    </div>

    <div class="dashboard-grid">
      <!-- Study Groups Card -->
      <div class="dashboard-card groups">
        <div class="card-header">
          <h2>Study Groups</h2>
          <button @click="createGroup" class="action-button">
            <i class="fas fa-plus"></i> Create Group
          </button>
        </div>
        <div class="groups-list">
          <div v-if="groups.length === 0" class="empty-state">
            <i class="fas fa-users"></i>
            <p>No active study groups</p>
          </div>
          <div v-else v-for="group in groups" :key="group.id" class="group-item">
            <div class="group-content">
              <h3>{{ group.name }}</h3>
              <p class="subject">{{ group.subject }}</p>
              <span class="meeting-time">Next meeting: {{ formatDateTime(group.nextMeeting) }}</span>
            </div>
            <button class="join-button">Join</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'Home',
  data() {
    return {
      loading: false,
      groups: []
    }
  },
  methods: {
    async fetchDashboardData() {
      this.loading = true
      try {
        console.log('Fetching dashboard data...')
        const token = localStorage.getItem('token')
        console.log('Token available:', !!token)
        
        const response = await axios.get('http://localhost:8080/api/dashboard', {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        })
        console.log('Dashboard response:', response.data)
        
        const data = response.data
        this.groups = Array.isArray(data.groups) ? data.groups : []
      } catch (error) {
        console.error('Error fetching dashboard data:', {
          message: error.message,
          response: error.response?.data,
          status: error.response?.status,
          headers: error.response?.headers
        })
        
        // Initialize with empty data on error
        this.groups = []
        
        if (error.response?.status === 401) {
          // Token expired or invalid
          localStorage.removeItem('token')
          this.$router.push('/auth/login')
        } else {
          alert('Failed to fetch dashboard data. Please try again.')
        }
      } finally {
        this.loading = false
      }
    },
    formatDateTime(dateTimeString) {
      return new Date(dateTimeString).toLocaleString()
    },
    createGroup() {
      // TODO: Implement group creation
      console.log('Create group clicked')
    }
  },
  mounted() {
    this.fetchDashboardData()
  }
}
</script>

<style scoped>
.home-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
}

.dashboard-header {
  text-align: center;
  margin-bottom: 2rem;
}

.dashboard-header h1 {
  font-size: 2.5rem;
  color: #2c3e50;
  margin-bottom: 0.5rem;
}

.subtitle {
  color: #666;
  font-size: 1.2rem;
}

.dashboard-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 2rem;
}

.dashboard-card {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  transition: transform 0.2s;
}

.dashboard-card:hover {
  transform: translateY(-5px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.card-header h2 {
  font-size: 1.5rem;
  color: #2c3e50;
  margin: 0;
}

.action-button {
  background: #4CAF50;
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 20px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  transition: background 0.2s;
}

.action-button:hover {
  background: #45a049;
}

/* Groups Card */
.groups-list {
  max-height: 300px;
  overflow-y: auto;
}

.group-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  border-bottom: 1px solid #eee;
}

.group-content h3 {
  margin: 0 0 0.5rem 0;
  font-size: 1.1rem;
}

.subject {
  color: #666;
  font-size: 0.9rem;
}

.meeting-time {
  font-size: 0.8rem;
  color: #999;
}

.join-button {
  background: #2196F3;
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 20px;
  cursor: pointer;
  transition: background 0.2s;
}

.join-button:hover {
  background: #1976D2;
}

/* Empty States */
.empty-state {
  text-align: center;
  padding: 2rem;
  color: #999;
}

.empty-state i {
  font-size: 2rem;
  margin-bottom: 1rem;
}

.empty-state p {
  margin: 0;
}
</style> 