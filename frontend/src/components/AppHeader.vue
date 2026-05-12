<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowDown, House, Message, SwitchButton, User } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'
import { getUnreadNoticeCount } from '../api/notice'

const props = defineProps({
  navItems: {
    type: Array,
    required: true,
  },
})

const emit = defineEmits(['navigate'])
const menuOpen = ref(false)
const noticeUnreadCount = ref(0)
const router = useRouter()
const userStore = useUserStore()
const actionButtonText = computed(() => (userStore.isLoggedIn ? '个人空间' : '立即加入'))
const avatarText = computed(() => (userStore.displayName || '用户').slice(0, 1).toUpperCase())

function handleNavigate(id) {
  const target = props.navItems.find((item) => item.id === id)
  if (target?.to) {
    router.push(target.to)
    menuOpen.value = false
    return
  }
  emit('navigate', id)
  menuOpen.value = false
}

function goHome() {
  router.push('/')
}

function handleActionClick() {
  menuOpen.value = false
  if (userStore.isLoggedIn) {
    router.push('/console')
    return
  }
  const redirect = router.currentRoute.value.fullPath || '/'
  router.push({
    path: '/login',
    query: { redirect },
  })
}

async function loadUnreadNoticeCount() {
  if (!userStore.isLoggedIn) {
    noticeUnreadCount.value = 0
    return
  }
  try {
    noticeUnreadCount.value = Number(await getUnreadNoticeCount() || 0)
  } catch {
    noticeUnreadCount.value = 0
  }
}

function handleNoticeUpdated() {
  loadUnreadNoticeCount()
}

async function handleUserCommand(command) {
  menuOpen.value = false
  if (command === 'profile') {
    router.push('/console/profile')
    return
  }
  if (command === 'notices') {
    router.push('/console/notices')
    return
  }
  if (command !== 'logout') {
    return
  }

  try {
    await userStore.logout()
    ElMessage.success('已退出登录')
  } catch (error) {
    ElMessage.warning(error?.message || '退出登录失败，已清除本地登录状态')
  } finally {
    noticeUnreadCount.value = 0
    router.replace('/login')
  }
}

onMounted(() => {
  loadUnreadNoticeCount()
  if (typeof window !== 'undefined') {
    window.addEventListener('notice-updated', handleNoticeUpdated)
  }
})

onBeforeUnmount(() => {
  if (typeof window !== 'undefined') {
    window.removeEventListener('notice-updated', handleNoticeUpdated)
  }
})

watch(
  () => userStore.isLoggedIn,
  (loggedIn) => {
    if (loggedIn) {
      loadUnreadNoticeCount()
      return
    }
    noticeUnreadCount.value = 0
  },
)
</script>

<template>
  <header class="site-header">
    <div class="nav-card">
      <button class="brand brand-button" type="button" @click="goHome">
        <span class="brand-mark" aria-hidden="true">
          <el-icon><House /></el-icon>
        </span>
        <div class="brand-text">
          <strong>暖窝救助</strong>
          <small><span class="top-strip-right">24h 救助热线：400-820-1314</span></small>
        </div>
      </button>

      <nav class="desktop-nav" aria-label="主导航">
        <button
          v-for="item in props.navItems"
          :key="item.id"
          class="nav-link"
          type="button"
          @click="handleNavigate(item.id)"
        >
          {{ item.label }}
        </button>
      </nav>

      <div class="nav-action">
        <el-dropdown
          v-if="userStore.isLoggedIn"
          class="user-menu-dropdown"
          trigger="hover"
          @command="handleUserCommand"
        >
          <button class="user-pill" type="button">
            <span class="user-pill-avatar">
              <img v-if="userStore.profile.avatar" :src="userStore.profile.avatar" alt="用户头像" />
              <span v-else>{{ avatarText }}</span>
            </span>
            <span class="user-pill-name">{{ userStore.displayName }}</span>
            <el-badge v-if="noticeUnreadCount > 0" :value="noticeUnreadCount" :max="99" class="user-pill-badge" />
          </button>
          <template #dropdown>
            <el-dropdown-menu class="header-user-menu">
              <el-dropdown-item command="profile">
                <el-icon><User /></el-icon>
                <span>个人中心</span>
              </el-dropdown-item>
              <el-dropdown-item command="notices">
                <el-icon><Message /></el-icon>
                <span>站内信</span>
                <el-badge v-if="noticeUnreadCount > 0" :value="noticeUnreadCount" :max="99" class="header-notice-menu-badge" />
              </el-dropdown-item>
              <el-dropdown-item divided command="logout">
                <el-icon><SwitchButton /></el-icon>
                <span>退出登录</span>
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        <el-button v-else class="nav-btn" type="warning" @click="handleActionClick">
          {{ actionButtonText }}
        </el-button>
        <button
          class="mobile-toggle"
          type="button"
          aria-label="切换导航菜单"
          aria-controls="mobile-menu"
          :aria-expanded="menuOpen ? 'true' : 'false'"
          @click="menuOpen = !menuOpen"
        >
          <el-icon><ArrowDown /></el-icon>
        </button>
      </div>
    </div>

    <transition name="menu-fade">
      <nav v-if="menuOpen" id="mobile-menu" class="mobile-menu" aria-label="移动端导航">
        <button
          v-for="item in props.navItems"
          :key="item.id"
          class="mobile-link"
          type="button"
          @click="handleNavigate(item.id)"
        >
          {{ item.label }}
        </button>
      </nav>
    </transition>
  </header>
</template>
