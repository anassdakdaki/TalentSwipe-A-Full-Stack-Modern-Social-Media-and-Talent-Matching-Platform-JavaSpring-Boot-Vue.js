<template>
  <div class="resources-container">
    <div class="header">
      <h1>Study Resources</h1>
      <p>Share and discover study materials with other students</p>
    </div>

    <div class="actions">
      <button @click="showUploadModal = true" class="upload-button">
        Upload Resource
      </button>
    </div>

    <div class="resources-section">
      <div class="search-bar">
        <input
          type="text"
          v-model="searchQuery"
          placeholder="Search resources..."
          @input="searchResources"
        />
        <select v-model="filterCategory" @change="searchResources">
          <option value="">All Categories</option>
          <option value="notes">Notes</option>
          <option value="assignments">Assignments</option>
          <option value="books">Books</option>
          <option value="videos">Videos</option>
          <option value="other">Other</option>
        </select>
      </div>

      <div class="resources-grid">
        <div v-if="loading" class="loading">
          Loading resources...
        </div>
        <div v-else-if="filteredResources.length === 0" class="no-results">
          No resources found matching your criteria.
        </div>
        <div v-else class="resource-cards">
          <div v-for="resource in filteredResources" :key="resource.id" class="resource-card">
            <div class="resource-info">
              <h3>{{ resource.title }}</h3>
              <p class="category">{{ resource.category }}</p>
              <p class="description">{{ resource.description }}</p>
              <p class="uploader">Uploaded by: {{ resource.uploader }}</p>
              <p class="date">Uploaded: {{ formatDate(resource.uploadDate) }}</p>
            </div>
            <div class="resource-actions">
              <button @click="downloadResource(resource)" class="download-button">
                Download
              </button>
              <button @click="shareResource(resource)" class="share-button">
                Share
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Upload Resource Modal -->
    <div v-if="showUploadModal" class="modal">
      <div class="modal-content">
        <h2>Upload Resource</h2>
        <form @submit.prevent="uploadResource">
          <div class="form-group">
            <label for="title">Title</label>
            <input
              type="text"
              id="title"
              v-model="newResource.title"
              required
              placeholder="Enter resource title"
            />
          </div>
          <div class="form-group">
            <label for="category">Category</label>
            <select id="category" v-model="newResource.category" required>
              <option value="">Select a category</option>
              <option value="notes">Notes</option>
              <option value="assignments">Assignments</option>
              <option value="books">Books</option>
              <option value="videos">Videos</option>
              <option value="other">Other</option>
            </select>
          </div>
          <div class="form-group">
            <label for="description">Description</label>
            <textarea
              id="description"
              v-model="newResource.description"
              required
              placeholder="Describe your resource"
              rows="4"
            ></textarea>
          </div>
          <div class="form-group">
            <label for="file">File</label>
            <input
              type="file"
              id="file"
              @change="handleFileUpload"
              required
            />
          </div>
          <div class="modal-actions">
            <button type="button" @click="showUploadModal = false" class="cancel-button">
              Cancel
            </button>
            <button type="submit" class="submit-button" :disabled="loading">
              {{ loading ? 'Uploading...' : 'Upload Resource' }}
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
  name: 'Resources',
  data() {
    return {
      resources: [],
      loading: false,
      showUploadModal: false,
      searchQuery: '',
      filterCategory: '',
      newResource: {
        title: '',
        category: '',
        description: '',
        file: null
      }
    }
  },
  computed: {
    filteredResources() {
      return this.resources.filter(resource => {
        const matchesSearch = resource.title.toLowerCase().includes(this.searchQuery.toLowerCase()) ||
                            resource.description.toLowerCase().includes(this.searchQuery.toLowerCase())
        const matchesCategory = !this.filterCategory || resource.category === this.filterCategory
        return matchesSearch && matchesCategory
      })
    }
  },
  methods: {
    async fetchResources() {
      this.loading = true
      try {
        const response = await axios.get('http://localhost:8080/api/resources', {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        })
        this.resources = response.data
      } catch (error) {
        console.error('Error fetching resources:', error)
        alert('Failed to fetch resources. Please try again.')
      } finally {
        this.loading = false
      }
    },
    searchResources() {
      // The filtering is handled by the computed property
    },
    handleFileUpload(event) {
      this.newResource.file = event.target.files[0]
    },
    async uploadResource() {
      if (!this.newResource.file) {
        alert('Please select a file to upload')
        return
      }

      this.loading = true
      const formData = new FormData()
      formData.append('title', this.newResource.title)
      formData.append('category', this.newResource.category)
      formData.append('description', this.newResource.description)
      formData.append('file', this.newResource.file)

      try {
        await axios.post('http://localhost:8080/api/resources', formData, {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`,
            'Content-Type': 'multipart/form-data'
          }
        })
        this.showUploadModal = false
        this.newResource = {
          title: '',
          category: '',
          description: '',
          file: null
        }
        await this.fetchResources()
        alert('Resource uploaded successfully!')
      } catch (error) {
        console.error('Error uploading resource:', error)
        alert('Failed to upload resource. Please try again.')
      } finally {
        this.loading = false
      }
    },
    async downloadResource(resource) {
      try {
        const response = await axios.get(`http://localhost:8080/api/resources/${resource.id}/download`, {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          },
          responseType: 'blob'
        })
        
        const url = window.URL.createObjectURL(new Blob([response.data]))
        const link = document.createElement('a')
        link.href = url
        link.setAttribute('download', resource.title)
        document.body.appendChild(link)
        link.click()
        link.remove()
      } catch (error) {
        console.error('Error downloading resource:', error)
        alert('Failed to download resource. Please try again.')
      }
    },
    shareResource(resource) {
      // Implement sharing functionality
      console.log('Share resource:', resource)
    },
    formatDate(date) {
      return new Date(date).toLocaleDateString()
    }
  },
  mounted() {
    this.fetchResources()
  }
}
</script>

<style scoped>
.resources-container {
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

.upload-button {
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

.upload-button:hover {
  background-color: #45a049;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.resources-section {
  background: white;
  padding: 2rem;
  border-radius: 12px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.search-bar {
  display: flex;
  gap: 1rem;
  margin-bottom: 2rem;
}

.search-bar input,
.search-bar select {
  padding: 1rem;
  border: 2px solid #ddd;
  border-radius: 8px;
  font-size: 1.1rem;
  transition: all 0.3s ease;
}

.search-bar input {
  flex: 1;
}

.search-bar input:focus,
.search-bar select:focus {
  outline: none;
  border-color: #4CAF50;
  box-shadow: 0 0 0 3px rgba(76, 175, 80, 0.1);
}

.resources-grid {
  margin-top: 2rem;
}

.loading, .no-results {
  text-align: center;
  padding: 2rem;
  font-size: 1.2rem;
  color: #666;
}

.resource-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 2rem;
}

.resource-card {
  background: white;
  padding: 2rem;
  border-radius: 12px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.resource-info h3 {
  font-size: 1.5rem;
  color: #2c3e50;
  margin-bottom: 0.5rem;
}

.resource-info p {
  color: #666;
  margin-bottom: 0.5rem;
}

.resource-info .category {
  font-weight: 500;
  color: #4CAF50;
}

.resource-info .description {
  color: #666;
  line-height: 1.5;
}

.resource-info .uploader {
  color: #666;
  font-size: 0.9rem;
}

.resource-info .date {
  color: #666;
  font-size: 0.9rem;
}

.resource-actions {
  display: flex;
  gap: 1rem;
}

.download-button,
.share-button {
  flex: 1;
  padding: 0.875rem;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.download-button {
  background-color: #4CAF50;
  color: white;
}

.download-button:hover {
  background-color: #45a049;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.share-button {
  background-color: #f8f9fa;
  color: #2c3e50;
  border: 2px solid #ddd;
}

.share-button:hover {
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
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 1rem;
  border: 2px solid #ddd;
  border-radius: 8px;
  font-size: 1.1rem;
  transition: all 0.3s ease;
}

.form-group input[type="file"] {
  padding: 0.5rem;
}

.form-group input:focus,
.form-group select:focus,
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
  .resources-container {
    padding: 1.5rem;
  }
}

@media (max-width: 768px) {
  .resources-container {
    padding: 1rem;
  }

  .header h1 {
    font-size: 2rem;
  }

  .resources-section {
    padding: 1.5rem;
  }

  .resource-card {
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

  .resources-section {
    padding: 1.25rem;
  }

  .resource-card {
    padding: 1.25rem;
  }

  .modal-content {
    padding: 1.25rem;
  }

  .modal-actions {
    flex-direction: column;
  }

  .search-bar {
    flex-direction: column;
  }
}
</style> 