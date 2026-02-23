<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowDown, House } from '@element-plus/icons-vue'

const props = defineProps({
  navItems: {
    type: Array,
    required: true,
  },
})

const emit = defineEmits(['navigate'])
const menuOpen = ref(false)
const router = useRouter()

function handleNavigate(id) {
  emit('navigate', id)
  menuOpen.value = false
}

function goAuth() {
  menuOpen.value = false
  router.push('/auth')
}
</script>

<template>
  <header class="site-header">
    <div class="nav-card">
      <div class="brand">
        <span class="brand-mark" aria-hidden="true">
          <el-icon><House /></el-icon>
        </span>
        <div class="brand-text">
          <strong>暖窝救助</strong>
          <small><span class="top-strip-right">24h 救助热线：400-820-1314</span></small>
        </div>
      </div>

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
        <el-button class="nav-btn" type="warning" @click="goAuth">
          立即加入
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
