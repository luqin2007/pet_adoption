<template>
  <el-card class="profile-card pet-admin-card">
    <template #header>
      <div class="profile-card-header">
        <strong>回访记录</strong>
        <span>{{ subtitle }}</span>
      </div>
    </template>

    <section class="pet-admin-section">
      <el-table :data="rows" v-loading="loading" class="user-admin-table">
        <el-table-column label="宠物" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            <button class="table-primary-link adoption-pet-cell" type="button" @click="goTaskDetail(row)">
              <div class="adoption-person-cell">
                <strong>{{ row.petName || '未命名宠物' }}</strong>
                <span>#{{ row.taskId }} · {{ taskStatusText(row.taskStatus) }}</span>
              </div>
            </button>
          </template>
        </el-table-column>
        <el-table-column label="申请人" min-width="160">
          <template #default="{ row }">
            <div class="adoption-person-cell">
              <strong>{{ row.applicantName || '未命名申请人' }}</strong>
              <span>#{{ row.applicantId || '—' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="回访时间" min-width="160">
          <template #default="{ row }">{{ formatDate(row.visitTime) }}</template>
        </el-table-column>
        <el-table-column label="志愿者" min-width="160">
          <template #default="{ row }">{{ row.volunteerName || '未命名志愿者' }}</template>
        </el-table-column>
        <el-table-column label="摘要" min-width="240" show-overflow-tooltip>
          <template #default="{ row }">{{ row.summary || '暂无' }}</template>
        </el-table-column>
        <el-table-column width="40" class-name="action-col">
          <template #header>
            <TableActionColumnHeader title="操作" :collapsed="actionCollapsed" @toggle="actionCollapsed = !actionCollapsed" />
          </template>
          <template #default="{ row }">
            <div class="table-action-cell">
              <div class="table-action-panel" :class="{ 'is-collapsed': actionCollapsed }">
                <el-button text type="primary" @click="goTaskDetail(row)">查看任务</el-button>
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

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getVisibleFollowRecords } from '../api/services'
import { useUserStore } from '../stores/user'
import { ROLE, hasRole } from '../utils/roles'
import TableActionColumnHeader from './TableActionColumnHeader.vue'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const actionCollapsed = ref(false)
const rows = ref([])
const total = ref(0)
const page = reactive({ page: 1, size: 10 })

const loginRole = computed(() => Number(userStore.profile?.role || 0))
const isWorker = computed(() => hasRole(loginRole.value, ROLE.WORKER) || hasRole(loginRole.value, ROLE.ADMIN))
const isVolunteer = computed(() => hasRole(loginRole.value, ROLE.VOLUNTEER))
const subtitle = computed(() => {
  if (isWorker.value) return '查看全部回访记录'
  if (isVolunteer.value) return '查看自己参与和自己领养宠物的回访记录'
  return '查看自己领养宠物的回访记录'
})

function taskStatusText(value) {
  const map = {
    CREATE: '刚创建',
    NOTIFIED: '已通知',
    IN_PROGRESS: '执行中',
    DELAY: '推迟',
    FINISH: '已完成',
  }
  return map[value] || value || ''
}

function formatDate(value) {
  if (!value) return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return String(value)
  return date.toLocaleString('zh-CN')
}

async function loadRows() {
  loading.value = true
  try {
    const result = await getVisibleFollowRecords({
      page: page.page,
      size: page.size,
      sort: 'visit_time',
      order: 'desc',
    })
    rows.value = Array.isArray(result?.records) ? result.records : []
    total.value = Number(result?.total || rows.value.length)
  } catch (error) {
    ElMessage.warning(error?.message || '加载回访记录失败')
  } finally {
    loading.value = false
  }
}

function changePage(value) {
  page.page = value
  loadRows()
}

function goTaskDetail(row) {
  if (!row?.taskId) return
  router.push({
    name: 'console-adoption-follow-task-detail',
    params: { id: String(row.taskId) },
  })
}

onMounted(loadRows)
</script>

<style scoped>
.adoption-person-cell {
  display: grid;
  gap: 3px;
}

.adoption-pet-cell {
  display: inline-flex;
  width: 100%;
  padding: 0;
  text-align: left;
}

.adoption-person-cell strong {
  color: var(--text);
}

.adoption-person-cell span {
  color: var(--muted);
  font-size: 12px;
}
</style>
