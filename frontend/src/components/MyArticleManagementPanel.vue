<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, RefreshRight, Search } from '@element-plus/icons-vue'
import { deleteArticle, getArticles, getFavoriteArticles, unfavoriteArticle, updateArticleStatus } from '../api/article'
import { useUserStore } from '../stores/user'
import { useRouter } from 'vue-router'
import TableActionColumnHeader from './TableActionColumnHeader.vue'
import { ROLE, hasRole } from '../utils/roles'

const props = defineProps({
  mode: {
    type: String,
    default: 'mine',
  },
})

const router = useRouter()
const userStore = useUserStore()

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
  time0: '',
  time1: '',
})

const loginRole = computed(() => Number(userStore.profile.role || 0))
const isAdmin = computed(() => hasRole(loginRole.value, ROLE.ADMIN))
const isWorker = computed(() => isAdmin.value || hasRole(loginRole.value, ROLE.WORKER))
const isVolunteer = computed(() => hasRole(loginRole.value, ROLE.VOLUNTEER))
const loginUserId = computed(() => String(userStore.profile.id || ''))
const isManageMode = computed(() => props.mode === 'manage')
const isFavoriteMode = computed(() => props.mode === 'favorites')
const canUseCurrentMode = computed(() => {
  if (isManageMode.value) {
    return isWorker.value
  }
  if (isFavoriteMode.value) {
    return Boolean(userStore.accessToken)
  }
  return isWorker.value || isVolunteer.value
})
const pageTitle = computed(() => {
  if (isManageMode.value) return '文章管理'
  if (isFavoriteMode.value) return '我的收藏'
  return '我的文章'
})
const pageHint = computed(() => {
  if (isManageMode.value) {
    return '查看已发布内容，必要时下线文章或活动。'
  }
  if (isFavoriteMode.value) {
    return '查看和整理你收藏的公益文章。'
  }
  return isWorker.value
    ? '管理你的救助故事、活动消息和养护知识。'
    : '管理你的救助故事。'
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
    time0: searchForm.time0 || undefined,
    time1: searchForm.time1 || undefined,
    isDiscard: false,
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
    const result = isFavoriteMode.value
      ? await getFavoriteArticles({ page: page.page, size: page.size })
      : await getArticles(buildQuery())
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
  searchForm.time0 = ''
  searchForm.time1 = ''
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

function goArticlePage(row) {
  if (!row?.id) return
  if (!isManageMode.value && row.status === 'DRAFT') {
    goEditArticle(row)
    return
  }
  router.push(`/articles/${row.id}`)
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
  return !isManageMode.value && !isFavoriteMode.value && row.status === 'DRAFT'
}

function canPublish(row) {
  return !isManageMode.value && !isFavoriteMode.value && row.status === 'DRAFT'
}

function canDelete(row) {
  return !isManageMode.value && !isFavoriteMode.value && row.status === 'DRAFT'
}

function canTakeDown(row) {
  return isManageMode.value && row.status === 'PUBLISHED'
}

function canRemoveFavorite() {
  return isFavoriteMode.value
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

async function removeFavorite(row) {
  try {
    await ElMessageBox.confirm(`确认取消收藏《${row.title}》吗？`, '取消收藏', {
      confirmButtonText: '取消收藏',
      cancelButtonText: '返回',
      type: 'warning',
    })
    await unfavoriteArticle(row.id)
    ElMessage.success('已取消收藏')
    if (rows.value.length === 1 && page.page > 1) {
      page.page -= 1
    }
    loadArticles()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.warning(error?.message || '取消收藏失败')
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
      <section v-if="!isFavoriteMode" class="filter-panel pet-directory-filter-panel article-directory-filter-panel">
        <div class="pet-filter-row article-filter-row-inline article-filter-cols-3">
          <el-input v-model="searchForm.title" clearable placeholder="标题" @keyup.enter="page.page = 1; loadArticles()" />
          <el-select v-model="searchForm.type" clearable placeholder="文章类型">
            <el-option v-for="item in articleTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
          <el-select v-model="searchForm.status" clearable placeholder="文章状态">
            <el-option v-for="item in ARTICLE_STATUS_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </div>

        <div class="pet-filter-row article-filter-row-secondary article-filter-cols-row2">
          <el-date-picker v-model="searchForm.time0" type="date" value-format="YYYY-MM-DD" placeholder="开始日期" class="full-width-control" />
          <el-date-picker v-model="searchForm.time1" type="date" value-format="YYYY-MM-DD" placeholder="结束日期" class="full-width-control" />
          <div class="pet-filter-action article-filter-action">
            <el-button v-if="!isManageMode" class="soft-btn" :icon="Plus" @click="goCreateArticle">发表文章</el-button>
            <el-button class="warm-btn" :icon="Search" :loading="loading" @click="page.page = 1; loadArticles()">搜索</el-button>
          </div>
        </div>
      </section>

      <section v-else class="filter-panel pet-directory-filter-panel article-directory-filter-panel">
        <div class="pet-filter-row article-filter-row-secondary article-favorite-toolbar">
          <span>收藏文章会显示在这里，便于稍后阅读。</span>
          <div class="pet-filter-action article-filter-action">
            <el-button class="soft-btn" :icon="RefreshRight" :loading="loading" @click="loadArticles">刷新</el-button>
          </div>
        </div>
      </section>

      <el-table :data="rows" v-loading="loading" class="user-admin-table">
        <el-table-column label="标题" min-width="260" show-overflow-tooltip>
          <template #default="{ row }">
            <button class="table-primary-link" type="button" @click="goArticlePage(row)">{{ row.title || '未命名文章' }}</button>
          </template>
        </el-table-column>
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
        <el-table-column label="互动" width="170">
          <template #default="{ row }">
            <div class="article-metrics-cell">
              <span>浏览 {{ row.viewCount || 0 }}</span>
              <span>点赞 {{ row.likeCount || 0 }}</span>
              <span>分享 {{ row.shareCount || 0 }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column width="40" class-name="action-col">
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
                <el-button v-if="canRemoveFavorite(row)" text type="danger" @click="removeFavorite(row)">取消收藏</el-button>
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
      <el-empty description="没有文章管理权限" />
    </section>
  </el-card>
</template>

<style scoped>
.article-directory-filter-panel .article-filter-cols-3 {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}
.article-directory-filter-panel .article-filter-cols-row2 {
  grid-template-columns: 1fr 1fr auto;
}
.article-directory-filter-panel .article-favorite-toolbar {
  grid-template-columns: minmax(0, 1fr) auto;
  align-items: center;
}
.article-favorite-toolbar > span {
  color: var(--muted);
  font-size: 14px;
}
</style>
