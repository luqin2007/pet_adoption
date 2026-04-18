<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowDown, House } from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'

const props = defineProps({
  navItems: {
    type: Array,
    required: true,
  },
})

const emit = defineEmits(['navigate'])
const menuOpen = ref(false)
const router = useRouter()
const userStore = useUserStore()
const actionButtonText = computed(() => (userStore.isLoggedIn ? '个人空间' : '立即加入'))

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
        <el-button class="nav-btn" type="warning" @click="handleActionClick">
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
