import { ElNotification } from 'element-plus'
import { ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { getUnreadNoticeCount } from '../api/notice'

const API_BASE_URL = String(import.meta.env.VITE_API_BASE_URL || '').replace(/\/$/, '')
const API_PREFIX = String(import.meta.env.VITE_API_PREFIX || '/api/v1')
  .replace(/^\/?/, '/')
  .replace(/\/$/, '')

const unreadCount = ref(0)
let controller = null
let reconnectTimer = null
let started = false
let router = null

async function parseSseStream(response, onNotice) {
  const reader = response.body.getReader()
  const decoder = new TextDecoder()
  let buffer = ''

  try {
    while (true) {
      const { done, value } = await reader.read()
      if (done) break
      buffer += decoder.decode(value, { stream: true })

      const events = buffer.split('\n\n')
      buffer = events.pop()

      for (const raw of events) {
        const lines = raw.split('\n')
        let name = 'message'
        let data = ''
        for (const line of lines) {
          if (line.startsWith('event:')) name = line.slice(6).trim()
          else if (line.startsWith('data:')) data += line.slice(5).trim()
        }
        if (!data) continue

        try {
          const parsed = JSON.parse(data)
          if (name === 'notice') {
            onNotice(parsed)
          }
        } catch {
          /* skip */
        }
      }
    }
  } catch {
    /* reader closed */
  }
}

export function useNoticeSse() {
  const userStore = useUserStore()

  if (!router) {
    try {
      router = useRouter()
    } catch {
      router = { push: () => {} }
    }
  }

  async function connect() {
    if (!started) return
    // Read from localStorage directly to always get the freshest token
    const session = (() => {
      try {
        const raw = localStorage.getItem('pet_adoption_user_session')
        return raw ? JSON.parse(raw) : null
      } catch { return null }
    })()
    const token = session?.accessToken || userStore.accessToken
    if (!token) {
      scheduleReconnect(userStore, 5000)
      return
    }

    try {
      controller = new AbortController()
      const url = `${API_BASE_URL}${API_PREFIX}/notices/connect`
      const response = await fetch(url, {
        headers: {
          Accept: 'text/event-stream',
          Authorization: `Bearer ${token}`,
        },
        signal: controller.signal,
      })

      if (!response.ok) {
        scheduleReconnect(userStore, 10000)
        return
      }

      await parseSseStream(response, (notice) => {
        const prev = unreadCount.value || 0
        unreadCount.value = prev + 1
        ElNotification({
          title: notice.title || '新站内信',
          message: notice.content || '',
          type: 'info',
          duration: 5000,
          onClick: () => router.push('/console/notices'),
        })
      })
    } catch (err) {
      if (err.name !== 'AbortError') {
        scheduleReconnect(userStore, 5000)
      }
    }
  }

  function scheduleReconnect(userStore, delay) {
    clearTimeout(reconnectTimer)
    reconnectTimer = setTimeout(() => {
      if (userStore.isLoggedIn) connect()
    }, delay)
  }

  function disconnect() {
    if (controller) {
      controller.abort()
      controller = null
    }
    clearTimeout(reconnectTimer)
    reconnectTimer = null
  }

  async function check() {
    if (!userStore.isLoggedIn) {
      unreadCount.value = 0
      return
    }
    try {
      unreadCount.value = Number((await getUnreadNoticeCount()) || 0)
    } catch {
      /* ignore */
    }
  }

  function start() {
    if (started) return
    started = true
    check()
    connect()
  }

  watch(
    () => userStore.isLoggedIn,
    (loggedIn) => {
      if (loggedIn) start()
      else {
        disconnect()
        unreadCount.value = 0
        started = false
      }
    },
    { immediate: true },
  )

  return { unreadCount, check }
}