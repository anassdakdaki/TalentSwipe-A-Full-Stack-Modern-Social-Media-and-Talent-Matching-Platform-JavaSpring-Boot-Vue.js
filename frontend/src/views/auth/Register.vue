<template>
  <div class="register-container futuristic-theme">
    <div class="register-box glassmorphic-card">
      <h2>Create Account</h2>
      <form @submit.prevent="handleRegister" name="registerForm" autocomplete="on">
        <div class="form-group">
          <label for="name">Name</label>
          <input
            type="text"
            id="name"
            name="name"
            v-model="name"
            required
            autocomplete="name"
            placeholder="Enter your name"
          />
        </div>
        <div class="form-group">
          <label for="email">Email</label>
          <input
            type="email"
            id="email"
            name="email"
            v-model="email"
            required
            autocomplete="email"
            placeholder="Enter your email"
          />
        </div>
        <div class="form-group">
          <label for="password">Password</label>
          <input
            type="password"
            id="password"
            name="password"
            v-model="password"
            required
            autocomplete="new-password"
            placeholder="Enter your password"
            minlength="6"
          />
        </div>
        <div class="form-group">
          <label for="confirmPassword">Confirm Password</label>
          <input
            type="password"
            id="confirmPassword"
            name="confirmPassword"
            v-model="confirmPassword"
            required
            autocomplete="new-password"
            placeholder="Confirm your password"
            minlength="6"
          />
        </div>
        <button type="submit" :disabled="loading || !isFormValid" class="futuristic-button">
          {{ loading ? 'Creating Account...' : 'Register' }}
        </button>
        <p v-if="error" class="error">{{ error }}</p>
      </form>
      <p class="login-link">
        Already have an account? <router-link to="/auth/login">Login here</router-link>
      </p>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'Register',
  data() {
    return {
      name: '',
      email: '',
      password: '',
      confirmPassword: '',
      loading: false,
      error: null
    }
  },
  computed: {
    isFormValid() {
      return this.name && 
             this.email && 
             this.password && 
             this.confirmPassword && 
             this.password === this.confirmPassword
    }
  },
  methods: {
    async handleRegister() {
      if (this.password !== this.confirmPassword) {
        this.error = 'Passwords do not match'
        return
      }

      if (this.password.length < 6) {
        this.error = 'Password must be at least 6 characters long'
        return
      }

      this.loading = true
      this.error = null
      try {
        const requestData = {
          name: this.name,
          email: this.email,
          password: this.password
        }
        
        console.log('Sending registration request with data:', {
          ...requestData,
          password: '***' // Don't log actual password
        })

        const response = await axios.post('http://localhost:8080/api/auth/register', requestData, {
          headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
          }
        })
        
        console.log('Registration response:', {
          status: response.status,
          statusText: response.statusText,
          headers: response.headers,
          data: response.data
        })
        
        if (response.data && response.data.message) {
          console.log('Registration successful, redirecting to login...')
          this.$router.push('/auth/login')
        } else {
          throw new Error('Invalid response from server')
        }
      } catch (err) {
        console.error('Registration error details:', {
          message: err.message,
          response: err.response ? {
            status: err.response.status,
            statusText: err.response.statusText,
            data: err.response.data,
            headers: err.response.headers
          } : 'No response',
          request: err.request ? {
            method: err.request.method,
            url: err.request.url,
            headers: err.request.headers
          } : 'No request was made'
        })
        
        if (err.response) {
          this.error = err.response.data?.message || 'Registration failed. Please try again.'
          console.error('Error response:', err.response.data)
        } else if (err.request) {
          this.error = 'No response from server. Please check your connection.'
          console.error('Error request:', err.request)
        } else {
          this.error = err.message || 'Registration failed. Please try again.'
          console.error('Error message:', err.message)
        }
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;600;700&display=swap');

.register-container.futuristic-theme {
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%); /* Dark gradient background */
  color: #e0e0e0; /* Light text */
  min-height: 100vh; /* Ensure it takes at least the full viewport height */
  width: 100%; /* Take full width */
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px; /* Add some padding */
  box-sizing: border-box; /* Include padding in width */
  overflow-x: hidden; /* Hide horizontal overflow */
}

.register-box.glassmorphic-card {
  background: rgba(255, 255, 255, 0.1); /* Glassmorphism background */
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 20px;
  padding: 40px; /* Increased padding for a larger card */
  box-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.37);
  width: 100%;
  max-width: 500px; /* Adjusted max-width for consistency */
}

h2 {
  text-align: center;
  color: #64ffda; /* Accent color for headings */
  margin-bottom: 25px;
  font-size: 2.2rem;
  font-weight: 600;
}

.form-group {
  margin-bottom: 20px;
}

label {
  display: block;
  margin-bottom: 8px;
  color: #e0e0e0;
  font-size: 1.1rem;
  font-weight: 500;
}

input, textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.1); /* Glassmorphism input background */
  color: #ffffff;
  font-size: 1rem;
  transition: all 0.3s ease;
  box-sizing: border-box;
}

input::placeholder,
textarea::placeholder {
  color: rgba(255, 255, 255, 0.6);
}

input:focus,
textarea:focus {
  outline: none;
  border-color: #64ffda;
  box-shadow: 0 0 0 3px rgba(100, 255, 218, 0.2);
}

textarea {
  resize: vertical;
  min-height: 100px; /* Adjusted min-height for consistency */
}

.futuristic-button {
  width: 100%;
  padding: 12px;
  background: linear-gradient(45deg, #64ffda, #3be8b0); /* Futuristic gradient button */
  color: #1a1a2e; /* Dark text on button */
  border: none;
  border-radius: 8px;
  font-size: 1.1rem;
  font-weight: 600;
  cursor: pointer;
  margin-top: 25px;
  transition: all 0.3s ease;
}

.futuristic-button:hover:not(:disabled) {
  box-shadow: 0 6px 15px rgba(100, 255, 218, 0.4);
  transform: translateY(-2px);
}

.futuristic-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.error {
  color: #ff5252; /* Error color */
  margin-top: 15px;
  text-align: center;
  font-size: 0.95rem;
  padding: 10px;
  background-color: rgba(255, 82, 82, 0.1);
  border-radius: 8px;
}

.login-link {
  text-align: center;
  margin-top: 20px;
  font-size: 1rem;
  color: #e0e0e0;
}

.login-link a {
  color: #64ffda; /* Accent color for links */
  text-decoration: none;
  font-weight: 600;
  margin-left: 5px;
  transition: color 0.3s ease;
}

.login-link a:hover {
  text-decoration: underline;
  color: #3be8b0;
}

/* Responsive Adjustments */
@media (max-width: 768px) {
  .register-box.glassmorphic-card {
    padding: 30px;
  }

  h2 {
    font-size: 2rem;
  }

  label,
  input,
  textarea,
  .futuristic-button {
    font-size: 1rem;
  }
}

@media (max-width: 480px) {
  .register-box.glassmorphic-card {
    padding: 20px;
  }

  h2 {
    font-size: 1.8rem;
  }
}
</style> 