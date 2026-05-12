<template>
  <div>
    <el-card class="profile-card pet-admin-card">
      <template #header>
        <div class="profile-card-header"><strong>救助任务</strong><span>处理现场任务和进度</span></div>
      </template>
      <section class="pet-admin-section">
        <section class="filter-panel pet-directory-filter-panel">
          <div class="pet-filter-row pet-filter-row-primary">
            <el-input v-model="taskKeyword" class="filter-field-lg" clearable placeholder="按标题、类型、位置或状态搜索" />
            <div class="pet-filter-action">
              <el-button class="warm-btn" :icon="RefreshRight" :loading="loadingTasks" @click="loadTasks">刷新</el-button>
            </div>
          </div>
        </section>
        <el-table :data="visibleTasks" v-loading="loadingTasks" class="user-admin-table">
          <el-table-column label="任务" min-width="220">
            <template #default="{ row }">
              <div class="rescue-task-admin-title">
                <button class="table-primary-link" type="button" @click="goTaskDetail(row)">{{ row.summary || '未命名任务' }}</button>
                <span>{{ rescueTaskTypeText(row.type) }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="120">
            <template #default="{ row }"><el-tag type="warning" effect="plain">{{ rescueTaskStatusText(row.status) }}</el-tag></template>
          </el-table-column>
          <el-table-column label="位置" min-width="240">
            <template #default="{ row }">{{ rescueTaskLocationText(row) }}</template>
          </el-table-column>
          <el-table-column prop="description" label="描述" min-width="220" show-overflow-tooltip />
          <el-table-column width="40" class-name="action-col">
            <template #header><TableActionColumnHeader title="操作" :collapsed="taskActionCollapsed" @toggle="taskActionCollapsed = !taskActionCollapsed" /></template>
            <template #default="{ row }">
              <div class="table-action-cell">
                <div class="table-action-panel" :class="{ 'is-collapsed': taskActionCollapsed }">
                  <el-button v-if="canEditTask(row)" text type="warning" @click="openTaskEditDialog(row)">编辑</el-button>
                  <el-button v-if="canManageUsers" text type="primary" @click="openTaskReviewDialog(row)">审核</el-button>
                  <el-button v-if="canEditTask(row)" text type="danger" @click="removeTask(row)">删除</el-button>
                </div>
              </div>
            </template>
          </el-table-column>
        </el-table>
        <div class="user-admin-pagination">
          <el-pagination layout="prev, pager, next, total" :current-page="taskPage.page" :page-size="taskPage.size" :total="taskTotal" @current-change="changeTaskPage" />
        </div>
      </section>
    </el-card>

    <el-dialog v-model="taskEditDialogVisible" title="编辑救助任务" width="720px">
      <el-form ref="taskEditFormRef" :model="taskEditForm" :rules="taskEditRules" label-position="top" class="pet-admin-form">
        <el-form-item label="任务简介" prop="summary"><el-input v-model="taskEditForm.summary" maxlength="20" show-word-limit placeholder="请输入任务简介" clearable /></el-form-item>
        <el-form-item label="任务类型" prop="type">
          <el-select v-model="taskEditForm.type" placeholder="请选择任务类型">
            <el-option v-for="item in rescueTaskTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="省份" prop="province">
          <el-select v-model="taskEditForm.province" placeholder="选择省份" filterable clearable @change="handleTaskEditProvinceChange">
            <el-option v-for="item in provinceOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="城市" prop="city">
          <el-select v-model="taskEditForm.city" placeholder="选择城市" filterable clearable :disabled="!taskEditForm.province" @change="handleTaskEditCityChange">
            <el-option v-for="item in taskEditCityOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="区县" prop="district">
          <el-select v-model="taskEditForm.district" placeholder="选择区县" filterable clearable :disabled="!taskEditForm.city">
            <el-option v-for="item in taskEditDistrictOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="详细位置" prop="detailAddress"><el-input v-model="taskEditForm.detailAddress" placeholder="请输入救助位置" clearable /></el-form-item>
        <el-form-item label="情况描述" prop="description" class="pet-admin-span-2"><el-input v-model="taskEditForm.description" type="textarea" :autosize="{ minRows: 4, maxRows: 7 }" placeholder="描述现场情况、动物状态和风险" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="taskEditDialogVisible = false">取消</el-button>
        <el-button type="warning" :loading="savingTask" @click="saveTaskInfo">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="taskReviewDialogVisible" title="审核救助任务" width="520px">
      <el-form ref="taskReviewFormRef" :model="taskReviewForm" :rules="taskReviewRules" label-position="top" class="pet-review-form">
        <el-form-item label="审核/状态" prop="status">
          <el-select v-model="taskReviewForm.status" placeholder="请选择审核状态">
            <el-option v-for="item in rescueTaskStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态原因" prop="reason">
          <el-input v-model="taskReviewForm.reason" type="textarea" :autosize="{ minRows: 4, maxRows: 7 }" placeholder="例如：资料审核通过，安排志愿者跟进" />
        </el-form-item>
      </el-form>
      <div class="dialog-footer">
        <el-button @click="taskReviewDialogVisible = false">取消</el-button>
        <el-button type="warning" :loading="savingTask" @click="saveTaskReview">保存审核</el-button>
      </div>
      <AuditRecordList :records="taskAuditRecords" :loading="taskAuditLoading" type="task" :target-id="taskReviewForm.id" :status-labels="taskStatusLabelMap" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ElMessage, ElMessageBox } from 'element-plus'
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { RefreshRight } from '@element-plus/icons-vue'
import { deleteRescueTask, getRescueTasks, updateRescueTask, updateRescueTaskStatus, getRescueTaskRecords } from '../api/services'
import { useInformationCatalog } from '../composables/useInformationCatalog'
import { useConsoleGuards } from '../composables/useConsoleGuards'
import { useUserStore } from '../stores/user'
import { rescueTaskTypeOptions, rescueTaskStatusOptions, rescueTaskStatusText, rescueTaskTypeText, rescueTaskLocationText, taskStatusLabelMap } from '../utils/roles'
import TableActionColumnHeader from './TableActionColumnHeader.vue'
import AuditRecordList from './AuditRecordList.vue'

const userStore = useUserStore()
const router = useRouter()
const { canManageUsers } = useConsoleGuards()
const { ensureInformationCatalog, ensureCityOptions, ensureDistrictOptions, provinceOptions, getCityOptions, getDistrictOptions } = useInformationCatalog()

// --- State ---
const taskEditFormRef = ref()
const taskReviewFormRef = ref()
const loadingTasks = ref(false)
const savingTask = ref(false)
const taskActionCollapsed = ref(false)
const taskEditDialogVisible = ref(false)
const taskReviewDialogVisible = ref(false)
const taskRows = ref([])
const taskTotal = ref(0)
const taskPage = reactive({ page: 1, size: 10 })
const taskAuditRecords = ref([])
const taskAuditLoading = ref(false)
const taskKeyword = ref('')

const taskEditForm = reactive({ id: '', summary: '', description: '', type: 'FIND', province: '', city: '', district: '', detailAddress: '' })
const taskReviewForm = reactive({ id: '', status: '', reason: '' })

const taskEditCityOptions = computed(() => getCityOptions(taskEditForm.province))
const taskEditDistrictOptions = computed(() => getDistrictOptions(taskEditForm.province, taskEditForm.city))
const currentUserId = computed(() => String(userStore.profile?.id || ''))
const visibleTasks = computed(() => {
  const text = taskKeyword.value.trim().toLowerCase()
  if (!text) return taskRows.value
  return taskRows.value.filter((item) =>
    [item.summary, item.type, item.status, rescueTaskLocationText(item)].filter(Boolean).some((s) => String(s).toLowerCase().includes(text))
  )
})

// --- Validation rules ---
const taskEditRules = {
  summary: [{ required: true, message: '请输入任务简介', trigger: 'blur' }],
  description: [{ required: true, message: '请输入任务描述', trigger: 'blur' }],
  type: [{ required: true, message: '请选择任务类型', trigger: 'change' }],
  province: [{ required: true, message: '请选择省份', trigger: 'change' }],
  city: [{ required: true, message: '请选择城市', trigger: 'change' }],
  district: [{ required: true, message: '请选择区县', trigger: 'change' }],
  detailAddress: [{ required: true, message: '请输入详细位置', trigger: 'blur' }],
}
const taskReviewRules = {
  status: [{ required: true, message: '请选择审核状态', trigger: 'change' }],
  reason: [{ required: true, message: '请输入审核原因', trigger: 'blur' }],
}

// --- Functions ---
function handleTaskEditProvinceChange() { taskEditForm.city = ''; taskEditForm.district = ''; if (taskEditForm.province) ensureCityOptions(taskEditForm.province) }
function handleTaskEditCityChange() { taskEditForm.district = ''; if (taskEditForm.province && taskEditForm.city) ensureDistrictOptions(taskEditForm.province, taskEditForm.city) }

function isOwnTask(row) {
  return currentUserId.value && String(row?.userId || '') === currentUserId.value
}

function canEditTask(row) {
  return canManageUsers.value || (isOwnTask(row) && String(row?.status || '') === 'CREATED')
}

function goTaskDetail(row) {
  if (row?.id) router.push(`/tasks/${row.id}`)
}

async function loadTasks() {
  if (!canManageUsers.value && !currentUserId.value) return
  loadingTasks.value = true
  try {
    const result = await getRescueTasks({
      page: taskPage.page,
      size: taskPage.size,
      sort: 'update_time',
      order: 'desc',
      user: canManageUsers.value ? undefined : [currentUserId.value],
      keyword: taskKeyword.value.trim() || undefined,
    })
    taskRows.value = Array.isArray(result?.records) ? result.records : []
    taskTotal.value = Number(result?.total || taskRows.value.length)
  } catch (error) { ElMessage.warning(error?.message || '加载救助任务失败') }
  finally { loadingTasks.value = false }
}

async function openTaskEditDialog(row) {
  const location = row.location || {}
  Object.assign(taskEditForm, {
    id: String(row.id || ''), summary: row.summary || '', description: row.description || '', type: row.type || 'FIND',
    province: location.province || '', city: location.city || '', district: location.district || '', detailAddress: location.detailAddress || '',
  })
  if (taskEditForm.province) await ensureCityOptions(taskEditForm.province)
  if (taskEditForm.province && taskEditForm.city) await ensureDistrictOptions(taskEditForm.province, taskEditForm.city)
  taskEditDialogVisible.value = true
}

function openTaskReviewDialog(row) {
  Object.assign(taskReviewForm, { id: String(row.id || ''), status: row.status === 'CREATED' ? 'APPROVED' : row.status || 'APPROVED', reason: '' })
  taskReviewDialogVisible.value = true
  loadTaskAuditRecords(row.id)
}

async function loadTaskAuditRecords(taskId) {
  taskAuditLoading.value = true; taskAuditRecords.value = []
  try { const res = await getRescueTaskRecords(taskId); taskAuditRecords.value = Array.isArray(res) ? res : res?.records || res?.data || [] }
  catch { /* ignore */ } finally { taskAuditLoading.value = false }
}

async function saveTaskInfo() {
  if (!taskEditFormRef.value || savingTask.value) return
  savingTask.value = true
  try {
    await taskEditFormRef.value.validate()
    await updateRescueTask(taskEditForm.id, {
      summary: taskEditForm.summary.trim(), description: taskEditForm.description.trim(), type: taskEditForm.type,
      province: taskEditForm.province, city: taskEditForm.city, district: taskEditForm.district, detailAddress: taskEditForm.detailAddress.trim(),
    })
    ElMessage.success('救助任务已保存'); taskEditDialogVisible.value = false; await loadTasks()
  } catch (error) { ElMessage.warning(error?.message || '保存救助任务失败') }
  finally { savingTask.value = false }
}

async function saveTaskReview() {
  if (!taskReviewFormRef.value || savingTask.value) return
  savingTask.value = true
  try {
    await taskReviewFormRef.value.validate()
    await updateRescueTaskStatus(taskReviewForm.id, { status: taskReviewForm.status, reason: taskReviewForm.reason.trim() })
    ElMessage.success('救助任务审核已保存'); taskReviewDialogVisible.value = false; await loadTasks()
  } catch (error) { ElMessage.warning(error?.message || '保存审核状态失败') }
  finally { savingTask.value = false }
}

async function removeTask(row) {
  try {
    await ElMessageBox.confirm(`确认删除「${row.summary || row.id}」救助任务？`, '删除救助任务', { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' })
    await deleteRescueTask(row.id); ElMessage.success('救助任务已删除'); await loadTasks()
  } catch (error) { if (error !== 'cancel' && error !== 'close') ElMessage.warning(error?.message || '删除救助任务失败') }
}

function changeTaskPage(page) { taskPage.page = page; loadTasks() }

onMounted(async () => { await ensureInformationCatalog(); loadTasks() })
</script>
