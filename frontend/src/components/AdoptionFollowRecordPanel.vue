<template>
  <el-card class="profile-card pet-admin-card">
    <template #header>
      <div class="profile-card-header">
        <strong>回访任务</strong>
        <div class="profile-actions">
          <el-button class="warm-btn" :icon="RefreshRight" :loading="loading" @click="handleRefresh">刷新</el-button>
        </div>
      </div>
    </template>

    <section class="pet-admin-section">
      <el-table :data="filteredTasks" v-loading="loading" class="user-admin-table">
        <el-table-column min-width="180" show-overflow-tooltip>
          <template #header>
            <TableFilterHeader label="宠物" :filter="filters.petName" type="text" :active="isActive('petName')" />
          </template>
          <template #default="{ row }">
            <button class="table-primary-link adoption-pet-cell" type="button" @click="goTaskDetail(row)">
              <div class="adoption-person-cell">
                <strong>{{ row.petName || '未命名宠物' }}</strong>
                <span>#{{ row.id }} · {{ taskStatusText(row.status) }}</span>
              </div>
            </button>
          </template>
        </el-table-column>
        <el-table-column label="领养人" min-width="160">
          <template #header>
            <TableFilterHeader label="领养人" :filter="filters.applicantName" type="text" :active="isActive('applicantName')" />
          </template>
          <template #default="{ row }">
            <div class="adoption-person-cell">
              <strong>{{ row.applicantName || '未命名申请人' }}</strong>
              <span>#{{ row.applicantId || '—' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column min-width="160">
          <template #header>
            <TableFilterHeader label="计划时间" :filter="filters.planTime" type="time" :active="isActive('planTime')" />
          </template>
          <template #default="{ row }">{{ formatDate(row.planTime) }}</template>
        </el-table-column>
        <el-table-column width="120">
          <template #header>
            <TableFilterHeader label="状态" :filter="filters.status" type="enum" :active="isActive('status')" :options="followStatusFilterOptions" />
          </template>
          <template #default="{ row }">
            <el-tag :type="taskStatusTagType(row.status)" effect="plain">{{ taskStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="志愿者" min-width="160">
          <template #default="{ row }">{{ row.volunteerName || '未命名志愿者' }}</template>
        </el-table-column>
        <el-table-column label="记录摘要" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">{{ row.summary || '' }}</template>
        </el-table-column>
        <el-table-column width="40" class-name="action-col">
          <template #header>
            <TableActionColumnHeader title="操作" :collapsed="actionCollapsed" @toggle="actionCollapsed = !actionCollapsed" />
          </template>
          <template #default="{ row }">
            <div class="table-action-cell">
              <div class="table-action-panel" :class="{ 'is-collapsed': actionCollapsed }">
                <el-button v-if="canApprove(row)" text type="success" :loading="actionLoadingId === actionKey(row, 'approve')" @click="submitTaskAction(row, 'approve')">同意</el-button>
                <el-button v-if="canReject(row)" text type="danger" :loading="actionLoadingId === actionKey(row, 'reject')" @click="submitTaskAction(row, 'reject')">拒绝</el-button>
                <el-button v-if="canRevoke(row)" text type="warning" :loading="actionLoadingId === actionKey(row, 'revoke')" @click="submitTaskAction(row, 'revoke')">撤销</el-button>
                <el-button v-if="canModify(row)" text type="primary" @click="openModifyDialog(row)">修改</el-button>
                <el-button v-if="canExecute(row)" text type="success" :loading="actionLoadingId === actionKey(row, 'execute')" @click="submitTaskAction(row, 'execute')">执行</el-button>
                <el-button v-if="canFinish(row)" text type="success" :loading="actionLoadingId === actionKey(row, 'finish')" @click="submitTaskAction(row, 'finish')">完成</el-button>
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

  <el-dialog v-model="modifyDialogVisible" title="修改回访任务" width="560px" :close-on-click-modal="false">
    <el-form label-position="top" class="follow-task-modify-form">
      <el-form-item label="志愿者">
        <el-select
          v-model="modifyForm.volunteerId"
          filterable
          class="full-width-control"
          placeholder="选择志愿者"
          :loading="volunteerLoading"
        >
          <el-option
            v-for="item in volunteerOptions"
            :key="volunteerValue(item)"
            :label="volunteerLabel(item)"
            :value="volunteerValue(item)"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="计划时间">
        <el-date-picker
          v-model="modifyForm.planTime"
          type="datetime"
          value-format="YYYY-MM-DDTHH:mm:ss.SSS"
          placeholder="选择计划时间"
          class="full-width-control"
        />
      </el-form-item>
      <el-form-item label="备注">
        <el-input
          v-model="modifyForm.remark"
          type="textarea"
          :autosize="{ minRows: 3, maxRows: 5 }"
          placeholder="填写调整说明"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="modifyDialogVisible = false">取消</el-button>
      <el-button class="warm-btn" :loading="actionLoadingId === 'modify'" @click="submitModify">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { RefreshRight } from '@element-plus/icons-vue'
import { getFollowTasks, updateFollowTask } from '../api/services'
import { getVolunteerProfiles } from '../api/volunteer'
import { useUserStore } from '../stores/user'
import { ROLE, hasRole } from '../utils/roles'
import TableActionColumnHeader from './TableActionColumnHeader.vue'
import TableFilterHeader from './TableFilterHeader.vue'
import { useTableFilters } from '../composables/useTableFilters'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const volunteerLoading = ref(false)
const actionCollapsed = ref(false)
const actionLoadingId = ref('')
const modifyDialogVisible = ref(false)
const modifyTarget = ref(null)
const volunteerOptions = ref([])
const rows = ref([])
const total = ref(0)
const page = reactive({ page: 1, size: 10 })
const modifyForm = reactive({
  volunteerId: '',
  planTime: '',
  remark: '',
})

const { filters, isActive, applyFilter } = useTableFilters({
  petName: { type: 'text' },
  applicantName: { type: 'text' },
  status: { type: 'enum' },
  planTime: { type: 'time' },
})

const followStatusFilterOptions = [
  { value: 'CREATE', label: '已创建' },
  { value: 'NOTIFIED', label: '已通知' },
  { value: 'IN_PROGRESS', label: '进行中' },
  { value: 'DELAY', label: '已延迟' },
  { value: 'FINISH', label: '已完成' },
]

const filteredTasks = computed(() => applyFilter(rows.value || []))

const loginRole = computed(() => Number(userStore.profile?.role || 0))
const loginUserId = computed(() => String(userStore.profile?.id || ''))
const isWorker = computed(() => hasRole(loginRole.value, ROLE.WORKER) || hasRole(loginRole.value, ROLE.ADMIN))
const isAdmin = computed(() => hasRole(loginRole.value, ROLE.ADMIN))
const isVolunteer = computed(() => hasRole(loginRole.value, ROLE.VOLUNTEER))
const subtitle = computed(() => {
  if (isWorker.value) return '查看全部回访任务'
  if (isVolunteer.value) return '查看自己参与和自己领养宠物的回访任务'
  return '查看自己领养宠物的回访任务'
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

function taskStatusTagType(value) {
  if (value === 'FINISH') return 'success'
  if (value === 'DELAY') return 'info'
  if (value === 'IN_PROGRESS' || value === 'NOTIFIED') return 'primary'
  return 'warning'
}

function formatDate(value) {
  if (!value) return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return String(value)
  return date.toLocaleString('zh-CN')
}

function isNotified(row) {
  return row?.status === 'NOTIFIED'
}

function isTaskVolunteer(row) {
  return String(row?.volunteerId || '') === loginUserId.value
}

function isTaskWorker(row) {
  return String(row?.workerId || '') === loginUserId.value
}

function isApplicant(row) {
  return String(row?.applicantId || '') === loginUserId.value
}

function canApprove(row) {
  return isNotified(row) && isVolunteer.value && isTaskVolunteer(row)
}

function canReject(row) {
  return isNotified(row) && ((isVolunteer.value && isTaskVolunteer(row)) || isApplicant(row))
}

function canRevoke(row) {
  return isNotified(row) && (isAdmin.value || (isWorker.value && isTaskVolunteer(row)))
}

function canModify(row) {
  return isNotified(row) && (isAdmin.value || (isWorker.value && isTaskWorker(row)))
}

function canExecute(row) {
  return isNotified(row) && isVolunteer.value && isTaskVolunteer(row)
}

function canFinish(row) {
  return row?.status === 'IN_PROGRESS' && hasRole(loginRole.value, ROLE.WORKER) && isTaskWorker(row)
}

function actionKey(row, action) {
  return `${row?.id || ''}:${action}`
}

function buildUpdatePayload(row, status, overrides = {}) {
  return {
    workerId: overrides.workerId ?? row.workerId,
    volunteerId: overrides.volunteerId ?? row.volunteerId,
    planTime: overrides.planTime ?? row.planTime,
    status,
    remark: overrides.remark ?? row.remark ?? undefined,
  }
}

async function submitTaskAction(row, action) {
  if (!row?.id || actionLoadingId.value) return
  const statusMap = {
    approve: 'IN_PROGRESS',
    reject: 'DELAY',
    revoke: 'CREATE',
    execute: 'IN_PROGRESS',
    finish: 'FINISH',
  }
  const messageMap = {
    approve: '确认同意这项回访任务？',
    reject: '确认拒绝这项回访任务？',
    revoke: '确认撤销这项回访任务通知？',
    execute: '确认开始执行这项回访任务？',
    finish: '确认完成这项回访任务？',
  }
  const successMap = {
    approve: '已同意回访任务',
    reject: '已拒绝回访任务',
    revoke: '已撤销回访任务通知',
    execute: '回访任务已进入执行中',
    finish: '回访任务已完成',
  }
  try {
    await ElMessageBox.confirm(messageMap[action], '回访任务', {
      type: action === 'reject' ? 'warning' : 'info',
      confirmButtonText: '确认',
      cancelButtonText: '取消',
    })
    actionLoadingId.value = actionKey(row, action)
    await updateFollowTask(row.id, buildUpdatePayload(row, statusMap[action]))
    ElMessage.success(successMap[action])
    await loadRows()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.warning(error?.message || '操作回访任务失败')
    }
  } finally {
    actionLoadingId.value = ''
  }
}

function volunteerName(item) {
  return item?.realName || item?.username || '未命名志愿者'
}

function volunteerLabel(item) {
  const phone = item?.phone ? ` · ${item.phone}` : ''
  return `${volunteerName(item)}${phone}`
}

function volunteerValue(item) {
  return String(item?.userId || item?.id || '')
}

async function loadVolunteerOptions() {
  volunteerLoading.value = true
  try {
    const result = await getVolunteerProfiles({ size: 100, status: ['ACTIVE'] })
    volunteerOptions.value = Array.isArray(result?.records) ? result.records : []
  } catch (error) {
    volunteerOptions.value = []
    ElMessage.warning(error?.message || '加载志愿者列表失败')
  } finally {
    volunteerLoading.value = false
  }
}

async function openModifyDialog(row) {
  modifyTarget.value = row
  modifyForm.volunteerId = String(row?.volunteerId || '')
  modifyForm.planTime = row?.planTime || ''
  modifyForm.remark = row?.remark || ''
  modifyDialogVisible.value = true
  if (!volunteerOptions.value.length) {
    await loadVolunteerOptions()
  }
}

async function submitModify() {
  if (!modifyTarget.value || actionLoadingId.value) return
  if (!modifyForm.volunteerId) {
    ElMessage.warning('请选择志愿者')
    return
  }
  if (!modifyForm.planTime) {
    ElMessage.warning('请选择计划时间')
    return
  }
  actionLoadingId.value = 'modify'
  try {
    await updateFollowTask(modifyTarget.value.id, buildUpdatePayload(modifyTarget.value, 'NOTIFIED', {
      volunteerId: modifyForm.volunteerId,
      planTime: modifyForm.planTime,
      remark: modifyForm.remark.trim() || undefined,
    }))
    ElMessage.success('回访任务已修改并重新通知')
    modifyDialogVisible.value = false
    await loadRows()
  } catch (error) {
    ElMessage.warning(error?.message || '修改回访任务失败')
  } finally {
    actionLoadingId.value = ''
  }
}

async function loadRows() {
  loading.value = true
  try {
    const result = await getFollowTasks({
      page: page.page,
      size: page.size,
      sort: 'plan_time',
      order: 'desc',
    })
    rows.value = Array.isArray(result?.records) ? result.records : []
    total.value = Number(result?.total || rows.value.length)
  } catch (error) {
    ElMessage.warning(error?.message || '加载回访任务失败')
  } finally {
    loading.value = false
  }
}

function handleRefresh() {
  page.page = 1
  loadRows()
}

function changePage(value) {
  page.page = value
  loadRows()
}

function goTaskDetail(row) {
  if (!row?.id) return
  router.push({
    name: 'console-adoption-follow-task-detail',
    params: { id: String(row.id) },
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

.follow-task-modify-form {
  display: grid;
  gap: 2px;
}
</style>
