import axios from 'axios'

const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080').replace(/\/+$/, '')
const ONBOARDING_STATUS_CACHE_KEY = 'biblo_onboarding_status_v1'
const ONBOARDING_STATUS_CACHE_TTL_MS = 2 * 60 * 1000

function getToken() {
  return localStorage.getItem('token') || ''
}

function tokenFingerprint(token) {
  if (!token) return ''
  return token.slice(-24)
}

function authHeaders() {
  const token = getToken()
  return token ? { Authorization: `Bearer ${token}` } : {}
}

export function getOnboardingStatus() {
  return axios.get(`${API_BASE_URL}/api/onboarding/status`, {
    headers: authHeaders()
  })
}

export function getOnboardingOptions() {
  return axios.get(`${API_BASE_URL}/api/onboarding/options`, {
    headers: authHeaders()
  })
}

export function completeOnboarding(payload) {
  return axios.put(`${API_BASE_URL}/api/onboarding/complete`, payload, {
    headers: {
      ...authHeaders(),
      'Content-Type': 'application/json'
    }
  })
}

export function readOnboardingStatusCache() {
  const token = getToken()
  if (!token) return null

  try {
    const raw = localStorage.getItem(ONBOARDING_STATUS_CACHE_KEY)
    if (!raw) return null
    const parsed = JSON.parse(raw)
    if (!parsed || parsed.tokenFingerprint !== tokenFingerprint(token)) return null
    if (typeof parsed.checkedAt !== 'number') return null
    if (Date.now() - parsed.checkedAt > ONBOARDING_STATUS_CACHE_TTL_MS) return null
    return parsed.completed === true
  } catch {
    return null
  }
}

export function writeOnboardingStatusCache(completed) {
  const token = getToken()
  if (!token) return

  const payload = {
    tokenFingerprint: tokenFingerprint(token),
    completed: completed === true,
    checkedAt: Date.now()
  }
  localStorage.setItem(ONBOARDING_STATUS_CACHE_KEY, JSON.stringify(payload))
}

export function clearOnboardingStatusCache() {
  localStorage.removeItem(ONBOARDING_STATUS_CACHE_KEY)
}