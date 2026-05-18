<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { Icon } from '@iconify/vue'
import { ArrowLeft, PictureFilled } from '@element-plus/icons-vue'
import { useRoute, useRouter } from 'vue-router'
import AppFooter from '../components/AppFooter.vue'
import AppHeader from '../components/AppHeader.vue'
import { getRescueTask, getRescueTaskMedia } from '../api/services'
import { MAIN_NAV_ITEMS as navItems } from '../constants/navigation'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const task = ref(null)
const mediaItems = ref([])

const taskId = computed(() => String(route.params.id || ''))
const pageTitle = computed(() => task.value?.summary || '救助任务详情')

function goBack() {
  router.push('/console')
}

function statusText(status) {
  const map = {
    CREATED: '已创建',
    APPROVED: '审核通过',
    PROCESSING: '处理中',
    COMPLETED: '任务完成',
    DISCARDED: '已废弃',
  }
  return map[status] || status || '状态待补充'
}

function typeText(type) {
  const map = {
    FIND: '发现流浪宠物',
    MEDICAL: '医疗救助',
    OTHER: '其他协助',
  }
  return map[type] || type || '类型待补充'
}

function formatDate(value) {
  if (!value) {
    return '时间待补充'
  }
  return String(value).slice(0, 10)
}

function formatLocation(location) {
  if (!location) {
    return '地点待补充'
  }
  return [location.province, location.city, location.district, location.detailAddress].filter(Boolean).join(' · ')
}

async function loadTask() {
  if (!taskId.value) {
    task.value = null
    mediaItems.value = []
    return
  }
  loading.value = true
  try {
    const [taskResult, mediaResult] = await Promise.all([getRescueTask(taskId.value), getRescueTaskMedia(taskId.value)])
    task.value = taskResult
    mediaItems.value = Array.isArray(mediaResult) ? mediaResult : []
  } catch {
    task.value = null
    mediaItems.value = []
  } finally {
    loading.value = false
  }
}

watch(
  () => taskId.value,
  () => {
    loadTask()
  },
)

onMounted(() => {
  loadTask()
})
</script>

<template>
  <div class="subpage-shell">
    <AppHeader :nav-items="navItems" />

    <main class="subpage-main rescue-detail-page" v-loading="loading">
      <section v-if="task" class="rescue-detail-hero">
        <div>
          <el-button class="soft-btn" :icon="ArrowLeft" @click="goBack">返回个人中心</el-button>
          <h1>{{ pageTitle }}</h1>
          <p>{{ task.description || '救助任务已创建，我们会继续跟进。' }}</p>
          <div class="rescue-detail-meta">
            <span><Icon icon="mdi:calendar-range" />{{ formatDate(task.createTime) }}</span>
            <span><Icon icon="mdi:map-marker-radius-outline" />{{ formatLocation(task.location) }}</span>
          </div>
        </div>
        <div class="rescue-detail-status">
          <span>{{ typeText(task.type) }}</span>
          <strong>{{ statusText(task.status) }}</strong>
        </div>
      </section>

      <section v-if="task" class="action-form-panel">
        <div style="padding: 0 20px 20px;">
          <div class="pet-profile-panel-head">
            <h2>现场图片</h2>
          </div>
          <div v-if="mediaItems.length" class="rescue-detail-media-grid">
            <article v-for="item in mediaItems" :key="item.id">
              <img :src="item.assetUrl" :alt="item.name" loading="lazy" />
              <strong>{{ item.name || '现场图片' }}</strong>
            </article>
          </div>
          <el-empty v-else description="暂未上传现场图片">
            <el-icon><PictureFilled /></el-icon>
          </el-empty>
        </div>
      </section>

      <section v-else-if="!loading" class="pet-profile-empty">
        <el-empty description="未找到对应的救助任务">
          <el-button class="warm-btn" @click="goBack">返回个人中心</el-button>
        </el-empty>
      </section>
    </main>

    <AppFooter />
  </div>
</template>
