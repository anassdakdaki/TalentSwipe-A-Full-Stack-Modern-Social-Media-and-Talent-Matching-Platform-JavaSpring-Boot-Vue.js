<template>
  <div class="login-container futuristic-theme">
    <div class="login-box glassmorphic-card">
      <h2>Login</h2>
      <form @submit.prevent="handleLogin">
        <div class="form-group">
          <label for="email">Email</label>
          <input
            type="email"
            id="email"
            v-model="email"
            required
            placeholder="Enter your email"
          />
        </div>
        <div class="form-group">
          <label for="password">Password</label>
          <input
            type="password"
            id="password"
            v-model="password"
            required
            placeholder="Enter your password"
          />
        </div>
        <button type="submit" :disabled="loading" class="futuristic-button">
          {{ loading ? 'Logging in...' : 'Login' }}
        </button>
        <p v-if="error" class="error">{{ error }}</p>
      </form>
      <p class="register-link">
        Don't have an account? <router-link to="/register">Register here</router-link>
      </p>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'Login',
  data() {
    return {
      email: '',
      password: '',
      loading: false,
      error: null
    }
  },
  methods: {
    async handleLogin() {
      this.loading = true
      this.error = null
      try {
        const response = await axios.post('http://localhost:8080/api/auth/login', {
          email: this.email,
          password: this.password
        }, {
          headers: {
            'Content-Type': 'application/json'
          }
        })
        
        if (response.data && response.data.token) {
          // Store the token
          localStorage.setItem('token', response.data.token)
          
          // Set default authorization header for future requests
          axios.defaults.headers.common['Authorization'] = `Bearer ${response.data.token}`
          
          // Redirect to home page
          this.$router.push('/')
        } else {
          throw new Error('Invalid response from server')
        }
      } catch (err) {
        console.error('Login error:', err)
        this.error = err.response?.data?.message || 'Login failed. Please check your credentials and try again.'
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;600;700&display=swap');

.login-container.futuristic-theme {
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

.login-box.glassmorphic-card {
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

input {
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

input::placeholder {
  color: rgba(255, 255, 255, 0.6);
}

input:focus {
  outline: none;
  border-color: #64ffda;
  box-shadow: 0 0 0 3px rgba(100, 255, 218, 0.2);
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

.register-link {
  text-align: center;
  margin-top: 20px;
  font-size: 1rem;
  color: #e0e0e0;
}

.register-link a {
  color: #64ffda; /* Accent color for links */
  text-decoration: none;
  font-weight: 600;
  margin-left: 5px;
  transition: color 0.3s ease;
}

.register-link a:hover {
  text-decoration: underline;
  color: #3be8b0;
}

/* Responsive Adjustments */
@media (max-width: 768px) {
  .login-box.glassmorphic-card {
    padding: 30px;
  }

  h2 {
    font-size: 2rem;
  }

  label,
  input,
  .futuristic-button {
    font-size: 1rem;
  }
}

@media (max-width: 480px) {
  .login-box.glassmorphic-card {
    padding: 20px;
  }

  h2 {
    font-size: 1.8rem;
  }
}
</style>