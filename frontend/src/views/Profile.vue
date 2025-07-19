<template>
  <div class="profile-container">
    <div class="profile-header">
      <h1>My Profile</h1>
    </div>

    <div class="profile-content">
      <div class="profile-section">
        <h2>Profile Picture</h2>
        <div class="profile-picture-section">
          <div class="current-picture" v-if="profile.profilePicture">
            <img :src="profile.profilePicture" alt="Profile Picture" />
          </div>
          <div class="upload-picture">
            <input
              type="file"
              id="profilePicture"
              accept="image/*"
              @change="handlePictureUpload"
              class="file-input"
            />
            <label for="profilePicture" class="upload-button">
              {{ profile.profilePicture ? 'Change Picture' : 'Upload Picture' }}
            </label>
          </div>
        </div>

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
            <label for="location">Location</label>
            <input
              type="text"
              id="location"
              v-model="profile.location"
              required
              placeholder="Your city/campus"
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

          <div class="form-group">
            <label>Interests & Hobbies</label>
            <div class="tags-input">
              <input
                type="text"
                v-model="newInterest"
                @keydown.enter.prevent="addInterest"
                placeholder="Add interests (press Enter)"
              />
              <div class="tags">
                <span v-for="(interest, index) in profile.interests" :key="index" class="tag">
                  {{ interest }}
                  <button @click="removeInterest(index)" class="remove-tag">&times;</button>
                </span>
              </div>
            </div>
          </div>

          <div class="form-group">
            <label>Languages</label>
            <div class="languages-section">
              <div v-for="(language, index) in profile.languages" :key="index" class="language-item">
                <select v-model="language.name" class="language-select">
                  <option value="">Select Language</option>
                  <option v-for="lang in availableLanguages" :key="lang" :value="lang">
                    {{ lang }}
                  </option>
                </select>
                <select v-model="language.level" class="level-select">
                  <option value="">Level</option>
                  <option value="native">Native</option>
                  <option value="fluent">Fluent</option>
                  <option value="advanced">Advanced</option>
                  <option value="intermediate">Intermediate</option>
                  <option value="beginner">Beginner</option>
                </select>
                <button @click="removeLanguage(index)" class="remove-button">Remove</button>
              </div>
              <button @click="addLanguage" class="add-button">Add Language</button>
            </div>
          </div>

          <div class="form-group">
            <label>Study Preferences</label>
            <div class="study-preferences">
              <div class="preference-item">
                <label>
                  <input type="checkbox" v-model="profile.studyPreferences.onlineStudy">
                  Online Study
                </label>
              </div>
              <div class="preference-item">
                <label>
                  <input type="checkbox" v-model="profile.studyPreferences.inPersonStudy">
                  In-Person Study
                </label>
              </div>
              <div class="preference-item">
                <label>
                  <input type="checkbox" v-model="profile.studyPreferences.groupStudy">
                  Group Study
                </label>
              </div>
            </div>
          </div>

          <div class="form-group">
            <label>Social Links</label>
            <input
              type="url"
              v-model="profile.socialLinks.github"
              placeholder="GitHub Profile URL"
            />
            <input
              type="url"
              v-model="profile.socialLinks.linkedin"
              placeholder="LinkedIn Profile URL"
            />
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
        bio: '',
        location: '',
        profilePicture: '',
        interests: [],
        languages: [],
        studyPreferences: {
          onlineStudy: false,
          inPersonStudy: false,
          groupStudy: false
        },
        socialLinks: {
          github: '',
          linkedin: ''
        }
      },
      newInterest: '',
      loading: false,
      availableLanguages: [
        'English', 'Spanish', 'French', 'German', 'Italian', 'Portuguese',
        'Russian', 'Chinese', 'Japanese', 'Korean', 'Arabic', 'Hindi'
      ]
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
    async handlePictureUpload(event) {
      const file = event.target.files[0]
      if (!file) return

      const formData = new FormData()
      formData.append('profilePicture', file)

      try {
        const response = await axios.post('http://localhost:8080/api/users/profile/picture', formData, {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`,
            'Content-Type': 'multipart/form-data'
          }
        })
        this.profile.profilePicture = response.data.profilePictureUrl
      } catch (error) {
        console.error('Error uploading picture:', error)
        alert('Failed to upload picture. Please try again.')
      }
    },
    addInterest() {
      if (this.newInterest.trim() && !this.profile.interests.includes(this.newInterest.trim())) {
        this.profile.interests.push(this.newInterest.trim())
        this.newInterest = ''
      }
    },
    removeInterest(index) {
      this.profile.interests.splice(index, 1)
    },
    addLanguage() {
      this.profile.languages.push({ name: '', level: '' })
    },
    removeLanguage(index) {
      this.profile.languages.splice(index, 1)
    },
    changePassword() {
      // Implement password change functionality
      console.log('Change password clicked')
    },
    logout() {
      localStorage.removeItem('token')
      this.$router.push('/login')
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

.profile-picture-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
  margin-bottom: 2rem;
}

.current-picture {
  width: 200px;
  height: 200px;
  border-radius: 50%;
  overflow: hidden;
  border: 3px solid #4CAF50;
}

.current-picture img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.file-input {
  display: none;
}

.upload-button {
  padding: 0.8rem 1.5rem;
  background-color: #4CAF50;
  color: white;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.upload-button:hover {
  background-color: #45a049;
}

.tags-input {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.tag {
  background-color: #e0e0e0;
  padding: 0.5rem 1rem;
  border-radius: 20px;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.remove-tag {
  background: none;
  border: none;
  color: #666;
  cursor: pointer;
  font-size: 1.2rem;
  padding: 0;
}

.languages-section {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.language-item {
  display: flex;
  gap: 1rem;
  align-items: center;
}

.language-select, .level-select {
  flex: 1;
  padding: 0.8rem;
  border: 2px solid #ddd;
  border-radius: 8px;
}

.remove-button {
  padding: 0.8rem;
  background-color: #ff4444;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
}

.add-button {
  padding: 0.8rem;
  background-color: #4CAF50;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  margin-top: 0.5rem;
}

.study-preferences {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.preference-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.preference-item input[type="checkbox"] {
  width: 20px;
  height: 20px;
}
</style> 