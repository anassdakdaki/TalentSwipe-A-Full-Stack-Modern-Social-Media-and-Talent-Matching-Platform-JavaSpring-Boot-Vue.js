<template>
  <div class="profile-page">
    <h2>My Profile</h2>
    <div v-if="loading">Loading profile...</div>
    <div v-if="error">{{ error }}</div>
    <div v-if="profile">
      <form @submit.prevent="updateProfile">
        <!-- Top Section - Avatar + Basic Info -->
        <div class="profile-section">
          <h3>Basic Information</h3>
          <div class="profile-picture-section">
            <div class="profile-picture-container">
              <div class="profile-picture-preview" :class="{ 'has-image': profile.profilePictureUrl }">
                <img 
                  v-if="profile.profilePictureUrl" 
                  :src="profile.profilePictureUrl" 
                  alt="Profile Picture"
                  class="preview-image"
                />
                <div v-else class="placeholder">
                  <i class="fas fa-user"></i>
                  <span>No image selected</span>
                </div>
              </div>
              <div class="upload-controls">
                <label for="profilePicture" class="upload-button">
                  {{ profile.profilePictureUrl ? 'Change Picture' : 'Upload Picture' }}
                </label>
                <input 
                  type="file" 
                  id="profilePicture" 
                  accept="image/*"
                  @change="handlePictureUpload" 
                  class="file-input"
                />
                <button 
                  v-if="profile.profilePictureUrl" 
                  type="button" 
                  @click="removeProfilePicture" 
                  class="remove-picture-button"
                >
                  Remove Picture
                </button>
              </div>
            </div>
          </div>

          <div class="form-group">
            <label for="name">Full Name</label>
            <input type="text" id="name" v-model="profile.name" required>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label for="age">Age</label>
              <input type="number" id="age" v-model="profile.age" min="18" max="100">
            </div>
            <div class="form-group">
              <label for="gender">Gender</label>
              <select id="gender" v-model="profile.gender">
                <option value="">Select Gender</option>
                <option value="male">Male</option>
                <option value="female">Female</option>
                <option value="other">Other</option>
                <option value="prefer-not-to-say">Prefer not to say</option>
              </select>
            </div>
          </div>

          <div class="form-group">
            <label for="location">Location (Optional)</label>
            <input type="text" id="location" v-model="profile.location" placeholder="City, Country">
          </div>
        </div>

        <!-- Education / Profession Section -->
        <div class="profile-section">
          <h3>Education & Profession</h3>
          <div class="form-group">
            <label for="university">University / Company</label>
            <input type="text" id="university" v-model="profile.university" placeholder="e.g., XYZ University">
          </div>
          <div class="form-group">
            <label for="major">Major / Role</label>
            <input type="text" id="major" v-model="profile.major" placeholder="e.g., Software Engineering">
          </div>
        </div>

        <!-- About Me Section -->
        <div class="profile-section">
          <h3>About Me</h3>
          <div class="form-group">
            <label for="bio">Bio</label>
            <textarea 
              id="bio" 
              v-model="profile.bio" 
              rows="4" 
              placeholder="Tell us about yourself..."
            ></textarea>
          </div>
        </div>

        <!-- Looking For Section -->
        <div class="profile-section">
          <h3>Looking For</h3>
          <div class="checkbox-group">
            <label class="checkbox-item">
              <input type="checkbox" v-model="profile.lookingFor.studyPartner">
              <span>Study Partner</span>
            </label>
            <label class="checkbox-item">
              <input type="checkbox" v-model="profile.lookingFor.languageExchange">
              <span>Language Exchange</span>
            </label>
            <label class="checkbox-item">
              <input type="checkbox" v-model="profile.lookingFor.friendship">
              <span>Friendship</span>
            </label>
            <label class="checkbox-item">
              <input type="checkbox" v-model="profile.lookingFor.networking">
              <span>Networking</span>
            </label>
            <label class="checkbox-item">
              <input type="checkbox" v-model="profile.lookingFor.community">
              <span>Community</span>
            </label>
          </div>
        </div>

        <!-- Interests Section -->
        <div class="profile-section">
          <h3>Interests</h3>
          <div class="tags-input">
            <input 
              type="text" 
              v-model="newInterest" 
              @keydown.enter.prevent="addInterest"
              placeholder="Add interests (press Enter)"
            />
            <div class="tags">
              <span v-for="(interest, index) in profile.interests" :key="index" class="tag">
                #{{ interest }}
                <button @click="removeInterest(index)" class="remove-tag">&times;</button>
              </span>
            </div>
          </div>
        </div>

        <!-- Language Skills Section -->
        <div class="profile-section">
          <h3>Language Skills</h3>
          <div class="language-section">
            <div class="language-group">
              <h4>I Speak</h4>
              <div class="language-selector">
                <select v-model="newLanguage.name" class="language-select">
                  <option value="">Select Language</option>
                  <option v-for="lang in availableLanguages" :key="lang" :value="lang">
                    {{ lang }}
                  </option>
                </select>
                <select v-model="newLanguage.level" class="level-select">
                  <option value="">Select Level</option>
                  <option value="native">Native</option>
                  <option value="fluent">Fluent</option>
                  <option value="advanced">Advanced</option>
                  <option value="intermediate">Intermediate</option>
                  <option value="beginner">Beginner</option>
                </select>
                <button type="button" @click="addLanguage" class="add-button">Add</button>
              </div>
              <div class="language-list">
                <div v-for="(lang, index) in profile.languages" :key="index" class="language-item">
                  <span>{{ lang.name }} ({{ lang.level }})</span>
                  <button type="button" @click="removeLanguage(index)" class="remove-button">&times;</button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Social Links Section -->
        <div class="profile-section">
          <h3>Social Links (Optional)</h3>
          <div class="form-group">
            <label for="github">GitHub</label>
            <input type="url" id="github" v-model="profile.socialLinks.github" placeholder="https://github.com/username">
          </div>
          <div class="form-group">
            <label for="linkedin">LinkedIn</label>
            <input type="url" id="linkedin" v-model="profile.socialLinks.linkedin" placeholder="https://linkedin.com/in/username">
          </div>
          <div class="form-group">
            <label for="instagram">Instagram</label>
            <input type="url" id="instagram" v-model="profile.socialLinks.instagram" placeholder="https://instagram.com/username">
          </div>
        </div>

        <!-- Action Buttons -->
        <div class="action-buttons">
          <button type="submit" class="save-button" :disabled="loading">
            {{ loading ? 'Saving...' : 'Save Changes' }}
          </button>
          <button type="button" @click="resetProfile" class="reset-button">
            Reset Profile
          </button>
          <button type="button" @click="logout" class="logout-button">
            Logout
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script>
import axios from 'axios';
const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080').replace(/\/+$/, '');

export default {
  name: 'ProfilePage',
  data() {
    return {
      profile: {
        name: '',
        age: null,
        gender: '',
        location: '',
        university: '',
        major: '',
        bio: '',
        profilePictureUrl: '',
        interests: [],
        languages: [],
        lookingFor: {
          studyPartner: false,
          languageExchange: false,
          friendship: false,
          networking: false,
          community: false
        },
        socialLinks: {
          github: '',
          linkedin: '',
          instagram: ''
        }
      },
      newInterest: '',
      newLanguage: { name: '', level: '' },
      loading: false,
      error: null,
      selectedFile: null,
      availableLanguages: [
        'English', 'Spanish', 'French', 'German', 'Italian', 'Portuguese',
        'Russian', 'Chinese', 'Japanese', 'Korean', 'Arabic', 'Hindi'
      ]
    };
  },
  created() {
    this.fetchProfile();
  },
  methods: {
    async fetchProfile() {
      try {
        const token = localStorage.getItem('token');
        const response = await axios.get(`${API_BASE_URL}/api/profiles/me`, {
          headers: {
            Authorization: `Bearer ${token}`
          }
        });
        this.profile = response.data;

        // Initialize nested objects if they are null
        if (!this.profile.lookingFor) {
          this.profile.lookingFor = {
            studyPartner: false,
            languageExchange: false,
            friendship: false,
            networking: false,
            community: false
          };
        }
        if (!this.profile.socialLinks) {
          this.profile.socialLinks = { github: '', linkedin: '', instagram: '' };
        }
        if (!this.profile.interests) {
          this.profile.interests = [];
        }
        if (!this.profile.languages) {
          this.profile.languages = [];
        }

        // Prepend backend URL to profile picture URL if needed
        if (this.profile.profilePictureUrl && !this.profile.profilePictureUrl.startsWith('http')) {
          this.profile.profilePictureUrl = `${API_BASE_URL}${this.profile.profilePictureUrl}`;
        }

        this.loading = false;
      } catch (error) {
        this.error = 'Failed to fetch profile. Please try again later.';
        console.error('Error fetching profile:', error);
        this.loading = false;
      }
    },

    handlePictureUpload(event) {
      const file = event.target.files[0];
      if (!file) return;

      // Validate file type
      if (!file.type.startsWith('image/')) {
        this.error = 'Please select an image file';
        return;
      }

      // Validate file size (max 5MB)
      if (file.size > 5 * 1024 * 1024) {
        this.error = 'Image size should be less than 5MB';
        return;
      }

      this.selectedFile = file;

      // Create preview
      const reader = new FileReader();
      reader.onload = (e) => {
        this.profile.profilePictureUrl = e.target.result;
      };
      reader.readAsDataURL(file);
    },

    async removeProfilePicture() {
      try {
        const token = localStorage.getItem('token');
        await axios.delete(`${API_BASE_URL}/api/profiles/me/picture`, {
          headers: { Authorization: `Bearer ${token}` }
        });
        this.profile.profilePictureUrl = null;
        this.selectedFile = null;
      } catch (error) {
        console.error('Error removing profile picture:', error);
        this.error = 'Failed to remove profile picture';
      }
    },

    addInterest() {
      if (this.newInterest.trim() && !this.profile.interests.includes(this.newInterest.trim())) {
        this.profile.interests.push(this.newInterest.trim());
        this.newInterest = '';
      }
    },

    removeInterest(index) {
      this.profile.interests.splice(index, 1);
    },

    addLanguage() {
      if (this.newLanguage.name && this.newLanguage.level) {
        this.profile.languages.push({ ...this.newLanguage });
        this.newLanguage = { name: '', level: '' };
      }
    },

    removeLanguage(index) {
      this.profile.languages.splice(index, 1);
    },

    async updateProfile() {
      this.loading = true;
      this.error = null;

      try {
        const token = localStorage.getItem('token');
        const headers = { Authorization: `Bearer ${token}` };

        // Update profile data
        const profileData = { ...this.profile };
        delete profileData.user; // Remove user object if present

        await axios.put(`${API_BASE_URL}/api/profiles/me`, profileData, {
          headers: { ...headers, 'Content-Type': 'application/json' }
        });

        // Upload picture if selected
        if (this.selectedFile) {
          const formData = new FormData();
          formData.append('profilePicture', this.selectedFile);

          const pictureResponse = await axios.post(`${API_BASE_URL}/api/profiles/me/picture`, formData, {
            headers: { ...headers, 'Content-Type': 'multipart/form-data' }
          });

          if (pictureResponse.data.profilePictureUrl) {
            let pictureUrl = pictureResponse.data.profilePictureUrl;
            if (!pictureUrl.startsWith('http')) {
              pictureUrl = `${API_BASE_URL}${pictureUrl}`;
            }
            this.profile.profilePictureUrl = pictureUrl;
          }
        }

        this.selectedFile = null;
        this.error = null;
      } catch (error) {
        console.error('Error updating profile:', error);
        this.error = 'Failed to update profile. Please try again.';
      } finally {
        this.loading = false;
      }
    },

    resetProfile() {
      if (confirm('Are you sure you want to reset your profile? This will clear all your information.')) {
        this.fetchProfile(); // Reload the original profile data
      }
    },

    logout() {
      localStorage.removeItem('token');
      this.$router.push('/auth/login');
    }
  }
};
</script>

<style scoped>
.profile-page {
  min-height: 100vh;
  width: 100%;
  padding: 24px clamp(12px, 2vw, 28px) 120px;
  background: var(--theme-page-background);
  color: var(--theme-text-primary);
}

.profile-page h2 {
  width: min(var(--page-max-width), 100%);
  margin: 0 auto 16px;
  font-size: clamp(2rem, 2.9vw, 2.7rem);
  color: var(--theme-heading-color);
}

.profile-page > div {
  width: min(var(--page-max-width), 100%);
  margin: 0 auto;
}

.profile-page form {
  width: min(var(--page-max-width), 100%);
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--page-content-gap);
}

.profile-section {
  border: 1px solid var(--theme-surface-border);
  background: var(--theme-surface-elevated);
  border-radius: 18px;
  box-shadow: var(--theme-shadow-soft);
  padding: 18px;
}

.profile-section:nth-of-type(1),
.profile-section:nth-of-type(6),
.profile-section:nth-of-type(7) {
  grid-column: 1 / -1;
}

.profile-section h3 {
  color: var(--theme-heading-color);
  margin: 0 0 12px;
  font-size: 1.2rem;
}

.form-group {
  margin-bottom: 12px;
}

.form-group label {
  display: block;
  margin-bottom: 6px;
  color: var(--theme-text-secondary);
  font-size: 0.92rem;
  font-weight: 600;
}

.form-group input[type='text'],
.form-group input[type='number'],
.form-group input[type='url'],
.form-group textarea,
.tags-input input[type='text'],
.form-group select,
.language-selector select {
  width: 100%;
  box-sizing: border-box;
  padding: 11px 12px;
  border: 1px solid var(--theme-input-border);
  border-radius: 12px;
  background: var(--theme-input-bg);
  color: var(--theme-input-text);
  font-size: 0.95rem;
  outline: none;
}

.form-group input::placeholder,
.form-group textarea::placeholder,
.tags-input input::placeholder {
  color: var(--theme-input-placeholder);
}

.form-group input:focus,
.form-group textarea:focus,
.tags-input input:focus,
.form-group select:focus,
.language-selector select:focus {
  border-color: var(--theme-accent);
  box-shadow: 0 0 0 3px color-mix(in srgb, var(--theme-accent) 18%, transparent);
}

.form-group textarea {
  min-height: 110px;
  resize: vertical;
}

.form-row {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.profile-picture-section {
  margin-bottom: 16px;
}

.profile-picture-container {
  display: flex;
  gap: 16px;
  align-items: center;
}

.profile-picture-preview {
  width: 110px;
  height: 110px;
  border-radius: 50%;
  border: 1px solid var(--theme-surface-border);
  background: var(--theme-surface-1);
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

.profile-picture-preview .preview-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.profile-picture-preview .placeholder {
  color: var(--theme-text-subtle);
  text-align: center;
  font-size: 1.8rem;
}

.profile-picture-preview .placeholder span {
  display: block;
  font-size: 0.78rem;
  margin-top: 4px;
}

.upload-controls {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.upload-button,
.remove-picture-button {
  border-radius: 10px;
  border: 1px solid var(--theme-button-secondary-border);
  background: var(--theme-button-secondary-bg);
  color: var(--theme-button-secondary-text);
  padding: 8px 12px;
  cursor: pointer;
  font-size: 0.9rem;
  font-weight: 700;
}

.upload-button:hover,
.remove-picture-button:hover {
  filter: brightness(1.03);
}

.file-input {
  display: none;
}

.checkbox-group {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.checkbox-item {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  border: 1px solid var(--theme-chip-border);
  background: var(--theme-chip-bg);
  color: var(--theme-chip-text);
  padding: 8px 12px;
  border-radius: 999px;
  font-size: 0.9rem;
}

.checkbox-item input[type='checkbox'] {
  width: 14px;
  height: 14px;
}

.tags-input input[type='text'] {
  margin-bottom: 10px;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag {
  border: 1px solid var(--theme-chip-border);
  background: var(--theme-chip-bg);
  color: var(--theme-chip-text);
  padding: 6px 10px;
  border-radius: 999px;
  font-size: 0.86rem;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-weight: 600;
}

.remove-tag {
  background: transparent;
  border: none;
  color: inherit;
  cursor: pointer;
  padding: 0;
  font-size: 0.9rem;
}

.language-group h4 {
  color: var(--theme-text-primary);
  margin-bottom: 10px;
  font-size: 1rem;
}

.language-selector {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr) 120px;
  gap: 8px;
  margin-bottom: 10px;
}

.language-selector .add-button {
  border: none;
  border-radius: 10px;
  background: var(--theme-button-primary-bg);
  color: var(--theme-button-primary-text);
  font-weight: 700;
  cursor: pointer;
}

.language-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.language-item {
  border: 1px solid var(--theme-surface-border);
  background: var(--theme-surface-1);
  color: var(--theme-text-primary);
  padding: 8px 10px;
  border-radius: 10px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.language-item .remove-button {
  background: none;
  border: none;
  color: var(--theme-text-secondary);
  cursor: pointer;
  font-size: 1rem;
}

.action-buttons {
  grid-column: 1 / -1;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.save-button,
.reset-button,
.logout-button {
  border-radius: 12px;
  padding: 10px 16px;
  font-size: 0.95rem;
  cursor: pointer;
  font-weight: 700;
}

.save-button {
  border: none;
  background: var(--theme-button-primary-bg);
  color: var(--theme-button-primary-text);
  box-shadow: var(--theme-button-primary-shadow);
}

.reset-button {
  border: 1px solid var(--theme-button-secondary-border);
  background: var(--theme-button-secondary-bg);
  color: var(--theme-button-secondary-text);
}

.logout-button {
  border: none;
  background: var(--theme-button-danger-bg);
  color: var(--theme-button-danger-text);
}

.save-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

@media (max-width: 1080px) {
  .profile-page form {
    grid-template-columns: 1fr;
  }

  .profile-section:nth-of-type(1),
  .profile-section:nth-of-type(6),
  .profile-section:nth-of-type(7) {
    grid-column: auto;
  }
}

@media (max-width: 760px) {
  .profile-page {
    padding: 14px 10px 94px;
  }

  .profile-picture-container {
    flex-direction: column;
    align-items: flex-start;
  }

  .form-row,
  .language-selector {
    grid-template-columns: 1fr;
  }

  .action-buttons {
    flex-direction: column;
  }

  .action-buttons button {
    width: 100%;
  }
}
</style>
