<template>
  <div class="find-partners-container">
    <div class="header">
      <h1>Find Study Partners</h1>
      <p>Connect with students who share your academic interests</p>
    </div>

    <div class="search-section">
      <div class="search-filters">
        <div class="filter-group">
          <label for="subject">Subject</label>
          <input
            type="text"
            id="subject"
            v-model="filters.subject"
            placeholder="e.g., Mathematics, Physics"
          />
        </div>
        <div class="filter-group">
          <label for="level">Level</label>
          <select id="level" v-model="filters.level">
            <option value="">Any Level</option>
            <option value="undergraduate">Undergraduate</option>
            <option value="graduate">Graduate</option>
            <option value="phd">PhD</option>
          </select>
        </div>
        <div class="filter-group">
          <label for="availability">Availability</label>
          <select id="availability" v-model="filters.availability">
            <option value="">Any Time</option>
            <option value="morning">Morning</option>
            <option value="afternoon">Afternoon</option>
            <option value="evening">Evening</option>
            <option value="night">Night</option>
          </select>
        </div>
        <button @click="searchPartners" class="search-button">
          Search Partners
        </button>
      </div>
    </div>

    <div class="partners-grid">
      <div v-if="loading" class="loading">
        Loading potential study partners...
      </div>
      <div v-else-if="partners.length === 0" class="no-results">
        No study partners found matching your criteria.
      </div>
      <div v-else class="partner-cards">
        <div v-for="partner in partners" :key="partner.id" class="partner-card">
          <div class="partner-info">
            <h3>{{ partner.name }}</h3>
            <p class="major">{{ partner.major }}</p>
            <p class="subjects">Interested in: {{ partner.subjects.join(', ') }}</p>
            <p class="availability">Available: {{ partner.availability }}</p>
          </div>
          <div class="partner-actions">
            <button @click="connectWithPartner(partner)" class="connect-button">
              Connect
            </button>
            <button @click="viewProfile(partner)" class="view-profile-button">
              View Profile
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'FindPartners',
  data() {
    return {
      filters: {
        subject: '',
        level: '',
        availability: ''
      },
      partners: [],
      loading: false
    }
  },
  methods: {
    async searchPartners() {
      this.loading = true
      try {
        const response = await axios.get('http://localhost:8080/api/study-partners/search', {
          params: this.filters,
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        })
        this.partners = response.data
      } catch (error) {
        console.error('Error searching partners:', error)
        alert('Failed to search for study partners. Please try again.')
      } finally {
        this.loading = false
      }
    },
    async connectWithPartner(partner) {
      try {
        await axios.post(`http://localhost:8080/api/study-partners/connect/${partner.id}`, {}, {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        })
        alert('Connection request sent successfully!')
      } catch (error) {
        console.error('Error connecting with partner:', error)
        alert('Failed to send connection request. Please try again.')
      }
    },
    viewProfile(partner) {
      this.$router.push(`/user/profile/${partner.id}`)
    }
  },
  mounted() {
    this.searchPartners()
  }
}
</script>

<style scoped>
.find-partners-container {
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

.search-section {
  background: white;
  padding: 2rem;
  border-radius: 12px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  margin-bottom: 2rem;
}

.search-filters {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1.5rem;
  align-items: end;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

label {
  color: #666;
  font-size: 1.1rem;
  font-weight: 500;
}

input, select {
  padding: 1rem;
  border: 2px solid #ddd;
  border-radius: 8px;
  font-size: 1.1rem;
  transition: all 0.3s ease;
}

input:focus, select:focus {
  outline: none;
  border-color: #4CAF50;
  box-shadow: 0 0 0 3px rgba(76, 175, 80, 0.1);
}

.search-button {
  padding: 1rem;
  background-color: #4CAF50;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 1.1rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.search-button:hover {
  background-color: #45a049;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.partners-grid {
  margin-top: 2rem;
}

.loading, .no-results {
  text-align: center;
  padding: 2rem;
  font-size: 1.2rem;
  color: #666;
}

.partner-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 2rem;
}

.partner-card {
  background: white;
  padding: 2rem;
  border-radius: 12px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.partner-info h3 {
  font-size: 1.5rem;
  color: #2c3e50;
  margin-bottom: 0.5rem;
}

.partner-info p {
  color: #666;
  margin-bottom: 0.5rem;
}

.partner-info .major {
  font-weight: 500;
  color: #4CAF50;
}

.partner-actions {
  display: flex;
  gap: 1rem;
}

.connect-button, .view-profile-button {
  flex: 1;
  padding: 0.875rem;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.connect-button {
  background-color: #4CAF50;
  color: white;
}

.connect-button:hover {
  background-color: #45a049;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.view-profile-button {
  background-color: #f8f9fa;
  color: #2c3e50;
  border: 2px solid #ddd;
}

.view-profile-button:hover {
  background-color: #e9ecef;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

@media (max-width: 1200px) {
  .find-partners-container {
    padding: 1.5rem;
  }
}

@media (max-width: 768px) {
  .find-partners-container {
    padding: 1rem;
  }

  .header h1 {
    font-size: 2rem;
  }

  .search-section {
    padding: 1.5rem;
  }

  .partner-card {
    padding: 1.5rem;
  }
}

@media (max-width: 480px) {
  .header h1 {
    font-size: 1.75rem;
  }

  .search-section {
    padding: 1.25rem;
  }

  .partner-card {
    padding: 1.25rem;
  }

  .partner-actions {
    flex-direction: column;
  }
}
</style> 