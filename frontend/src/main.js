import './assets/main.css'

import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import axios from 'axios'
import { loadAppSettings, saveAppSettings } from './utils/appSettings'

// Configure axios base URL from env (defaults to local backend)
axios.defaults.baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

// Apply persisted runtime settings (theme, compact mode, reduced motion) before mount.
saveAppSettings(loadAppSettings())

const app = createApp(App)
app.use(router)
app.mount('#app')
