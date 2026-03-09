import axios from 'axios'

const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080').replace(/\/+$/, '')

function authHeaders() {
  const token = localStorage.getItem('token')
  return token ? { Authorization: `Bearer ${token}` } : {}
}

export function normalizeMediaUrl(url) {
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

export function getFeedPosts({ limit = 10, cursorCreatedAt = null, cursorPostId = null } = {}) {
  const params = { limit }
  if (cursorCreatedAt && cursorPostId !== null && cursorPostId !== undefined) {
    params.cursorCreatedAt = cursorCreatedAt
    params.cursorPostId = cursorPostId
  }
  return axios.get(`${API_BASE_URL}/api/posts/feed`, {
    headers: authHeaders(),
    params
  })
}

export function toggleLike(postId) {
  return axios.post(
    `${API_BASE_URL}/api/likes/post/${postId}`,
    {},
    { headers: authHeaders() }
  )
}

export function getComments(postId) {
  return axios.get(`${API_BASE_URL}/api/comments/post/${postId}`, {
    headers: authHeaders()
  })
}

export function createComment({ postId, content }) {
  return axios.post(
    `${API_BASE_URL}/api/comments`,
    { postId, content },
    {
      headers: {
        ...authHeaders(),
        'Content-Type': 'application/json'
      }
    }
  )
}

