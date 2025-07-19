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
              <span>🧠 Study Partner</span>
            </label>
            <label class="checkbox-item">
              <input type="checkbox" v-model="profile.lookingFor.languageExchange">
              <span>🗣️ Language Exchange</span>
            </label>
            <label class="checkbox-item">
              <input type="checkbox" v-model="profile.lookingFor.friendship">
              <span>💬 Friendship</span>
            </label>
            <label class="checkbox-item">
              <input type="checkbox" v-model="profile.lookingFor.networking">
              <span>🤝 Networking</span>
            </label>
            <label class="checkbox-item">
              <input type="checkbox" v-model="profile.lookingFor.community">
              <span>🌍 Community</span>
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
        const response = await axios.get('http://localhost:8080/api/profiles/me', {
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
          this.profile.profilePictureUrl = `http://localhost:8080${this.profile.profilePictureUrl}`;
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
        await axios.delete('http://localhost:8080/api/profiles/me/picture', {
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

        await axios.put('http://localhost:8080/api/profiles/me', profileData, {
          headers: { ...headers, 'Content-Type': 'application/json' }
        });

        // Upload picture if selected
        if (this.selectedFile) {
          const formData = new FormData();
          formData.append('profilePicture', this.selectedFile);

          const pictureResponse = await axios.post('http://localhost:8080/api/profiles/me/picture', formData, {
            headers: { ...headers, 'Content-Type': 'multipart/form-data' }
          });

          if (pictureResponse.data.profilePictureUrl) {
            let pictureUrl = pictureResponse.data.profilePictureUrl;
            if (!pictureUrl.startsWith('http')) {
              pictureUrl = `http://localhost:8080${pictureUrl}`;
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
@import url('https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;600;700&display=swap');

.profile-page {
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%); /* Dark gradient background */
  color: #e0e0e0; /* Light text */
  min-height: 100vh; /* Ensure it takes at least the full viewport height */
  width: 100%; /* Ensure the profile page container takes full width */
  /* Removed width: 100% and padding to see if it resolves side spaces */
  /* padding: 20px; */
  box-sizing: border-box; /* Include padding in width */
  overflow-x: hidden; /* Hide horizontal overflow */
  display: flex; /* Use flexbox for centering content */
  flex-direction: column; /* Stack content vertically */
  align-items: center; /* Center content horizontally */
  justify-content: center; /* Center content vertically */
}

.profile-page h2 {
  text-align: center;
  color: #64ffda;
  margin-bottom: 20px;
}

.profile-page form {
    width: 100%;
    max-width: 800px; /* Limit the form width */
    background: rgba(255, 255, 255, 0.1); /* Glassmorphism background */
    backdrop-filter: blur(10px);
    -webkit-backdrop-filter: blur(10px);
    border: 1px solid rgba(255, 255, 255, 0.2);
    border-radius: 20px;
    padding: 30px; /* Keep padding on the form itself */
    box-sizing: border-box; /* Ensure padding is included in form's width */
    box-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.37);
    margin: 20px auto; /* Center the form and add vertical margin */
}

.profile-section {
    margin-bottom: 30px;
    padding-bottom: 20px;
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.profile-section:last-child {
    border-bottom: none;
    margin-bottom: 0;
    padding-bottom: 0;
}

.profile-section h3 {
    color: #64ffda;
    margin-bottom: 15px;
    font-size: 1.3rem;
}

/* Add styles for form groups, labels, inputs, etc. */
.form-group {
    margin-bottom: 15px;
}

.form-group label {
    display: block;
    margin-bottom: 5px;
    color: #e0e0e0;
    font-size: 1rem;
}

.form-group input[type="text"],
.form-group input[type="number"],
.form-group input[type="url"],
.form-group textarea,
.tags-input input[type="text"] {
    width: 100%;
    padding: 12px 15px;
    background: #1a1a2e;
    border: 1px solid rgba(255, 255, 255, 0.2);
    border-radius: 10px;
    color: #e0e0e0;
    font-size: 1rem;
    box-shadow: inset 0 2px 5px rgba(0, 0, 0, 0.2);
    transition: all 0.3s ease;
    outline: none;
}

.form-group select,
.language-selector select {
    width: 100%;
    padding: 12px 15px;
    background: #1a1a2e; /* Consistent background */
    border: 1px solid rgba(255, 255, 255, 0.2);
    border-radius: 10px;
    color: #e0e0e0; /* Consistent text color */
    font-size: 1rem;
    box-shadow: inset 0 2px 5px rgba(0, 0, 0, 0.2);
    transition: all 0.3s ease;
    outline: none;
    -webkit-appearance: none; /* Remove default arrow on WebKit browsers */
    -moz-appearance: none; /* Remove default arrow on Firefox */
    appearance: none; /* Remove default arrow */
    background-image: url('data:image/svg+xml;charset=UTF-8,%3Csvg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="%2364ffda" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"%3E%3Cpolyline points="6 9 12 15 18 9">%3C/polyline%3E%3C/svg%3E');
    background-repeat: no-repeat;
    background-position: right 15px center;
    background-size: 20px;
    cursor: pointer;
}

.form-group input[type="text"]::placeholder,
.form-group input[type="number"]::placeholder,
.form-group input[type="url"]::placeholder,
.form-group textarea::placeholder,
.tags-input input[type="text"]::placeholder {
    color: rgba(224, 224, 224, 0.6);
}

.form-group input[type="text"]:focus,
.form-group input[type="number"]:focus,
.form-group input[type="url"]:focus,
.form-group textarea:focus,
.tags-input input[type="text"]:focus {
    outline: none;
    border-color: #64ffda;
}

.form-group select:focus,
.language-selector select:focus {
    outline: none;
    border-color: #64ffda;
}

.form-group select option {
  background-color: #1a1a2e; /* Dark background for options */
  color: #e0e0e0; /* Light text for options */
  padding: 10px 15px;
  font-size: 1rem; /* Ensure consistent font size for options */
}

.form-group select option:checked,
.form-group select option:hover {
  background-color: #64ffda; /* Accent color on hover/selected */
  color: #1a1a2e; /* Dark text on hover/selected */
}

.form-row {
    display: flex;
    gap: 20px;
}

.form-row .form-group {
    flex: 1;
}

/* Profile Picture Styles */
.profile-picture-section {
    margin-bottom: 20px;
    display: flex;
    justify-content: center;
}

.profile-picture-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 15px;
}

.profile-picture-preview {
    width: 120px;
    height: 120px;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.1);
    border: 2px solid rgba(255, 255, 255, 0.3);
    display: flex;
    align-items: center;
    justify-content: center;
    overflow: hidden;
}

.profile-picture-preview .preview-image {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.profile-picture-preview .placeholder {
     font-size: 3rem;
     color: rgba(255, 255, 255, 0.5);
     text-align: center;
}

.profile-picture-preview .placeholder span {
    display: block;
    font-size: 0.8rem;
    margin-top: 5px;
}

.upload-controls {
    display: flex;
    gap: 10px;
}

.upload-button,
.remove-picture-button {
    background: rgba(100, 255, 218, 0.1);
    color: #64ffda;
    padding: 8px 15px;
    border: 1px solid rgba(100, 255, 218, 0.3);
    border-radius: 8px;
    cursor: pointer;
    font-size: 0.9rem;
    transition: background-color 0.3s ease, border-color 0.3s ease;
}

.upload-button:hover,
.remove-picture-button:hover {
    background: rgba(100, 255, 218, 0.2);
    border-color: #64ffda;
}

.file-input {
    display: none;
}

/* Checkbox Group Styles */
.checkbox-group {
    display: flex;
    flex-wrap: wrap;
    gap: 15px;
}

.checkbox-item {
    display: flex;
    align-items: center;
    color: #e0e0e0;
    font-size: 1rem;
    cursor: pointer;
}

.checkbox-item input[type="checkbox"] {
    margin-right: 8px;
    /* Custom checkbox styling if needed */
}

/* Tags Input Styles */
.tags-input input[type="text"] {
    margin-bottom: 10px;
}

.tags {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
}

.tag {
    background: rgba(255, 255, 255, 0.15);
    color: #ffffff;
    padding: 6px 12px;
    border-radius: 15px;
    font-size: 0.9rem;
    backdrop-filter: blur(5px);
    border: 1px solid rgba(255, 255, 255, 0.2);
    display: flex;
    align-items: center;
    gap: 5px;
}

.remove-tag {
    background: none;
    border: none;
    color: rgba(255, 255, 255, 0.7);
    cursor: pointer;
    font-size: 1rem;
    padding: 0;
    transition: color 0.3s ease;
}

.remove-tag:hover {
    color: #ffffff;
}

/* Language Section Styles */
.language-group {
    margin-bottom: 20px;
}

.language-group h4 {
    color: #64ffda;
    margin-bottom: 10px;
    font-size: 1.1rem;
}

.language-selector {
    display: flex;
    gap: 10px;
    margin-bottom: 15px;
}

.language-selector select {
    flex: 1;
}

.language-selector .add-button {
    background: #64ffda;
    color: #1a1a2e;
    padding: 10px 15px;
    border: none;
    border-radius: 8px;
    cursor: pointer;
    font-size: 1rem;
    transition: background-color 0.3s ease;
}

.language-selector .add-button:hover {
    background: #3be8b0;
}

.language-list {
    display: flex;
    flex-direction: column;
    gap: 8px;
}

.language-item {
    background: rgba(255, 255, 255, 0.1);
    color: #e0e0e0;
    padding: 8px 12px;
    border-radius: 8px;
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.language-item .remove-button {
    background: none;
    border: none;
    color: rgba(255, 255, 255, 0.7);
    cursor: pointer;
    font-size: 1rem;
    padding: 0;
    transition: color 0.3s ease;
}

.language-item .remove-button:hover {
    color: #ffffff;
}

/* Action Buttons Styles */
.action-buttons {
    display: flex;
    gap: 15px;
    justify-content: flex-end;
    margin-top: 30px;
    padding-top: 20px;
    border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.save-button,
.reset-button,
.logout-button {
    padding: 10px 20px;
    border: none;
    border-radius: 8px;
    font-size: 1rem;
    cursor: pointer;
    transition: background-color 0.3s ease, opacity 0.3s ease;
}

.save-button {
    background: #64ffda;
    color: #1a1a2e;
}

.save-button:hover:not(:disabled) {
    background: #3be8b0;
}

.save-button:disabled {
    opacity: 0.5;
    cursor: not-allowed;
}

.reset-button {
    background: rgba(255, 255, 255, 0.15);
    color: #e0e0e0;
    border: 1px solid rgba(255, 255, 255, 0.3);
}

.reset-button:hover {
    background: rgba(255, 255, 255, 0.25);
}

.logout-button {
    background: rgba(255, 82, 82, 0.8);
    color: white;
}

.logout-button:hover {
    background: rgba(255, 82, 82, 1);
}

/* Responsive Adjustments */
@media screen and (max-width: 768px) {
    .profile-page form {
        padding: 20px;
    }

    .profile-section {
        margin-bottom: 20px;
        padding-bottom: 15px;
    }

    .profile-section h3 {
        font-size: 1.2rem;
    }

    .form-row {
        flex-direction: column;
        gap: 15px;
    }

    .upload-controls {
        flex-direction: column;
        gap: 10px;
        width: 100%;
    }

    .upload-button,
    .remove-picture-button {
        text-align: center;
    }

     .language-selector {
        flex-direction: column;
        gap: 10px;
    }

    .action-buttons {
        flex-direction: column;
        gap: 10px;
    }

    .action-buttons button {
        width: 100%;
        text-align: center;
    }
}
</style> 