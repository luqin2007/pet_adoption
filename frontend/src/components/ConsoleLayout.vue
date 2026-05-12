<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, Connection, Message, SwitchButton, User } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '../stores/user'
import { useConsoleGuards } from '../composables/useConsoleGuards'
import { ROLE, hasRole } from '../utils/roles'
import { medicalRecordOwnerExists } from '../api/services'
import { getUnreadNoticeCount } from '../api/notice'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const { loginRole, isLoginAdmin, canManageUsers, canManageMedical, canManageRehab, canManageArticles } = useConsoleGuards()
const hasOwnedMedicalRecords = ref(false)
const noticeUnreadCount = ref(0)

const activeMenu = computed(() => {
  if (route.path.startsWith('/console/volunteer/applications/')) return '/console/volunteer/applications'
  if (route.path.startsWith('/console/volunteer/profiles')) return '/console/volunteer/profiles'
  if (route.path.startsWith('/console/volunteer/activities/')) return '/console/volunteer/activities'
  if (route.path.startsWith('/console/adoption/agreements')) return '/console/adoption/agreements'
  if (route.path.startsWith('/console/adoption/breading')) return '/console/adoption/breading'
  if (route.path.startsWith('/console/adoption/follow-records')) return '/console/adoption/follow-tasks'
  if (route.path.startsWith('/console/adoption/follow-tasks')) return '/console/adoption/follow-tasks'
  if (route.path.startsWith('/console/adoption/adopts')) return '/console/adoption/adopts'
  if (route.path.startsWith('/console/items/donations')) return '/console/items/donations'
  if (route.path.startsWith('/console/items/stocks')) return '/console/items/stocks'
  if (route.path.startsWith('/console/items/records')) return '/console/items/records'
  if (route.path.startsWith('/console/medical/first/')) return '/console/medical/first'
  if (route.path.startsWith('/console/medical/records/')) return '/console/medical/records'
  if (route.path.startsWith('/console/medical/detail-list')) return '/console/medical/detail-list'
  if (route.path.startsWith('/console/medical/rehab')) return '/console/medical/rehab'
  if (route.path.startsWith('/console/medical/health')) return '/console/medical/health'
  return route.path
})
const canViewMedical = computed(() => canManageMedical.value || canManageRehab.value || hasOwnedMedicalRecords.value)

function handleMenuSelect(index) {
  if (index === 'api-coverage') {
    router.push('/api-coverage')
  } else {
    router.push(index)
  }
}

function goHome() {
  router.push('/')
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

async function handleLogout() {
  try {
    await ElMessageBox.confirm('确认退出登录？', '退出确认', {
      type: 'warning',
      confirmButtonText: '退出',
      cancelButtonText: '取消',
    })
  } catch {
    return
  }

  try {
    await userStore.logout()
    ElMessage.success('已退出登录')
  } catch (error) {
    const message = error?.message ? String(error.message) : '退出登录失败，已清除本地登录状态'
    ElMessage.warning(message)
  } finally {
    router.replace('/login')
  }
}

const TAB_REDIRECTS = {
  'medical-first': '/console/medical/first',
  'articles': null,
  'article-mine': '/console/articles/mine',
  'article-manage': '/console/articles/manage',
  'article-favorites': '/console/articles/favorites',
  'lost-pets': '/console/lost-pets',
  'volunteer': null,
}

onMounted(() => {
  const tab = route.query.tab
  if (tab) {
    if (tab === 'articles') {
      router.replace(canManageArticles.value ? '/console/articles/mine' : '/console/articles/favorites')
    } else if (tab === 'volunteer') {
      router.replace(isLoginAdmin.value || hasRole(loginRole.value, ROLE.WORKER) ? '/console/volunteer/recruitments' : '/console/volunteer/applications')
    } else if (TAB_REDIRECTS[tab]) {
      router.replace(TAB_REDIRECTS[tab])
    }
  }
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
  () => userStore.profile?.id,
  async (id) => {
    if (canManageMedical.value || !id) {
      hasOwnedMedicalRecords.value = false
      return
    }
    try {
      hasOwnedMedicalRecords.value = Boolean(await medicalRecordOwnerExists(id))
    } catch {
      hasOwnedMedicalRecords.value = false
    }
  },
  { immediate: true },
)
</script>

<template>
  <div class="console-page">
    <header class="console-topbar">
      <div>
        <h1>后台管理</h1>
        <p>{{ userStore.displayName }} 的全幅工作台</p>
      </div>
      <div class="console-topbar-actions">
        <el-button text type="warning" @click="goHome">
          <el-icon><ArrowLeft /></el-icon>
          返回首页
        </el-button>
        <el-button text type="danger" @click="handleLogout">
          <el-icon><SwitchButton /></el-icon>
          退出登录
        </el-button>
      </div>
    </header>

    <div class="console-layout">
      <aside class="console-sidebar">
        <el-menu class="console-menu" :default-active="activeMenu" @select="handleMenuSelect">
          <el-menu-item index="/console/profile">
            <el-icon><User /></el-icon>
            <span>个人信息</span>
          </el-menu-item>
          <el-menu-item index="/console/notices">
            <el-icon><Message /></el-icon>
            <span>站内信</span>
            <el-badge v-if="noticeUnreadCount > 0" :value="noticeUnreadCount" :max="99" class="console-menu-badge" />
          </el-menu-item>
          <el-menu-item v-if="canManageUsers" index="/console/users">
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item index="/console/pets">
            <el-icon><Connection /></el-icon>
            <span>流浪宠物</span>
          </el-menu-item>
          <el-menu-item index="/console/lost-pets">
            <el-icon><Connection /></el-icon>
            <span>丢失宠物</span>
          </el-menu-item>
          <el-menu-item index="/console/tasks">
            <el-icon><Connection /></el-icon>
            <span>救助任务</span>
          </el-menu-item>
          <el-sub-menu index="/console/articles">
            <template #title>
              <el-icon><Message /></el-icon>
              <span>公益文章</span>
            </template>
            <el-menu-item v-if="canManageArticles" index="/console/articles/mine">我的文章</el-menu-item>
            <el-menu-item index="/console/articles/favorites">我的收藏</el-menu-item>
            <el-menu-item v-if="canManageUsers" index="/console/articles/manage">文章管理</el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="/console/adoption">
            <template #title>
              <el-icon><Connection /></el-icon>
              <span>领养寄养</span>
            </template>
            <el-menu-item index="/console/adoption/breading">寄养管理</el-menu-item>
            <el-menu-item index="/console/adoption/adopts">领养管理</el-menu-item>
            <el-menu-item index="/console/adoption/follow-tasks">回访任务</el-menu-item>
            <el-menu-item v-if="canManageUsers" index="/console/adoption/agreements">协议管理</el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="/console/volunteer">
            <template #title>
              <el-icon><Connection /></el-icon>
              <span>志愿者</span>
            </template>
            <el-menu-item v-if="canManageUsers" index="/console/volunteer/recruitments">招募计划</el-menu-item>
            <el-menu-item index="/console/volunteer/applications">招募申请</el-menu-item>
            <el-menu-item v-if="canManageUsers" index="/console/volunteer/profiles">志愿者档案</el-menu-item>
            <el-menu-item v-if="canManageUsers || hasRole(loginRole, ROLE.VOLUNTEER)" index="/console/volunteer/rewards">志愿者激励</el-menu-item>
            <el-menu-item v-if="canManageUsers || hasRole(loginRole, ROLE.VOLUNTEER)" index="/console/volunteer/activities">志愿活动</el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="/console/items">
            <template #title>
              <el-icon><Connection /></el-icon>
              <span>物资管理</span>
            </template>
            <el-menu-item index="/console/items/donations">捐赠</el-menu-item>
            <el-menu-item v-if="canManageUsers" index="/console/items/stocks">物资余量</el-menu-item>
            <el-menu-item v-if="canManageUsers" index="/console/items/records">库存管理</el-menu-item>
          </el-sub-menu>
          <el-sub-menu v-if="canViewMedical" index="/console/medical">
            <template #title>
              <el-icon><Connection /></el-icon>
              <span>医疗护理</span>
            </template>
            <el-menu-item v-if="canManageMedical" index="/console/medical/first">初诊登记</el-menu-item>
            <el-menu-item index="/console/medical/records">就诊记录</el-menu-item>
            <el-menu-item v-if="isLoginAdmin || hasRole(loginRole, ROLE.DOCTOR)" index="/console/medical/detail-list">病历</el-menu-item>
            <el-menu-item v-if="canManageMedical" index="/console/medical/vaccines">疫苗接种</el-menu-item>
            <el-menu-item v-if="canManageMedical" index="/console/medical/deworms">驱虫管理</el-menu-item>
            <el-menu-item v-if="canManageRehab" index="/console/medical/rehab">康复计划</el-menu-item>
            <el-menu-item v-if="canManageMedical" index="/console/medical/health">健康评估</el-menu-item>
          </el-sub-menu>
          <el-menu-item index="api-coverage">
            <el-icon><Connection /></el-icon>
            <span>接口覆盖台</span>
          </el-menu-item>
        </el-menu>
      </aside>

      <section class="console-content">
        <router-view />
      </section>
    </div>
  </div>
</template>
