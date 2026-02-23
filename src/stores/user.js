import { defineStore } from 'pinia'
import { loginUser, registerUser, sendRegisterCode } from '../api/user'

const STORAGE_KEY = 'pet_adoption_user_session'

function getDefaultSession() {
  return {
    token: '',
    profile: {
      username: '',
      email: '',
      avatar: '',
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
      token: parsed?.token || '',
      profile: {
        username: parsed?.profile?.username || '',
        email: parsed?.profile?.email || '',
        avatar: parsed?.profile?.avatar || '',
      },
    }
  } catch {
    return getDefaultSession()
  }
}

export const useUserStore = defineStore('user', {
  state: () => loadSession(),
  getters: {
    isLoggedIn: (state) => Boolean(state.token),
    displayName: (state) => state.profile.username || '用户',
  },
  actions: {
    persistSession() {
      if (typeof window === 'undefined') {
        return
      }
      const hasSession = Boolean(this.token || this.profile.username || this.profile.email || this.profile.avatar)
      if (!hasSession) {
        localStorage.removeItem(STORAGE_KEY)
        return
      }
      localStorage.setItem(
        STORAGE_KEY,
        JSON.stringify({
          token: this.token,
          profile: this.profile,
        }),
      )
    },
    setSession(userData) {
      this.token = userData?.token || ''
      this.profile = {
        username: userData?.username || '',
        email: userData?.email || '',
        avatar: userData?.avatar || '',
      }
      this.persistSession()
    },
    clearSession() {
      this.token = ''
      this.profile = {
        username: '',
        email: '',
        avatar: '',
      }
      this.persistSession()
    },
    async login(payload) {
      const data = await loginUser(payload)
      if (!data?.token) {
        throw new Error('登录响应缺少 token')
      }
      this.setSession(data)
      return data
    },
    async register(payload) {
      const data = await registerUser(payload)
      if (data?.token) {
        this.setSession(data)
      }
      return data
    },
    async sendRegisterCode(email) {
      return sendRegisterCode(email)
    },
  },
})
