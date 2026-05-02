import { defineStore } from 'pinia'
import { loginUser, logoutUser, registerUser, sendRegisterCode } from '../api/user'

const STORAGE_KEY = 'pet_adoption_user_session'
const API_BASE_URL = String(import.meta.env.VITE_API_BASE_URL || '').replace(/\/$/, '')
const API_PREFIX = String(import.meta.env.VITE_API_PREFIX || '/api/v1')
  .replace(/^\/?/, '/')
  .replace(/\/$/, '')

function normalizeAssetUrl(value) {
  const path = String(value || '').trim()
  if (!path) {
    return ''
  }
  if (/^(https?:)?\/\//.test(path) || path.startsWith('data:') || path.startsWith('blob:')) {
    return path
  }
  if (path.startsWith('/assets/')) {
    return `${API_BASE_URL}${API_PREFIX}${path}`
  }
  if (API_PREFIX && path.startsWith(`${API_PREFIX}/assets/`)) {
    return `${API_BASE_URL}${path}`
  }
  return path
}

function getDefaultSession() {
  return {
    accessToken: '',
    refreshToken: '',
      profile: {
        id: '',
        username: '',
        email: '',
        avatar: '',
        role: 0,
        phone: '',
      },
  }
}

function loadSession() {
  if (typeof window === 'undefined') {
    return getDefaultSession()
  }

  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (!raw) {
      return getDefaultSession()
    }
    const parsed = JSON.parse(raw)
    return {
      accessToken: parsed?.accessToken || parsed?.token || '',
      refreshToken: parsed?.refreshToken || '',
      profile: {
        id: String(parsed?.profile?.id || ''),
        username: parsed?.profile?.username || '',
        email: parsed?.profile?.email || '',
        avatar: normalizeAssetUrl(parsed?.profile?.avatar || ''),
        role: Number(parsed?.profile?.role || 0),
        phone: parsed?.profile?.phone || '',
      },
    }
  } catch {
    return getDefaultSession()
  }
}

function getPersistedRefreshToken() {
  if (typeof window === 'undefined') {
    return ''
  }

  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (!raw) {
      return ''
    }
    const parsed = JSON.parse(raw)
    return parsed?.refreshToken || ''
  } catch {
    return ''
  }
}

export const useUserStore = defineStore('user', {
  state: () => loadSession(),
  getters: {
    isLoggedIn: (state) => Boolean(state.accessToken),
    displayName: (state) => state.profile.username || '用户',
  },
  actions: {
    persistSession() {
      if (typeof window === 'undefined') {
        return
      }
      const hasSession = Boolean(
        this.accessToken ||
          this.refreshToken ||
          this.profile.id ||
          this.profile.username ||
          this.profile.email ||
          this.profile.avatar,
      )
      if (!hasSession) {
        localStorage.removeItem(STORAGE_KEY)
        return
      }
      localStorage.setItem(
        STORAGE_KEY,
        JSON.stringify({
          accessToken: this.accessToken,
          refreshToken: this.refreshToken,
          profile: this.profile,
        }),
      )
    },
    setSession(userData) {
      this.accessToken = userData?.accessToken || userData?.token || ''
      this.refreshToken = userData?.refreshToken || ''
      this.profile = {
        id: String(userData?.id || ''),
        username: userData?.username || '',
        email: userData?.email || '',
        avatar: normalizeAssetUrl(userData?.avatar || ''),
        role: Number(userData?.role || 0),
        phone: userData?.phone || '',
      }
      this.persistSession()
    },
    setProfile(profileData) {
      this.profile = {
        id: String(profileData?.id || this.profile.id || ''),
        username: profileData?.username || '',
        email: profileData?.email || '',
        avatar: normalizeAssetUrl(profileData?.avatar || ''),
        role: Number(profileData?.role ?? this.profile.role ?? 0),
        phone: profileData?.phone || '',
      }
      this.persistSession()
    },
    clearSession() {
      this.accessToken = ''
      this.refreshToken = ''
      this.profile = {
        id: '',
        username: '',
        email: '',
        avatar: '',
        role: 0,
        phone: '',
      }
      this.persistSession()
    },
    async login(payload) {
      const data = await loginUser(payload)
      if (!data?.accessToken) {
        throw new Error('登录响应缺少 accessToken')
      }
      this.setSession(data)
      return data
    },
    async register(payload) {
      const data = await registerUser(payload)
      if (data?.accessToken) {
        this.setSession(data)
      }
      return data
    },
    async logout() {
      try {
        const token = getPersistedRefreshToken() || this.refreshToken
        if (token) {
          await logoutUser(token)
        }
      } finally {
        this.clearSession()
      }
    },
    async sendRegisterCode(email) {
      return sendRegisterCode(email)
    },
  },
})
