<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { Icon } from '@iconify/vue'
import { ElMessage } from 'element-plus'
import { Plus, RefreshRight } from '@element-plus/icons-vue'
import AppFooter from '../components/AppFooter.vue'
import AppHeader from '../components/AppHeader.vue'
import {
  beginDonation,
  beginRescueTask,
  getAdoptApplications,
  getBreadingApplications,
  getDonations,
  getFirstVisitRegistrations,
  getItems,
  getMedicalRecords,
  getRescueTasks,
} from '../api/services'
import { MAIN_NAV_ITEMS as navItems } from '../constants/navigation'

const activeModule = ref('rescue')
const loading = ref(false)
const keyword = ref('')
const rescueTasks = ref([])
const adoptApplications = ref([])
const breadingApplications = ref([])
const medicalRecords = ref([])
const firstVisits = ref([])
const items = ref([])
const donations = ref([])

const moduleTabs = [
  { label: '救助任务', value: 'rescue', icon: 'mdi:ambulance' },
  { label: '领养寄养', value: 'adopt', icon: 'mdi:home-heart' },
  { label: '医疗护理', value: 'medical', icon: 'mdi:medical-bag' },
  { label: '物资捐赠', value: 'items', icon: 'mdi:package-variant-heart' },
]

const stats = computed(() => [
  { label: '救助任务', value: rescueTasks.value.length, note: '待跟进与执行' },
  { label: '领养/寄养申请', value: adoptApplications.value.length + breadingApplications.value.length, note: '等待审核流转' },
  { label: '医疗记录', value: medicalRecords.value.length + firstVisits.value.length, note: '诊疗与首诊登记' },
  { label: '物资与捐赠', value: items.value.length + donations.value.length, note: '库存和捐赠信息' },
])

const activeRecords = computed(() => {
  if (activeModule.value === 'rescue') {
    return rescueTasks.value
  }
  if (activeModule.value === 'adopt') {
    return [
      ...adoptApplications.value.map((item) => ({ ...item, moduleType: '领养申请' })),
      ...breadingApplications.value.map((item) => ({ ...item, moduleType: '寄养申请' })),
    ]
  }
  if (activeModule.value === 'medical') {
    return [
      ...medicalRecords.value.map((item) => ({ ...item, moduleType: '医疗记录' })),
      ...firstVisits.value.map((item) => ({ ...item, moduleType: '首诊登记' })),
    ]
  }
  return [
    ...items.value.map((item) => ({ ...item, moduleType: '物资信息' })),
    ...donations.value.map((item) => ({ ...item, moduleType: '捐赠记录' })),
  ]
})

const visibleRecords = computed(() => {
  const text = keyword.value.trim()
  if (!text) {
    return activeRecords.value
  }
  return activeRecords.value.filter((item) =>
    [
      item.title,
      item.name,
      item.petName,
      item.applicantName,
      item.donorName,
      item.categoryName,
      item.description,
      item.reason,
      item.remark,
      item.status,
      item.locations?.[0]?.city,
      item.locations?.[0]?.detailAddress,
    ]
      .filter(Boolean)
      .join('')
      .includes(text),
  )
})

async function loadData() {
  loading.value = true
  try {
    const [taskResult, adoptResult, breadingResult, medicalResult, firstResult, itemResult, donationResult] =
      await Promise.allSettled([
        getRescueTasks({ size: 8 }),
        getAdoptApplications({ size: 8 }),
        getBreadingApplications({ size: 8 }),
        getMedicalRecords({ size: 8 }),
        getFirstVisitRegistrations({ size: 8 }),
        getItems({ size: 8 }),
        getDonations({ size: 8 }),
      ])

    rescueTasks.value = getRecords(taskResult)
    adoptApplications.value = getRecords(adoptResult)
    breadingApplications.value = getRecords(breadingResult)
    medicalRecords.value = getRecords(medicalResult)
    firstVisits.value = getRecords(firstResult)
    items.value = getRecords(itemResult)
    donations.value = getRecords(donationResult)
  } finally {
    loading.value = false
  }
}

function getRecords(result) {
  if (result.status !== 'fulfilled') {
    return []
  }
  const data = result.value
  if (Array.isArray(data)) {
    return data
  }
  return Array.isArray(data?.records) ? data.records : []
}

function formatDate(value) {
  if (!value) {
    return '时间待补充'
  }
  return String(value).slice(0, 10)
}

function formatLocation(item) {
  const location = item.locations?.[0] || item.location
  if (!location) {
    return '地点待补充'
  }
  return [location.city, location.district, location.detailAddress].filter(Boolean).join(' · ')
}

function titleOf(item) {
  return item.title || item.name || item.petName || item.summary || item.moduleType || '业务记录'
}

function descriptionOf(item) {
  return item.description || item.reason || item.remark || item.diagnosis || item.symptom || '后端已提供该业务数据，详情字段将随模块流程继续完善。'
}

function statusText(status) {
  const map = {
    WAITING: '待处理',
    ASSIGNED: '已分配',
    PROCESSING: '处理中',
    PENDING: '待审核',
    ACTIVE: '启用中',
    PASSED: '已通过',
    REJECTED: '已驳回',
  }
  return map[status] || status || '状态待补充'
}

async function createDraft() {
  try {
    if (activeModule.value === 'rescue') {
      const uuid = await beginRescueTask()
      ElMessage.success(`已创建救助上报草稿：${uuid}`)
      return
    }
    if (activeModule.value === 'items') {
      const uuid = await beginDonation()
      ElMessage.success(`已创建捐赠草稿：${uuid}`)
      return
    }
    ElMessage.info('该模块的新增流程需要填写完整表单，当前页面先提供数据看板与接口入口。')
  } catch (error) {
    ElMessage.warning(error?.message || '创建草稿失败，请稍后重试')
  }
}

watch(activeModule, () => {
  keyword.value = ''
})

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="subpage-shell">
    <AppHeader :nav-items="navItems" />

    <main class="subpage-main">
      <section class="service-stats">
        <article v-for="item in stats" :key="item.label" class="service-stat-card">
          <span>{{ item.label }}</span>
          <strong>{{ item.value }}</strong>
          <small>{{ item.note }}</small>
        </article>
      </section>

      <section class="service-toolbar">
        <el-segmented v-model="activeModule" :options="moduleTabs" block>
          <template #default="{ item }">
            <span class="service-tab-label">
              <Icon :icon="item.icon" />
              {{ item.label }}
            </span>
          </template>
        </el-segmented>
        <el-input v-model="keyword" placeholder="按标题、宠物、地点、状态搜索" clearable />
      </section>

      <section class="service-record-grid" v-loading="loading">
        <article v-for="item in visibleRecords" :key="`${activeModule}-${item.moduleType || 'record'}-${item.id}`" class="service-record-card">
          <div class="hub-card-head">
            <el-tag type="warning" effect="plain">{{ item.moduleType || moduleTabs.find((tab) => tab.value === activeModule)?.label }}</el-tag>
            <span>{{ statusText(item.status) }}</span>
          </div>
          <h3>{{ titleOf(item) }}</h3>
          <p>{{ descriptionOf(item) }}</p>
          <div class="volunteer-meta">
            <span><Icon icon="mdi:calendar-range" />{{ formatDate(item.createTime || item.visitTime || item.applyTime) }}</span>
            <span><Icon icon="mdi:map-marker-radius-outline" />{{ formatLocation(item) }}</span>
            <span><Icon icon="mdi:account-heart-outline" />{{ item.reporterName || item.applicantName || item.donorName || item.registrarName || '暖窝业务组' }}</span>
          </div>
        </article>

        <el-empty v-if="!loading && visibleRecords.length === 0" description="当前筛选下暂无记录" />
      </section>
    </main>

    <AppFooter />
  </div>
</template>
