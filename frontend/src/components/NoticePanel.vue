<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { RefreshRight } from '@element-plus/icons-vue'
import { getNotices, markNoticeRead, markNoticeUnread } from '../api/notice'
import TableActionColumnHeader from './TableActionColumnHeader.vue'

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
const filters = reactive({
  read: '',
  source: '',
  timeRange: [],
})

const sourceOptions = computed(() =>
  Object.entries(NOTICE_SOURCE_LABELS).map(([value, label]) => ({
    value,
    label,
  })),
)

function sourceText(source) {
  return NOTICE_SOURCE_LABELS[source] || source || '通知'
}

function formatTime(value) {
  if (!value) {
    return ''
  }
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return String(value)
  }
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
  const [time0, time1] = filters.timeRange || []
  loading.value = true
  try {
    const result = await getNotices({
      page: page.page,
      size: page.size,
      sort: 'create_time',
      order: 'desc',
      read: filters.read === '' ? undefined : filters.read,
      source: filters.source ? [filters.source] : undefined,
      time0: time0 || undefined,
      time1: time1 || undefined,
    })
    notices.value = Array.isArray(result?.records) ? result.records : []
    total.value = Number(result?.total || notices.value.length)
  } catch (error) {
    ElMessage.warning(error?.message || '加载站内信失败')
  } finally {
    loading.value = false
  }
}

function resetPageAndLoad() {
  page.page = 1
  loadNotices()
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
        <span>查看系统通知和工作人员发送的消息</span>
      </div>
    </template>

    <section class="filter-panel pet-directory-filter-panel notice-filter-panel">
      <div class="pet-filter-row pet-filter-row-primary">
        <el-select v-model="filters.read" class="filter-field-sm" clearable placeholder="阅读状态" @change="resetPageAndLoad">
          <el-option label="未读" :value="false" />
          <el-option label="已读" :value="true" />
        </el-select>
        <el-select v-model="filters.source" class="filter-field-sm" clearable filterable placeholder="通知来源" @change="resetPageAndLoad">
          <el-option
            v-for="item in sourceOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
        <el-date-picker
          v-model="filters.timeRange"
          type="daterange"
          value-format="YYYY-MM-DD HH:mm:ss"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
          class="filter-field-lg"
          @change="resetPageAndLoad"
        />
        <div class="pet-filter-action">
          <el-button class="warm-btn" :icon="RefreshRight" :loading="loading" @click="loadNotices">刷新</el-button>
        </div>
      </div>
    </section>

    <el-table :data="notices" v-loading="loading" class="notice-table" row-key="id">
      <el-table-column label="标题" min-width="180">
        <template #default="{ row }">
          <div class="notice-title-cell">
            <strong>{{ row.title || '通知' }}</strong>
            <el-tag v-if="!row.read" size="small" type="danger" effect="plain">未读</el-tag>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="来源" width="120">
        <template #default="{ row }">
          <el-tag type="warning" effect="plain">{{ sourceText(row.source) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="内容" min-width="280">
        <template #default="{ row }">
          <div class="notice-content-cell">{{ row.content }}</div>
        </template>
      </el-table-column>
      <el-table-column label="时间" width="170">
        <template #default="{ row }">
          {{ formatTime(row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
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
