<template>
  <section class="pet-admin-section">
    <el-table :data="followFilteredTasks" v-loading="followLoading" class="user-admin-table">
      <el-table-column min-width="180" show-overflow-tooltip>
        <template #header>
          <TableFilterHeader label="宠物" :filter="followFilters.petName" type="text" :active="followIsActive('petName')" />
        </template>
        <template #default="{ row }">
          <button class="table-primary-link adoption-pet-cell" type="button" @click="goFollowTaskDetail(row)">
            <div class="adoption-person-cell">
              <strong>{{ row.petName || '未命名宠物' }}</strong>
              <span>#{{ row.id }} · {{ followStatusText(row.status) }}</span>
            </div>
          </button>
        </template>
      </el-table-column>
      <el-table-column label="领养人" min-width="160">
        <template #header>
          <TableFilterHeader label="领养人" :filter="followFilters.applicantName" type="text" :active="followIsActive('applicantName')" />
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
          <TableFilterHeader label="计划时间" :filter="followFilters.planTime" type="time" :active="followIsActive('planTime')" />
        </template>
        <template #default="{ row }">{{ formatDate(row.planTime) }}</template>
      </el-table-column>
      <el-table-column width="120">
        <template #header>
          <TableFilterHeader label="状态" :filter="followFilters.status" type="enum" :active="followIsActive('status')" :options="followStatusFilterOptions" />
        </template>
        <template #default="{ row }">
          <el-tag :type="followStatusTagType(row.status)" effect="plain">{{ followStatusText(row.status) }}</el-tag>
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
          <TableActionColumnHeader title="操作" :collapsed="followActionCollapsed" @toggle="followActionCollapsed = !followActionCollapsed" />
        </template>
        <template #default="{ row }">
          <div class="table-action-cell">
            <div class="table-action-panel" :class="{ 'is-collapsed': followActionCollapsed }">
              <el-button v-if="followCanApprove(row)" text type="success" :loading="followActionLoadingId === followActionKey(row, 'approve')" @click="followSubmitAction(row, 'approve')">同意</el-button>
              <el-button v-if="followCanReject(row)" text type="danger" :loading="followActionLoadingId === followActionKey(row, 'reject')" @click="followSubmitAction(row, 'reject')">拒绝</el-button>
              <el-button v-if="followCanRevoke(row)" text type="warning" :loading="followActionLoadingId === followActionKey(row, 'revoke')" @click="followSubmitAction(row, 'revoke')">撤销</el-button>
              <el-button v-if="followCanModify(row)" text type="primary" @click="openFollowModifyDialog(row)">修改</el-button>
              <el-button v-if="followCanExecute(row)" text type="success" :loading="followActionLoadingId === followActionKey(row, 'execute')" @click="followSubmitAction(row, 'execute')">执行</el-button>
              <el-button v-if="followCanFinish(row)" text type="success" :loading="followActionLoadingId === followActionKey(row, 'finish')" @click="followSubmitAction(row, 'finish')">完成</el-button>
            </div>
          </div>
        </template>
      </el-table-column>
    </el-table>
    <div class="user-admin-pagination">
      <el-pagination
        layout="prev, pager, next, total"
        :current-page="followPage.page"
        :page-size="followPage.size"
        :total="followTotal"
        @current-change="followChangePage"
      />
    </div>
  </section>

  <!-- Follow modify dialog -->
  <el-dialog v-model="followModifyDialogVisible" title="修改回访任务" width="560px" :close-on-click-modal="false">
    <el-form label-position="top" class="follow-task-modify-form">
      <el-form-item label="志愿者">
        <el-select
          v-model="followModifyForm.volunteerId"
          filterable
          class="full-width-control"
          placeholder="选择志愿者"
          :loading="followVolunteerLoading"
        >
          <el-option
            v-for="item in followVolunteerOptions"
            :key="followVolunteerValue(item)"
            :label="followVolunteerLabel(item)"
            :value="followVolunteerValue(item)"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="计划时间">
        <el-date-picker
          v-model="followModifyForm.planTime"
          type="datetime"
          value-format="YYYY-MM-DDTHH:mm:ss.SSS"
          placeholder="选择计划时间"
          class="full-width-control"
        />
      </el-form-item>
      <el-form-item label="备注">
        <el-input
          v-model="followModifyForm.remark"
          type="textarea"
          :autosize="{ minRows: 3, maxRows: 5 }"
          placeholder="填写调整说明"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="followModifyDialogVisible = false">取消</el-button>
      <el-button class="warm-btn" :loading="followActionLoadingId === 'modify'" @click="submitFollowModify">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Edit, Plus, RefreshRight } from '@element-plus/icons-vue'
import { getFollowTasks, updateFollowTask } from '../../api/adoption'
import { getVolunteerProfiles } from '../../api/volunteer'
import { useTableFilters } from '../../composables/useTableFilters'
import { useUserStore } from '../../stores/user'
import TableFilterHeader from '../TableFilterHeader.vue'
import TableActionColumnHeader from '../TableActionColumnHeader.vue'
import { useRoute, useRouter } from 'vue-router'
import { ROLE, hasRole } from '../../utils/roles'
import { adoptionStatusText, adoptionStatusTagType, formatDate } from '../../utils/format'

const userStore = useUserStore()
const router = useRouter()
const route = useRoute()

const loginRole = computed(() => Number(userStore.profile?.role || 0))
const loginUserId = computed(() => String(userStore.profile?.id || ''))
const isWorker = computed(() => hasRole(loginRole.value, ROLE.WORKER) || hasRole(loginRole.value, ROLE.ADMIN))
const isAdmin = computed(() => hasRole(loginRole.value, ROLE.ADMIN))
const isVolunteerRole = computed(() => hasRole(loginRole.value, ROLE.VOLUNTEER))

const followLoading = ref(false)
const followVolunteerLoading = ref(false)
const followActionCollapsed = ref(false)
const followActionLoadingId = ref('')
const followModifyDialogVisible = ref(false)
const followModifyTarget = ref(null)
const followVolunteerOptions = ref([])
const followRows = ref([])
const followTotal = ref(0)
const followPage = reactive({ page: 1, size: 10 })
const followModifyForm = reactive({ volunteerId: '', planTime: '', remark: '' })

const { filters: followFilters, isActive: followIsActive, applyFilter: followApplyFilter } = useTableFilters({
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

const followFilteredTasks = computed(() => followApplyFilter(followRows.value || []))

function followStatusText(value) {
  const map = { CREATE: '刚创建', NOTIFIED: '已通知', IN_PROGRESS: '执行中', DELAY: '推迟', FINISH: '已完成' }
  return map[value] || value || ''
}

function followStatusTagType(value) {
  if (value === 'FINISH') return 'success'
  if (value === 'DELAY') return 'info'
  if (value === 'IN_PROGRESS' || value === 'NOTIFIED') return 'primary'
  return 'warning'
}

function followIsNotified(row) { return row?.status === 'NOTIFIED' }
function followIsTaskVolunteer(row) { return String(row?.volunteerId || '') === loginUserId.value }
function followIsTaskWorker(row) { return String(row?.workerId || '') === loginUserId.value }
function followIsApplicant(row) { return String(row?.applicantId || '') === loginUserId.value }

function followCanApprove(row) { return followIsNotified(row) && isVolunteerRole.value && followIsTaskVolunteer(row) }
function followCanReject(row) { return followIsNotified(row) && ((isVolunteerRole.value && followIsTaskVolunteer(row)) || followIsApplicant(row)) }
function followCanRevoke(row) { return followIsNotified(row) && (isAdmin.value || (isWorker.value && followIsTaskVolunteer(row))) }
function followCanModify(row) { return followIsNotified(row) && (isAdmin.value || (isWorker.value && followIsTaskWorker(row))) }
function followCanExecute(row) { return followIsNotified(row) && isVolunteerRole.value && followIsTaskVolunteer(row) }
function followCanFinish(row) { return row?.status === 'IN_PROGRESS' && hasRole(loginRole.value, ROLE.WORKER) && followIsTaskWorker(row) }

function followActionKey(row, action) { return `${row?.id || ''}:${action}` }

function followBuildUpdatePayload(row, status, overrides = {}) {
  return {
    workerId: overrides.workerId ?? row.workerId,
    volunteerId: overrides.volunteerId ?? row.volunteerId,
    planTime: overrides.planTime ?? row.planTime,
    status,
    remark: overrides.remark ?? row.remark ?? undefined,
  }
}

async function followSubmitAction(row, action) {
  if (!row?.id || followActionLoadingId.value) return
  const statusMap = { approve: 'IN_PROGRESS', reject: 'DELAY', revoke: 'CREATE', execute: 'IN_PROGRESS', finish: 'FINISH' }
  const messageMap = {
    approve: '确认同意这项回访任务？', reject: '确认拒绝这项回访任务？', revoke: '确认撤销这项回访任务通知？',
    execute: '确认开始执行这项回访任务？', finish: '确认完成这项回访任务？',
  }
  const successMap = { approve: '已同意回访任务', reject: '已拒绝回访任务', revoke: '已撤销回访任务通知', execute: '回访任务已进入执行中', finish: '回访任务已完成' }
  try {
    await ElMessageBox.confirm(messageMap[action], '回访任务', {
      type: action === 'reject' ? 'warning' : 'info', confirmButtonText: '确认', cancelButtonText: '取消',
    })
    followActionLoadingId.value = followActionKey(row, action)
    await updateFollowTask(row.id, followBuildUpdatePayload(row, statusMap[action]))
    ElMessage.success(successMap[action])
    await loadFollowRows()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') ElMessage.warning(error?.message || '操作回访任务失败')
  } finally {
    followActionLoadingId.value = ''
  }
}

function followVolunteerName(item) { return item?.realName || item?.username || '未命名志愿者' }
function followVolunteerLabel(item) { const phone = item?.phone ? ` · ${item.phone}` : ''; return `${followVolunteerName(item)}${phone}` }
function followVolunteerValue(item) { return String(item?.userId || item?.id || '') }

async function loadFollowVolunteerOptions() {
  followVolunteerLoading.value = true
  try {
    const result = await getVolunteerProfiles({ size: 100, status: ['ACTIVE'] })
    followVolunteerOptions.value = Array.isArray(result?.records) ? result.records : []
  } catch (error) {
    followVolunteerOptions.value = []
    ElMessage.warning(error?.message || '加载志愿者列表失败')
  } finally {
    followVolunteerLoading.value = false
  }
}

async function openFollowModifyDialog(row) {
  followModifyTarget.value = row
  followModifyForm.volunteerId = String(row?.volunteerId || '')
  followModifyForm.planTime = row?.planTime || ''
  followModifyForm.remark = row?.remark || ''
  followModifyDialogVisible.value = true
  if (!followVolunteerOptions.value.length) await loadFollowVolunteerOptions()
}

async function submitFollowModify() {
  if (!followModifyTarget.value || followActionLoadingId.value) return
  if (!followModifyForm.volunteerId) { ElMessage.warning('请选择志愿者'); return }
  if (!followModifyForm.planTime) { ElMessage.warning('请选择计划时间'); return }
  followActionLoadingId.value = 'modify'
  try {
    await updateFollowTask(followModifyTarget.value.id, followBuildUpdatePayload(followModifyTarget.value, 'NOTIFIED', {
      volunteerId: followModifyForm.volunteerId, planTime: followModifyForm.planTime, remark: followModifyForm.remark.trim() || undefined,
    }))
    ElMessage.success('回访任务已修改并重新通知')
    followModifyDialogVisible.value = false
    await loadFollowRows()
  } catch (error) {
    ElMessage.warning(error?.message || '修改回访任务失败')
  } finally {
    followActionLoadingId.value = ''
  }
}

async function loadFollowRows() {
  followLoading.value = true
  try {
    const result = await getFollowTasks({ page: followPage.page, size: followPage.size, sort: 'plan_time', order: 'desc' })
    followRows.value = Array.isArray(result?.records) ? result.records : []
    followTotal.value = Number(result?.total || followRows.value.length)
  } catch (error) {
    ElMessage.warning(error?.message || '加载回访任务失败')
  } finally {
    followLoading.value = false
  }
}

function handleFollowRefresh() { followPage.page = 1; loadFollowRows() }
function followChangePage(value) { followPage.page = value; loadFollowRows() }

function goFollowTaskDetail(row) {
  if (!row?.id) return
  router.push({ name: 'console-adoption-follow-task-detail', params: { id: String(row.id) } })
}

onMounted(() => {
  loadFollowRows()
})

defineExpose({ loadData: loadFollowRows, refresh: handleFollowRefresh })
</script>
