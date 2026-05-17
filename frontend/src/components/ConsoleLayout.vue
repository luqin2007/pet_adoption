<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, Connection, Message, SwitchButton, User } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '../stores/user'
import { useConsoleGuards } from '../composables/useConsoleGuards'
import { useNoticeSse } from '../composables/useNoticeSse'
import { ROLE, hasRole } from '../utils/roles'
import { medicalRecordOwnerExists } from '../api/services'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const { loginRole, isLoginAdmin, canManageUsers, canManageMedical, canManageRehab, canManageArticles } = useConsoleGuards()
const hasOwnedMedicalRecords = ref(false)
const { unreadCount: noticeUnreadCount, check: checkNoticeCount } = useNoticeSse()

const activeMenu = computed(() => {
  if (route.path.startsWith('/console/volunteer')) return route.path.startsWith('/console/volunteer/recruitments') ? '/console/volunteer/recruitments' : '/console/volunteer'
  if (route.path.startsWith('/console/adoption')) return '/console/adoption'
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
  router.push(index)
}

function goHome() {
  router.push('/')
}

function handleNoticeUpdated() {
  checkNoticeCount()
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
  'article-mine': '/console/articles',
  'article-manage': '/console/articles',
  'article-favorites': '/console/articles',
  'lost-pets': '/console/lost-pets',
  'volunteer': null,
}

onMounted(() => {
  const tab = route.query.tab
  if (tab) {
    if (tab === 'articles') {
      router.replace('/console/articles')
    } else if (tab === 'volunteer') {
      router.replace('/console/volunteer')
    } else if (TAB_REDIRECTS[tab]) {
      router.replace(TAB_REDIRECTS[tab])
    }
  }
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
            <span v-if="noticeUnreadCount > 0" class="console-menu-badge">{{ noticeUnreadCount > 99 ? '99+' : noticeUnreadCount }}</span>
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
          <el-menu-item index="/console/articles">
            <el-icon><Message /></el-icon>
            <span>公益文章</span>
          </el-menu-item>
          <el-menu-item index="/console/adoption">
            <el-icon><Connection /></el-icon>
            <span>领养寄养</span>
          </el-menu-item>
          <el-sub-menu index="/console/volunteer">
            <template #title>
              <el-icon><Connection /></el-icon>
              <span>志愿者</span>
            </template>
            <el-menu-item index="/console/volunteer">志愿者</el-menu-item>
            <el-menu-item index="/console/volunteer/recruitments">志愿者招募</el-menu-item>
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
        </el-menu>
      </aside>

      <section class="console-content">
        <router-view />
      </section>
    </div>
  </div>
</template>
