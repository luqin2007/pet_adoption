<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, RefreshRight, Search } from '@element-plus/icons-vue'
import { deleteArticle, getArticles, updateArticleStatus } from '../api/publicity'
import { useUserStore } from '../stores/user'
import { useRouter } from 'vue-router'
import TableActionColumnHeader from './TableActionColumnHeader.vue'

const props = defineProps({
  mode: {
    type: String,
    default: 'mine',
  },
})

const router = useRouter()
const userStore = useUserStore()

const ROLE = {
  VOLUNTEER: 1,
  WORKER: 2,
}

const ARTICLE_TYPE_OPTIONS = [
  { label: '救助故事', value: 'STORY' },
  { label: '活动推广', value: 'ACTIVITY' },
  { label: '科普知识', value: 'KNOWLEDGE' },
]

const ARTICLE_STATUS_OPTIONS = [
  { label: '草稿', value: 'DRAFT' },
  { label: '已发布', value: 'PUBLISHED' },
  { label: '已下线', value: 'OFFLINE' },
]

const loading = ref(false)
const rows = ref([])
const total = ref(0)
const actionCollapsed = ref(false)

const page = reactive({
  page: 1,
  size: 10,
})

const searchForm = reactive({
  title: '',
  type: '',
  status: '',
  timeRange: [],
})

const isWorker = computed(() => (Number(userStore.profile.role || 0) & ROLE.WORKER) === ROLE.WORKER)
const isVolunteer = computed(() => (Number(userStore.profile.role || 0) & ROLE.VOLUNTEER) === ROLE.VOLUNTEER)
const loginUserId = computed(() => Number(userStore.profile.id || 0))
const isManageMode = computed(() => props.mode === 'manage')
const canUseCurrentMode = computed(() => {
  if (isManageMode.value) {
    return isWorker.value
  }
  return isWorker.value || isVolunteer.value
})
const pageTitle = computed(() => (isManageMode.value ? '文章管理' : '我的文章'))
const pageHint = computed(() => {
  if (isManageMode.value) {
    return '工作人员可在这里统一查看已发布内容，并按业务需要下线文章或活动。'
  }
  return isWorker.value
    ? '你可以创建和管理自己的救助故事、活动推广与科普知识。'
    : '你可以创建和管理自己的救助故事，其他类型仅工作人员可发布。'
})
const articleTypeOptions = computed(() => (isManageMode.value || isWorker.value ? ARTICLE_TYPE_OPTIONS : ARTICLE_TYPE_OPTIONS.filter((item) => item.value === 'STORY')))

function buildQuery() {
  return {
    page: page.page,
    size: page.size,
    author: isManageMode.value ? undefined : loginUserId.value || undefined,
    title: searchForm.title.trim() || undefined,
    type: searchForm.type || undefined,
    status: searchForm.status ? [searchForm.status] : undefined,
    time0: searchForm.timeRange?.[0] || undefined,
    time1: searchForm.timeRange?.[1] || undefined,
  }
}

async function loadArticles() {
  if (!canUseCurrentMode.value) {
    rows.value = []
    total.value = 0
    return
  }
  loading.value = true
  try {
    const result = await getArticles(buildQuery())
    rows.value = Array.isArray(result?.records) ? result.records : []
    total.value = Number(result?.total || 0)
  } catch (error) {
    rows.value = []
    total.value = 0
    ElMessage.warning(error?.message || '加载文章失败')
  } finally {
    loading.value = false
  }
}

function resetSearch() {
  searchForm.title = ''
  searchForm.type = ''
  searchForm.status = ''
  searchForm.timeRange = []
  page.page = 1
  loadArticles()
}

function changePage(current) {
  page.page = current
  loadArticles()
}

function goCreateArticle() {
  router.push('/console/articles/new')
}

function goEditArticle(row) {
  router.push(`/console/articles/${row.id}/edit`)
}

function typeText(type) {
  return ARTICLE_TYPE_OPTIONS.find((item) => item.value === type)?.label || '公益文章'
}

function statusText(status) {
  return ARTICLE_STATUS_OPTIONS.find((item) => item.value === status)?.label || '未知状态'
}

function statusTagType(status) {
  if (status === 'PUBLISHED') return 'success'
  if (status === 'OFFLINE') return 'info'
  return 'warning'
}

function formatDate(value) {
  if (!value) {
    return '—'
  }
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return String(value)
  }
  return date.toLocaleString('zh-CN')
}

function canEdit(row) {
  return !isManageMode.value && row.status === 'DRAFT'
}

function canPublish(row) {
  return !isManageMode.value && row.status === 'DRAFT'
}

function canDelete(row) {
  return !isManageMode.value && row.status === 'DRAFT'
}

function canTakeDown(row) {
  return isManageMode.value && row.status === 'PUBLISHED'
}

function resolveTakeDownStatus(row) {
  return row.type === 'ACTIVITY' ? 'OFFLINE' : 'DRAFT'
}

async function changeStatus(row, status) {
  const label = status === 'PUBLISHED' ? '发布' : status === 'OFFLINE' ? '下线' : '转为草稿'
  try {
    await ElMessageBox.confirm(`确认${label}《${row.title}》吗？`, '文章状态', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await updateArticleStatus(row.id, status)
    ElMessage.success(`已${label}`)
    loadArticles()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.warning(error?.message || '状态修改失败')
    }
  }
}

async function removeArticle(row) {
  try {
    await ElMessageBox.confirm(`确认删除草稿《${row.title}》吗？`, '删除文章', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await deleteArticle(row.id)
    ElMessage.success('草稿已删除')
    if (rows.value.length === 1 && page.page > 1) {
      page.page -= 1
    }
    loadArticles()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.warning(error?.message || '删除文章失败')
    }
  }
}

watch(
  () => props.mode,
  () => {
    page.page = 1
    resetSearch()
  },
)

onMounted(() => {
  loadArticles()
})
</script>

<template>
  <el-card class="profile-card pet-admin-card article-admin-card">
    <template #header>
      <div class="profile-card-header">
        <strong>{{ pageTitle }}</strong>
        <span>{{ pageHint }}</span>
      </div>
    </template>

    <section v-if="canUseCurrentMode" class="pet-admin-section article-admin-shell">
      <div class="console-filter-form article-console-filter-form">
        <div class="console-filter-grid article-filter-grid article-filter-grid-primary">
          <el-form-item label="标题">
            <el-input v-model="searchForm.title" clearable placeholder="按标题搜索" @keyup.enter="page.page = 1; loadArticles()" />
          </el-form-item>
          <el-form-item label="文章类型">
            <el-select v-model="searchForm.type" clearable placeholder="全部类型">
              <el-option v-for="item in articleTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="文章状态">
            <el-select v-model="searchForm.status" clearable placeholder="全部状态">
              <el-option v-for="item in ARTICLE_STATUS_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
        </div>

        <div class="article-filter-grid-secondary">
          <el-form-item label="发布时间" class="article-time-field">
            <el-date-picker
              v-model="searchForm.timeRange"
              type="datetimerange"
              value-format="YYYY-MM-DD HH:mm:ss"
              range-separator="至"
              start-placeholder="开始时间"
              end-placeholder="结束时间"
              class="full-width-control"
            />
          </el-form-item>
          <div class="article-filter-actions article-filter-actions-left">
            <el-button class="soft-btn" :icon="RefreshRight" @click="resetSearch">重置</el-button>
            <el-button v-if="!isManageMode" class="soft-btn" :icon="Plus" @click="goCreateArticle">发表文章</el-button>
            <el-button class="soft-btn article-search-btn" :icon="Search" :loading="loading" @click="page.page = 1; loadArticles()">搜索</el-button>
          </div>
        </div>
      </div>

      <el-table :data="rows" v-loading="loading" class="user-admin-table">
        <el-table-column prop="title" label="标题" min-width="260" show-overflow-tooltip />
        <el-table-column v-if="isManageMode" label="作者" min-width="140">
          <template #default="{ row }">{{ row.authorName || '—' }}</template>
        </el-table-column>
        <el-table-column label="类型" width="120">
          <template #default="{ row }">
            <el-tag effect="plain" type="warning">{{ typeText(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag effect="plain" :type="statusTagType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="发布时间" min-width="175">
          <template #default="{ row }">{{ formatDate(row.publishTime) }}</template>
        </el-table-column>
        <el-table-column label="更新时间" min-width="175">
          <template #default="{ row }">{{ formatDate(row.updateTime) }}</template>
        </el-table-column>
        <el-table-column label="数据" width="170">
          <template #default="{ row }">
            <div class="article-metrics-cell">
              <span>浏览 {{ row.viewCount || 0 }}</span>
              <span>点赞 {{ row.likeCount || 0 }}</span>
              <span>分享 {{ row.shareCount || 0 }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column :width="actionCollapsed ? 52 : 230" fixed="right">
          <template #header>
            <TableActionColumnHeader
              :title="isManageMode ? '处理' : '操作'"
              :collapsed="actionCollapsed"
              @toggle="actionCollapsed = !actionCollapsed"
            />
          </template>
          <template #default="{ row }">
            <div class="table-action-cell">
              <div class="table-action-panel" :class="{ 'is-collapsed': actionCollapsed }">
                <el-button v-if="canEdit(row)" text type="warning" @click="goEditArticle(row)">编辑</el-button>
                <el-button v-if="canPublish(row)" text type="primary" @click="changeStatus(row, 'PUBLISHED')">发布</el-button>
                <el-button v-if="canDelete(row)" text type="danger" @click="removeArticle(row)">删除</el-button>
                <el-button v-if="canTakeDown(row)" text type="warning" @click="changeStatus(row, resolveTakeDownStatus(row))">下线</el-button>
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
    </section>

    <section v-else class="pet-admin-section">
      <el-empty description="当前账号没有文章管理权限" />
    </section>
  </el-card>
</template>
