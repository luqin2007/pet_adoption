import { ref, onBeforeUnmount, watch } from 'vue'
import { ElNotification } from 'element-plus'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { getUnreadNoticeCount } from '../api/notice'

const unreadCount = ref(0)
let timer = null
let started = false

export function useNoticePoller(interval = 30000) {
  const userStore = useUserStore()
  const router = useRouter()

  async function check() {
    if (!userStore.isLoggedIn) {
      unreadCount.value = 0
      return
    }
    try {
      const count = Number((await getUnreadNoticeCount()) || 0)
      const prev = unreadCount.value
      unreadCount.value = count
      if (prev > 0 && count > prev) {
        const delta = count - prev
        ElNotification({
          title: '新站内信',
          message: `您有 ${delta} 条未读站内信`,
          type: 'info',
          duration: 5000,
          onClick: () => router.push('/console/notices'),
        })
      }
    } catch {
      /* ignore */
    }
  }

  function start() {
    if (started) return
    started = true
    check()
    timer = setInterval(check, interval)
  }

  function stop() {
    if (timer) {
      clearInterval(timer)
      timer = null
    }
    started = false
  }

  watch(
    () => userStore.isLoggedIn,
    (loggedIn) => {
      if (loggedIn) start()
      else stop()
    },
    { immediate: true },
  )

  onBeforeUnmount(() => {
    /* keep polling across component mounts */
  })

  return { unreadCount, check }
}