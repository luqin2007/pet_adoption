<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { RefreshRight } from '@element-plus/icons-vue'
import { getNotices, markNoticeRead, markNoticeUnread } from '../api/notice'
import { useTableFilters } from '../composables/useTableFilters'
import TableActionColumnHeader from './TableActionColumnHeader.vue'
import TableFilterHeader from './TableFilterHeader.vue'

const NOTICE_SOURCE_LABELS = {
  SYSTEM: '系统通知',
  PET_ADOPT: '领养',
  AGREEMENT: '协议',
  BREADING: '寄养',
  DONATION: '捐赠',
  FOLLOW_TASK: '回访',
  PET_CLAIM: '认领',
  PET_RECORD: '宠物档案',
  RESCUE_TASK: '救助任务',
  STOCK: '库存',
  VOLUNTEER: '志愿活动',
}

const loading = ref(false)
const actionCollapsed = ref(false)
const notices = ref([])
const total = ref(0)
const page = reactive({
  page: 1,
  size: 10,
})

const { filters, isActive, applyFilter } = useTableFilters({
  title: { type: 'text' },
  content: { type: 'text' },
  source: { type: 'enum' },
  createTime: { type: 'time' },
  read: { type: 'enum' },
})

const filteredNotices = computed(() => applyFilter(notices.value))

const sourceOptions = computed(() =>
  Object.entries(NOTICE_SOURCE_LABELS).map(([value, label]) => ({
    value,
    label,
  })),
)

const readOptions = [
  { value: true, label: '已读' },
  { value: false, label: '未读' },
]

function sourceText(source) {
  return NOTICE_SOURCE_LABELS[source] || source || '通知'
}

function formatTime(value) {
  if (!value) return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return String(value)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

function dispatchNoticeUpdated() {
  if (typeof window !== 'undefined') {
    window.dispatchEvent(new Event('notice-updated'))
  }
}

async function loadNotices() {
  loading.value = true
  try {
    const result = await getNotices({
      page: page.page,
      size: page.size,
      sort: 'create_time',
      order: 'desc',
    })
    notices.value = Array.isArray(result?.records) ? result.records : []
    total.value = Number(result?.total || notices.value.length)
  } catch (error) {
    ElMessage.warning(error?.message || '加载站内信失败')
  } finally {
    loading.value = false
  }
}

async function setRead(row, read) {
  try {
    if (read) {
      await markNoticeRead([row.id])
      ElMessage.success('已标记为已读')
    } else {
      await markNoticeUnread([row.id])
      ElMessage.success('已标记为未读')
    }
    await loadNotices()
    dispatchNoticeUpdated()
  } catch (error) {
    ElMessage.warning(error?.message || '操作失败')
  }
}

function changePage(nextPage) {
  page.page = nextPage
  loadNotices()
}

function handleRefresh() {
  page.page = 1
  loadNotices()
  dispatchNoticeUpdated()
}

onMounted(() => {
  loadNotices()
  dispatchNoticeUpdated()
})
</script>

<template>
  <el-card class="profile-card notice-panel-card">
    <template #header>
      <div class="profile-card-header">
        <strong>站内信</strong>
        <div class="profile-actions">
          <el-button class="warm-btn" :icon="RefreshRight" :loading="loading" @click="handleRefresh">刷新</el-button>
        </div>
      </div>
    </template>

    <el-table :data="filteredNotices" v-loading="loading" class="notice-table" row-key="id">
      <el-table-column label="来源" width="120">
        <template #header>
          <TableFilterHeader label="来源" :filter="filters.source" type="enum" :options="sourceOptions" :active="isActive('source')" />
        </template>
        <template #default="{ row }">
          <el-tag type="warning" effect="plain">{{ sourceText(row.source) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="标题" min-width="180">
        <template #header>
          <TableFilterHeader label="标题" :filter="filters.title" type="text" :active="isActive('title')" />
        </template>
        <template #default="{ row }">
          <div class="notice-title-cell">
            <strong>{{ row.title || '通知' }}</strong>
            <el-tag v-if="!row.read" size="small" type="danger" effect="plain">未读</el-tag>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="内容" min-width="280">
        <template #header>
          <TableFilterHeader label="内容" :filter="filters.content" type="text" :active="isActive('content')" />
        </template>
        <template #default="{ row }">
          <div class="notice-content-cell">{{ row.content }}</div>
        </template>
      </el-table-column>
      <el-table-column label="时间" width="170">
        <template #header>
          <TableFilterHeader label="时间" :filter="filters.createTime" type="time" :active="isActive('createTime')" />
        </template>
        <template #default="{ row }">
          {{ formatTime(row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #header>
          <TableFilterHeader label="状态" :filter="filters.read" type="enum" :options="readOptions" :active="isActive('read')" />
        </template>
        <template #default="{ row }">
          <el-tag :type="row.read ? 'info' : 'success'" effect="plain">{{ row.read ? '已读' : '未读' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column width="40" class-name="action-col">
        <template #header>
          <TableActionColumnHeader title="操作" :collapsed="actionCollapsed" @toggle="actionCollapsed = !actionCollapsed" />
        </template>
        <template #default="{ row }">
          <div class="table-action-cell">
            <div class="table-action-panel" :class="{ 'is-collapsed': actionCollapsed }">
              <el-button v-if="!row.read" text type="warning" @click="setRead(row, true)">已读</el-button>
              <el-button v-else text type="primary" @click="setRead(row, false)">未读</el-button>
            </div>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <div class="user-admin-pagination">
      <el-pagination
        layout="prev, pager, next, total"
        :current-page="page.page"
        :page-size="page.size"
        :total="total"
        @current-change="changePage"
      />
    </div>
  </el-card>
</template>
