<template>
  <div class="profile-container">
    <div class="profile-header">
      <h1>My Profile</h1>
    </div>

    <div class="profile-content">
      <div class="profile-section">
        <h2>Personal Information</h2>
        <form @submit.prevent="updateProfile" class="profile-form">
          <div class="form-group">
            <label for="name">Name</label>
            <input
              type="text"
              id="name"
              v-model="profile.name"
              required
              placeholder="Your name"
            />
          </div>

          <div class="form-group">
            <label for="email">Email</label>
            <input
              type="email"
              id="email"
              v-model="profile.email"
              required
              placeholder="Your email"
              disabled
            />
          </div>

          <div class="form-group">
            <label for="major">Major</label>
            <input
              type="text"
              id="major"
              v-model="profile.major"
              required
              placeholder="Your major"
            />
          </div>

          <div class="form-group">
            <label for="bio">Bio</label>
            <textarea
              id="bio"
              v-model="profile.bio"
              required
              placeholder="Tell us about yourself"
              rows="4"
            ></textarea>
          </div>

          <button type="submit" class="save-button" :disabled="loading">
            {{ loading ? 'Saving...' : 'Save Changes' }}
          </button>
        </form>
      </div>

      <div class="profile-section">
        <h2>Account Settings</h2>
        <div class="settings-group">
          <button @click="changePassword" class="settings-button">
            Change Password
          </button>
          <button @click="deleteAccount" class="settings-button danger">
            Delete Account
          </button>
          <button @click="logout" class="settings-button danger">
            Logout
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'Profile',
  data() {
    return {
      profile: {
        name: '',
        email: '',
        major: '',
        bio: ''
      },
      loading: false
    }
  },
  methods: {
    async fetchProfile() {
      try {
        const response = await axios.get('http://localhost:8080/api/users/profile', {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        })
        this.profile = response.data
      } catch (error) {
        console.error('Error fetching profile:', error)
      }
    },
    async updateProfile() {
      this.loading = true
      try {
        await axios.put('http://localhost:8080/api/users/profile', this.profile, {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        })
        alert('Profile updated successfully!')
      } catch (error) {
        console.error('Error updating profile:', error)
        alert('Failed to update profile. Please try again.')
      } finally {
        this.loading = false
      }
    },
    async deleteAccount() {
      if (!confirm('Are you sure you want to delete your account? This action cannot be undone.')) {
        return
      }

      try {
        await axios.delete(`http://localhost:8080/api/users/${this.profile.id}`, {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        })
        
        // Clear local storage and redirect to login
        localStorage.removeItem('token')
        this.$router.push('/auth/login')
        alert('Account deleted successfully')
      } catch (error) {
        console.error('Error deleting account:', error)
        alert('Failed to delete account. Please try again.')
      }
    },
    changePassword() {
      // Implement password change functionality
      console.log('Change password clicked')
    },
    logout() {
      localStorage.removeItem('token')
      this.$router.push('/auth/login')
    }
  },
  mounted() {
    this.fetchProfile()
  }
}
</script>

<style scoped>
.profile-container {
  max-width: 1920px;
  margin: 0 auto;
  padding: 2rem;
}

.profile-header {
  text-align: center;
  margin-bottom: 3rem;
}

.profile-header h1 {
  font-size: 2.5rem;
  color: #2c3e50;
  font-weight: 600;
}

.profile-content {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 2rem;
}

.profile-section {
  background: white;
  padding: 2rem;
  border-radius: 12px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.profile-section h2 {
  color: #2c3e50;
  font-size: 1.8rem;
  margin-bottom: 2rem;
}

.profile-form {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

label {
  color: #666;
  font-size: 1.1rem;
  font-weight: 500;
}

input, textarea {
  padding: 1rem;
  border: 2px solid #ddd;
  border-radius: 8px;
  font-size: 1.1rem;
  transition: all 0.3s ease;
}

input:focus, textarea:focus {
  outline: none;
  border-color: #4CAF50;
  box-shadow: 0 0 0 3px rgba(76, 175, 80, 0.1);
}

input:disabled {
  background-color: #f5f5f5;
  cursor: not-allowed;
}

textarea {
  resize: vertical;
  min-height: 120px;
}

.save-button {
  padding: 1rem 2rem;
  background-color: #4CAF50;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 1.1rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-top: 1rem;
}

.save-button:hover:not(:disabled) {
  background-color: #45a049;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.save-button:disabled {
  background-color: #cccccc;
  cursor: not-allowed;
}

.settings-group {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.settings-button {
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

.settings-button:hover {
  background-color: #45a049;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.settings-button.danger {
  background-color: #dc3545;
}

.settings-button.danger:hover {
  background-color: #c82333;
}

@media (max-width: 1200px) {
  .profile-container {
    padding: 1.5rem;
  }
}

@media (max-width: 992px) {
  .profile-content {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .profile-container {
    padding: 1rem;
  }

  .profile-header h1 {
    font-size: 2rem;
  }

  .profile-section {
    padding: 1.5rem;
  }

  .profile-section h2 {
    font-size: 1.5rem;
  }
}

@media (max-width: 480px) {
  .profile-header h1 {
    font-size: 1.75rem;
  }

  .profile-section {
    padding: 1.25rem;
  }

  .profile-section h2 {
    font-size: 1.3rem;
  }

  input, textarea {
    padding: 0.875rem;
    font-size: 1rem;
  }

  .save-button, .settings-button {
    padding: 0.875rem;
    font-size: 1rem;
  }
}
</style> 