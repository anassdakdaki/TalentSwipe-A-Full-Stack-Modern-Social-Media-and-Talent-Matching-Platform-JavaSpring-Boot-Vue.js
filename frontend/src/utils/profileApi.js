import axios from 'axios'

const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080').replace(/\/+$/, '')

function authHeaders() {
  const token = localStorage.getItem('token')
  return token ? { Authorization: `Bearer ${token}` } : {}
}

export function getEditorProfile() {
  return axios.get(`${API_BASE_URL}/api/profiles/me/editor`, {
    headers: authHeaders()
  })
}

export function updateEditorProfile(payload) {
  return axios.put(`${API_BASE_URL}/api/profiles/me/editor`, payload, {
    headers: {
      ...authHeaders(),
      'Content-Type': 'application/json'
    }
  })
}

export function getPublicProfile(userId) {
  return axios.get(`${API_BASE_URL}/api/profiles/${userId}/public`, {
    headers: authHeaders()
  })
}

export function getProfileSummary(userId) {
  return axios.get(`${API_BASE_URL}/api/profiles/${userId}/summary`, {
    headers: authHeaders()
  })
}

export function getMatchStatus(userId) {
  return axios.get(`${API_BASE_URL}/api/matches/with/${userId}`, {
    headers: authHeaders()
  })
}

export function swipeLike(userId) {
  return axios.post(
    `${API_BASE_URL}/api/matches/swipe`,
    {
      swipedUserId: Number(userId),
      swipeType: 'LIKE'
    },
    { headers: authHeaders() }
  )
}

export function getCurrentUser() {
  return axios.get(`${API_BASE_URL}/api/auth/me`, {
    headers: authHeaders()
  })
}

export function findOrCreateChatRoom(payload) {
  return axios.post(`${API_BASE_URL}/api/chat/findOrCreate`, payload, {
    headers: authHeaders()
  })
}

export function normalizeProfileImageUrl(url) {
  if (!url) {
    return ''
  }
  const normalized = String(url).trim().replace(/\\/g, '/')
  if (!normalized) {
    return ''
  }
  if (normalized.startsWith('http://') || normalized.startsWith('https://')) {
    return normalized
  }
  const path = normalized.startsWith('/') ? normalized : `/${normalized}`
  return `${API_BASE_URL}${path}`
}