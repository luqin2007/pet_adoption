<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, RefreshRight } from '@element-plus/icons-vue'
import { deleteArticle, getArticles, getFavoriteArticles, unfavoriteArticle, updateArticleStatus } from '../api/article'
import { useUserStore } from '../stores/user'
import { useRouter } from 'vue-router'
import TableActionColumnHeader from './TableActionColumnHeader.vue'
import { useTableFilters } from '../composables/useTableFilters'
import TableFilterHeader from './TableFilterHeader.vue'
import { ROLE, hasRole } from '../utils/roles'

const router = useRouter()
const userStore = useUserStore()

const activeTab = ref('favorites')

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

const loginRole = computed(() => Number(userStore.profile.role || 0))
const isAdmin = computed(() => hasRole(loginRole.value, ROLE.ADMIN))
const isWorker = computed(() => isAdmin.value || hasRole(loginRole.value, ROLE.WORKER))
const isVolunteer = computed(() => hasRole(loginRole.value, ROLE.VOLUNTEER))
const loginUserId = computed(() => String(userStore.profile.id || ''))
const isManageMode = computed(() => activeTab.value === 'manage')
const isFavoriteMode = computed(() => activeTab.value === 'favorites')
const isMineMode = computed(() => activeTab.value === 'mine')

async function loadArticles() {
  loading.value = true
  try {
    const result = activeTab.value === 'favorites'
      ? await getFavoriteArticles({ page: page.page, size: page.size })
      : await getArticles({
          page: page.page,
          size: page.size,
          author: activeTab.value === 'manage' ? undefined : loginUserId.value || undefined,
          isDiscard: false,
        })
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

const { filters, isActive, applyFilter } = useTableFilters({
  title: { type: 'text' },
  type: { type: 'enum' },
  status: { type: 'enum' },
  publishTime: { type: 'time' },
})

const typeOptions = [
  { value: 'STORY', label: '救助故事' },
  { value: 'ACTIVITY', label: '活动' },
  { value: 'KNOWLEDGE', label: '科普知识' },
]

const statusOptions = [
  { value: 'DRAFT', label: '草稿' },
  { value: 'PUBLISHED', label: '已发布' },
  { value: 'OFFLINE', label: '已下线' },
]

const filteredArticles = computed(() => applyFilter(rows.value || []))

function handleRefresh() {
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
  if (isMineMode.value && row.status === 'DRAFT') {
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
  return isMineMode.value && row.status === 'DRAFT'
}

function canPublish(row) {
  return isMineMode.value && row.status === 'DRAFT'
}

function canDelete(row) {
  return isMineMode.value && row.status === 'DRAFT'
}

function canTakeDown(row) {
  return isManageMode.value && row.status === 'PUBLISHED'
}

function canRemoveFavorite(row) {
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

watch(activeTab, () => {
  page.page = 1
  loadArticles()
})

onMounted(() => {
  loadArticles()
})
</script>

<template>
  <el-card class="profile-card pet-admin-card article-admin-card">
    <template #header>
      <div class="profile-card-header">
        <strong>公益文章</strong>
        <div class="profile-actions">
          <el-button v-if="!isFavoriteMode" class="warm-btn" :icon="Plus" @click="goCreateArticle">发表文章</el-button>
          <el-button class="warm-btn" :icon="RefreshRight" :loading="loading" @click="handleRefresh">刷新</el-button>
        </div>
      </div>
    </template>

    <el-tabs v-model="activeTab" class="article-tabs">
      <el-tab-pane label="我的收藏" name="favorites" />
      <el-tab-pane v-if="isWorker || isVolunteer" label="我的文章" name="mine" />
      <el-tab-pane v-if="isWorker" label="文章管理" name="manage" />
    </el-tabs>

    <section class="pet-admin-section article-admin-shell">
      <el-table :data="filteredArticles" v-loading="loading" class="user-admin-table">
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
  </el-card>
</template>

<style scoped>
.article-tabs {
  margin-bottom: 8px;
}
</style>