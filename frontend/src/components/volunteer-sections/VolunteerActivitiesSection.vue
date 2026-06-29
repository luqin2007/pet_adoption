<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, RefreshRight } from '@element-plus/icons-vue'
import { createVolunteerServiceRecord, createVolunteerShift, getVolunteerProfiles, getVolunteerServiceRecords, getVolunteerShifts, updateVolunteerServiceRecordStatus, updateVolunteerShift, updateVolunteerShiftStatus } from '../../api/volunteer'
import { useInformationCatalog } from '../../composables/useInformationCatalog'
import { useTableFilters } from '../../composables/useTableFilters'
import { useUserStore } from '../../stores/user'
import TableFilterHeader from '../TableFilterHeader.vue'
import TableActionColumnHeader from '../TableActionColumnHeader.vue'
import AuditRecordList from '../AuditRecordList.vue'
import { useRoute, useRouter } from 'vue-router'
import { ROLE, hasRole } from '../../utils/roles'

const router = useRouter()
const route = useRoute()

const props = defineProps({
  section: { type: String, default: 'activities' },
  hideTabs: { type: Boolean, default: false },
  allowedSections: { type: Array, default: null },
})

const emit = defineEmits(['update:section'])

const userStore = useUserStore()

const loginRole = computed(() => Number(userStore.profile.role || 0))
const isAdmin = computed(() => hasRole(loginRole.value, ROLE.ADMIN))
const isWorker = computed(() => isAdmin.value || hasRole(loginRole.value, ROLE.WORKER))
const isVolunteer = computed(() => hasRole(loginRole.value, ROLE.VOLUNTEER))
const loginUserId = computed(() => String(userStore.profile.id || ''))
const activityVolunteerId = computed(() => String(route.query.volunteer || ''))
const activityShiftId = computed(() => String(route.query.shift || ''))

function formatDate(value, withTime = false) {
  if (!value) return '待补充'
  const text = String(value)
  return withTime ? text.slice(0, 16).replace('T', ' ') : text.slice(0, 10)
}

function toApiDateTime(value) {
  if (!value) return undefined
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? value : date.toISOString()
}

function locationText(row) {
  return [row.province, row.city, row.district, row.serviceAddress || row.address || row.detailAddress].filter(Boolean).join(' · ') || '位置待补充'
}

function tagTypeByStatus(value) {
  if (['APPROVED', 'ACTIVE', 'ISSUED', 'COMPLETED'].includes(value)) return 'success'
  if (['UNDER_REVIEW', 'CONFIRMED', 'IN_PROGRESS'].includes(value)) return 'primary'
  if (['REJECTED', 'CANCELED', 'DISABLED', 'CANCELLED', 'ABSENT'].includes(value)) return 'info'
  return 'warning'
}

function shiftStatusText(value) {
  return shiftStatusOptions.find((item) => item.value === value)?.label || value || '待补充'
}

function taskTypeText(value) {
  return taskTypeOptions.find((item) => item.value === value)?.label || value || '待补充'
}

function recordStatusText(value) {
  return recordStatusOptions.find((item) => item.value === value)?.label || value || '待补充'
}

const shiftStatusOptions = [
  { label: '已分配', value: 'ASSIGNED' },
  { label: '已确认', value: 'CONFIRMED' },
  { label: '执行中', value: 'IN_PROGRESS' },
  { label: '已完成', value: 'COMPLETED' },
  { label: '已取消', value: 'CANCELLED' },
  { label: '缺勤', value: 'ABSENT' },
]

const shiftStatusLabelMap = Object.fromEntries(shiftStatusOptions.map(o => [o.value, o.label]))

const taskTypeOptions = [
  { label: '救助任务', value: 'RESCUE' },
  { label: '日常值班', value: 'DAILY_DUTY' },
  { label: '回访任务', value: 'FOLLOW_VISIT' },
  { label: '医疗协助', value: 'MEDICAL_SUPPORT' },
  { label: '物资运输', value: 'TRANSPORT' },
  { label: '活动支持', value: 'EVENT' },
  { label: '其他任务', value: 'OTHER' },
]

const recordStatusOptions = [
  { label: '草稿', value: 'DRAFT' },
  { label: '已提交', value: 'SUBMITTED' },
  { label: '已通过', value: 'APPROVED' },
  { label: '已驳回', value: 'REJECTED' },
]

const shiftLoading = ref(false)
const shiftActionCollapsed = ref(false)
const shiftRows = ref([])
const shiftTotal = ref(0)
const shiftPage = reactive({ page: 1, size: 10 })
const shiftSearch = reactive({ status: '', taskType: '', timeRange: [] })

const { filters: shiftFilters, isActive: isShiftFilterActive, applyFilter: applyShiftFilter } = useTableFilters({
  title: { type: 'text' },
  volunteer: { type: 'text' },
  taskType: { type: 'enum' },
  time: { type: 'time' },
  status: { type: 'enum' },
  location: { type: 'text' },
})

const filteredShifts = computed(() => applyShiftFilter(shiftRows.value || []))

const recordLoading = ref(false)
const recordActionCollapsed = ref(false)
const recordRows = ref([])
const recordTotal = ref(0)
const recordPage = reactive({ page: 1, size: 10 })
const recordSearch = reactive({ status: '', timeRange: [] })

const { filters: recordFilters, isActive: isRecordFilterActive, applyFilter: applyRecordFilter } = useTableFilters({
  title: { type: 'text' },
  volunteer: { type: 'text' },
  summary: { type: 'text' },
  status: { type: 'enum' },
  review: { type: 'text' },
})

const filteredRecords = computed(() => applyRecordFilter(recordRows.value || []))

const volunteerOptions = ref([])
const volunteerOptionsLoading = ref(false)

const shiftDialogVisible = ref(false)
const shiftStatusDialogVisible = ref(false)
const shiftAuditRecords = ref([])
const recordDialogVisible = ref(false)
const recordReviewDialogVisible = ref(false)

const shiftFormRef = ref()
const shiftStatusFormRef = ref()
const recordFormRef = ref()
const recordReviewFormRef = ref()

const savingShift = ref(false)
const savingShiftStatus = ref(false)
const savingRecord = ref(false)
const savingRecordReview = ref(false)

const shiftForm = reactive({
  id: '', volunteerId: '', taskType: 'EVENT', taskId: '', title: '', content: '',
  province: '', city: '', district: '', detailAddress: '',
  shiftTimeRange: [], taskTimeRange: [], remark: '',
})

const shiftStatusForm = reactive({
  id: '', status: '', currentStatus: '', reason: '',
})

const recordForm = reactive({
  shiftId: '', startTime: '', endTime: '', actualHours: undefined,
  summary: '', content: '', problem: '', suggestion: '',
})

const recordReviewForm = reactive({
  id: '', status: '', reason: '',
})

const shiftRules = {
  volunteerId: [{ required: true, message: '请选择志愿者', trigger: 'change' }],
  taskType: [{ required: true, message: '请选择任务类型', trigger: 'change' }],
  title: [{ required: true, message: '请输入任务标题', trigger: 'blur' }],
  province: [{ required: true, message: '请选择省份', trigger: 'change' }],
  city: [{ required: true, message: '请选择城市', trigger: 'change' }],
  district: [{ required: true, message: '请选择区县', trigger: 'change' }],
  detailAddress: [{ required: true, message: '请输入详细地点', trigger: 'blur' }],
  shiftTimeRange: [{ required: true, message: '请选择排班时间', trigger: 'change' }],
  taskTimeRange: [{ required: true, message: '请选择任务时间', trigger: 'change' }],
}

const shiftStatusRules = {
  status: [{ required: true, message: '请选择排班状态', trigger: 'change' }],
  reason: [{ required: true, message: '请填写原因说明', trigger: 'blur' }],
}

const recordRules = {
  startTime: [{ required: true, message: '请选择服务开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择服务结束时间', trigger: 'change' }],
  summary: [{ required: true, message: '请输入服务摘要', trigger: 'blur' }],
}

const recordReviewRules = {
  status: [{ required: true, message: '请选择审核结果', trigger: 'change' }],
  reason: [{ required: true, message: '请填写审核意见', trigger: 'blur' }],
}

const {
  ensureInformationCatalog, ensureCityOptions, ensureDistrictOptions,
  provinceOptions, getCityOptions, getDistrictOptions,
} = useInformationCatalog()

const shiftFormCityOptions = computed(() => getCityOptions(shiftForm.province))
const shiftFormDistrictOptions = computed(() => getDistrictOptions(shiftForm.province, shiftForm.city))

function handleShiftFormProvinceChange() {
  shiftForm.city = ''
  shiftForm.district = ''
  if (shiftForm.province) {
    ensureCityOptions(shiftForm.province)
  }
}

function handleShiftFormCityChange() {
  shiftForm.district = ''
  if (shiftForm.province && shiftForm.city) {
    ensureDistrictOptions(shiftForm.province, shiftForm.city)
  }
}

function buildShiftQuery() {
  const [time0, time1] = shiftSearch.timeRange || []
  return {
    page: shiftPage.page, size: shiftPage.size,
    volunteer: activityVolunteerId.value || undefined,
    status: shiftSearch.status ? [shiftSearch.status] : undefined,
    taskType: shiftSearch.taskType ? [shiftSearch.taskType] : undefined,
    time0: toApiDateTime(time0), time1: toApiDateTime(time1),
  }
}

async function loadShifts() {
  shiftLoading.value = true
  try {
    const result = await getVolunteerShifts(buildShiftQuery())
    shiftRows.value = Array.isArray(result?.records) ? result.records : []
    shiftTotal.value = Number(result?.total || 0)
  } catch (error) {
    ElMessage.warning(error?.message || '加载志愿活动排班失败')
  } finally {
    shiftLoading.value = false
  }
}

function searchShifts() {
  shiftPage.page = 1
  loadShifts()
}

function changeShiftPage(page) {
  shiftPage.page = page
  loadShifts()
}

function resetShiftForm() {
  shiftForm.id = ''
  shiftForm.volunteerId = ''
  shiftForm.taskType = 'EVENT'
  shiftForm.taskId = ''
  shiftForm.title = ''
  shiftForm.content = ''
  shiftForm.province = ''
  shiftForm.city = ''
  shiftForm.district = ''
  shiftForm.detailAddress = ''
  shiftForm.shiftTimeRange = []
  shiftForm.taskTimeRange = []
  shiftForm.remark = ''
}

function openShiftDialog(row = null) {
  if (row) {
    if (!canEditShift(row)) {
      ElMessage.info('执行中或已完成的排班不能编辑')
      return
    }
    shiftForm.id = row.id
    shiftForm.volunteerId = row.volunteerId || ''
    shiftForm.taskType = row.taskType || 'EVENT'
    shiftForm.taskId = row.taskSourceId || ''
    shiftForm.title = row.title || ''
    shiftForm.content = row.content || ''
    shiftForm.province = row.province || ''
    shiftForm.city = row.city || ''
    shiftForm.district = row.district || ''
    shiftForm.detailAddress = row.serviceAddress || ''
    shiftForm.shiftTimeRange = [row.startTime || '', row.endTime || '']
    shiftForm.taskTimeRange = [row.startTime || '', row.endTime || '']
    shiftForm.remark = row.remark || ''
  } else {
    resetShiftForm()
  }
  shiftDialogVisible.value = true
  loadVolunteerOptions()
}

async function saveShift() {
  if (!shiftFormRef.value) return
  try { await shiftFormRef.value.validate() } catch { return }
  savingShift.value = true
  try {
    if (shiftForm.id) {
      await updateVolunteerShift(shiftForm.id, {
        volunteerId: shiftForm.volunteerId,
        province: shiftForm.province, city: shiftForm.city, district: shiftForm.district,
        detailAddress: shiftForm.detailAddress.trim(),
        startTime: toApiDateTime(shiftForm.shiftTimeRange?.[0]),
        endTime: toApiDateTime(shiftForm.shiftTimeRange?.[1]),
      })
      ElMessage.success('排班已更新')
    } else {
      await createVolunteerShift({
        volunteerId: shiftForm.volunteerId, taskType: shiftForm.taskType,
        taskId: shiftForm.taskId || undefined, title: shiftForm.title.trim(),
        content: shiftForm.content.trim() || undefined,
        province: shiftForm.province, city: shiftForm.city, district: shiftForm.district,
        detailAddress: shiftForm.detailAddress.trim(),
        startTime: toApiDateTime(shiftForm.shiftTimeRange?.[0]),
        endTime: toApiDateTime(shiftForm.shiftTimeRange?.[1]),
        taskStartTime: toApiDateTime(shiftForm.taskTimeRange?.[0]),
        taskEndTime: toApiDateTime(shiftForm.taskTimeRange?.[1]),
        remark: shiftForm.remark.trim() || undefined,
      })
      ElMessage.success('排班已创建')
    }
    shiftDialogVisible.value = false
    loadShifts()
  } catch (error) {
    ElMessage.warning(error?.message || '保存排班失败')
  } finally {
    savingShift.value = false
  }
}

function openShiftStatusDialog(row) {
  shiftStatusForm.id = row.id
  shiftStatusForm.currentStatus = row.status || ''
  shiftStatusForm.status = availableShiftStatusOptions(row.status)[0]?.value || ''
  shiftStatusForm.reason = ''
  shiftAuditRecords.value = Array.isArray(row.statusRecords) ? row.statusRecords : []
  shiftStatusDialogVisible.value = true
}

function availableShiftStatusOptions(status = shiftStatusForm.currentStatus) {
  const allowedMap = {
    ASSIGNED: ['CANCELLED'],
    CONFIRMED: ['IN_PROGRESS', 'COMPLETED', 'CANCELLED', 'ABSENT'],
    IN_PROGRESS: ['COMPLETED', 'CANCELLED'],
  }
  const allowed = allowedMap[status] || []
  return shiftStatusOptions.filter((item) => allowed.includes(item.value))
}

async function saveShiftStatus() {
  if (!shiftStatusFormRef.value) return
  try { await shiftStatusFormRef.value.validate() } catch { return }
  savingShiftStatus.value = true
  try {
    await updateVolunteerShiftStatus(shiftStatusForm.id, {
      status: shiftStatusForm.status,
      reason: shiftStatusForm.reason.trim(),
    })
    ElMessage.success('排班状态已更新')
    shiftStatusDialogVisible.value = false
    loadShifts()
    loadRecords()
  } catch (error) {
    ElMessage.warning(error?.message || '更新排班状态失败')
  } finally {
    savingShiftStatus.value = false
  }
}

async function confirmShift(row) {
  try {
    await updateVolunteerShiftStatus(row.id, { status: 'CONFIRMED', reason: String(Date.now()) })
    ElMessage.success('已确认本次排班')
    loadShifts()
  } catch (error) {
    ElMessage.warning(error?.message || '确认排班失败')
  }
}

async function rejectShift(row) {
  try {
    const { value } = await ElMessageBox.prompt('请输入拒绝原因', '拒绝排班', {
      type: 'warning', inputType: 'textarea',
      inputPlaceholder: '请说明无法参与本次排班的原因',
      inputValidator: (value) => Boolean(String(value || '').trim()) || '请输入原因',
      confirmButtonText: '提交', cancelButtonText: '取消',
    })
    await updateVolunteerShiftStatus(row.id, {
      status: 'CANCELLED',
      reason: String(value || '').trim(),
    })
    ElMessage.success('已拒绝本次排班')
    loadShifts()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.warning(error?.message || '拒绝排班失败')
    }
  }
}

function buildRecordQuery() {
  const [time0, time1] = recordSearch.timeRange || []
  return {
    page: recordPage.page, size: recordPage.size,
    volunteer: activityVolunteerId.value || undefined,
    shift: activityShiftId.value || undefined,
    status: recordSearch.status ? [recordSearch.status] : undefined,
    time0: toApiDateTime(time0), time1: toApiDateTime(time1),
  }
}

async function loadRecords() {
  recordLoading.value = true
  try {
    const result = await getVolunteerServiceRecords(buildRecordQuery())
    recordRows.value = Array.isArray(result?.records) ? result.records : []
    recordTotal.value = Number(result?.total || 0)
  } catch (error) {
    ElMessage.warning(error?.message || '加载活动报告失败')
  } finally {
    recordLoading.value = false
  }
}

function searchRecords() {
  recordPage.page = 1
  loadRecords()
}

function changeRecordPage(page) {
  recordPage.page = page
  loadRecords()
}

function openRecordDialog(row) {
  recordForm.shiftId = row.id
  recordForm.startTime = row.startTime || ''
  recordForm.endTime = row.endTime || ''
  recordForm.actualHours = undefined
  recordForm.summary = ''
  recordForm.content = ''
  recordForm.problem = ''
  recordForm.suggestion = ''
  recordDialogVisible.value = true
}

async function saveRecord() {
  if (!recordFormRef.value) return
  try { await recordFormRef.value.validate() } catch { return }
  savingRecord.value = true
  try {
    await createVolunteerServiceRecord(recordForm.shiftId, {
      startTime: toApiDateTime(recordForm.startTime),
      endTime: toApiDateTime(recordForm.endTime),
      actualHours: recordForm.actualHours,
      summary: recordForm.summary.trim(),
      content: recordForm.content.trim() || undefined,
      problem: recordForm.problem.trim() || undefined,
      suggestion: recordForm.suggestion.trim() || undefined,
    })
    ElMessage.success('活动报告已提交')
    recordDialogVisible.value = false
    loadRecords()
    loadShifts()
  } catch (error) {
    ElMessage.warning(error?.message || '提交活动报告失败')
  } finally {
    savingRecord.value = false
  }
}

function openRecordReviewDialog(row) {
  recordReviewForm.id = row.id
  recordReviewForm.status = row.status || ''
  recordReviewForm.reason = row.reviewComment || ''
  recordReviewDialogVisible.value = true
}

async function saveRecordReview() {
  if (!recordReviewFormRef.value) return
  try { await recordReviewFormRef.value.validate() } catch { return }
  savingRecordReview.value = true
  try {
    await updateVolunteerServiceRecordStatus(recordReviewForm.id, {
      status: recordReviewForm.status,
      reason: recordReviewForm.reason.trim(),
    })
    ElMessage.success('活动报告审核已更新')
    recordReviewDialogVisible.value = false
    loadRecords()
  } catch (error) {
    ElMessage.warning(error?.message || '更新活动报告失败')
  } finally {
    savingRecordReview.value = false
  }
}

async function loadVolunteerOptions() {
  if (!isWorker.value) return
  volunteerOptionsLoading.value = true
  try {
    const result = await getVolunteerProfiles({ size: 100, status: ['ACTIVE'] })
    volunteerOptions.value = Array.isArray(result?.records) ? result.records : []
  } catch (error) {
    ElMessage.warning(error?.message || '加载志愿者列表失败')
  } finally {
    volunteerOptionsLoading.value = false
  }
}

function canWriteRecord(row) {
  return isVolunteer.value && String(row.volunteerId || '') === loginUserId.value && row.status === 'COMPLETED' && !row.recordId
}

function canConfirmShift(row) {
  return isVolunteer.value && String(row.volunteerId || '') === loginUserId.value && row.status === 'ASSIGNED'
}

function canRejectShift(row) {
  return isVolunteer.value && String(row.volunteerId || '') === loginUserId.value && row.status === 'ASSIGNED'
}

function canEditShift(row) {
  return isWorker.value && !['IN_PROGRESS', 'COMPLETED'].includes(row?.status)
}

function canManageShiftStatus(row) {
  return isWorker.value && availableShiftStatusOptions(row?.status).length > 0
}

function goShiftPage(row) {
  if (row?.id) {
    router.push({ name: 'console-volunteer-activity-detail', params: { id: String(row.id) } })
  }
}

function goRecordShift(row) {
  if (row?.shiftId) {
    router.push({ name: 'console-volunteer-activity-detail', params: { id: String(row.shiftId) } })
  }
}

function handlePlusClick() {
  if (isWorker.value) {
    openShiftDialog()
  } else {
    openRecordDialog()
  }
}

const currentSectionLoading = shiftLoading

function searchCurrentSection() {
  Promise.all([loadShifts(), loadRecords()])
}

onMounted(async () => {
  await ensureInformationCatalog()
  await loadVolunteerOptions()
  await Promise.all([loadShifts(), loadRecords()])
})

defineExpose({ handlePlusClick, searchCurrentSection })
</script>

<template>
  <section class="volunteer-activity-stack">
    <div class="volunteer-activity-block">
      <div class="volunteer-block-head">
        <strong>志愿活动排班</strong>
        <span>排班安排与确认</span>
      </div>
      <el-table :data="filteredShifts" v-loading="shiftLoading" class="user-admin-table">
        <el-table-column min-width="140" show-overflow-tooltip>
          <template #header>
            <TableFilterHeader label="活动标题" :filter="shiftFilters.title" type="text" :active="isShiftFilterActive('title')" />
          </template>
          <template #default="{ row }">
            <button class="table-primary-link" type="button" @click="goShiftPage(row)">{{ row.title || '未命名活动' }}</button>
          </template>
        </el-table-column>
        <el-table-column min-width="100">
          <template #header>
            <TableFilterHeader label="志愿者" :filter="shiftFilters.volunteer" type="text" :active="isShiftFilterActive('volunteer')" />
          </template>
          <template #default="{ row }">{{ row.volunteerName || '待指派' }}</template>
        </el-table-column>
        <el-table-column width="100">
          <template #header>
            <TableFilterHeader label="类型" :filter="shiftFilters.taskType" type="enum" :options="taskTypeOptions" :active="isShiftFilterActive('taskType')" />
          </template>
          <template #default="{ row }">{{ taskTypeText(row.taskType) }}</template>
        </el-table-column>
        <el-table-column min-width="150">
          <template #header>
            <TableFilterHeader label="时间" :filter="shiftFilters.time" type="time" :active="isShiftFilterActive('time')" />
          </template>
          <template #default="{ row }">{{ formatDate(row.startTime, true) }} - {{ formatDate(row.endTime, true) }}</template>
        </el-table-column>
        <el-table-column width="100">
          <template #header>
            <TableFilterHeader label="状态" :filter="shiftFilters.status" type="enum" :options="shiftStatusOptions" :active="isShiftFilterActive('status')" />
          </template>
          <template #default="{ row }">
            <el-tag :type="tagTypeByStatus(row.status)" effect="plain">{{ shiftStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column min-width="160" show-overflow-tooltip>
          <template #header>
            <TableFilterHeader label="地点" :filter="shiftFilters.location" type="text" :active="isShiftFilterActive('location')" />
          </template>
          <template #default="{ row }">{{ locationText(row) }}</template>
        </el-table-column>
        <el-table-column width="40" class-name="action-col">
          <template #header>
            <TableActionColumnHeader title="操作" :collapsed="shiftActionCollapsed" @toggle="shiftActionCollapsed = !shiftActionCollapsed" />
          </template>
          <template #default="{ row }">
            <div class="table-action-cell">
              <div class="table-action-panel" :class="{ 'is-collapsed': shiftActionCollapsed }">
                <el-button v-if="canEditShift(row)" text type="warning" @click="openShiftDialog(row)">编辑</el-button>
                <el-button v-if="canManageShiftStatus(row)" text type="primary" @click="openShiftStatusDialog(row)">状态</el-button>
                <el-button v-if="canConfirmShift(row)" text type="success" @click="confirmShift(row)">确认</el-button>
                <el-button v-if="canRejectShift(row)" text type="danger" @click="rejectShift(row)">拒绝</el-button>
                <el-button v-if="canWriteRecord(row)" text type="primary" @click="openRecordDialog(row)">写报告</el-button>
              </div>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <div class="user-admin-pagination">
        <el-pagination layout="prev, pager, next, total" :current-page="shiftPage.page" :page-size="shiftPage.size" :total="shiftTotal" @current-change="changeShiftPage" />
      </div>
    </div>

    <div class="volunteer-activity-block">
      <div class="volunteer-block-head">
        <strong>活动报告</strong>
        <span>活动报告与审核</span>
      </div>
      <el-table :data="filteredRecords" v-loading="recordLoading" class="user-admin-table">
        <el-table-column min-width="140" show-overflow-tooltip>
          <template #header>
            <TableFilterHeader label="排班标题" :filter="recordFilters.title" type="text" :active="isRecordFilterActive('title')" />
          </template>
          <template #default="{ row }">
            <button class="table-primary-link" type="button" @click="goRecordShift(row)">{{ row.shiftTitle || '未命名排班' }}</button>
          </template>
        </el-table-column>
        <el-table-column min-width="100">
          <template #header>
            <TableFilterHeader label="志愿者" :filter="recordFilters.volunteer" type="text" :active="isRecordFilterActive('volunteer')" />
          </template>
          <template #default="{ row }">{{ row.volunteerName || '未命名' }}</template>
        </el-table-column>
        <el-table-column prop="summary" min-width="160" show-overflow-tooltip>
          <template #header>
            <TableFilterHeader label="服务摘要" :filter="recordFilters.summary" type="text" :active="isRecordFilterActive('summary')" />
          </template>
        </el-table-column>
        <el-table-column width="100">
          <template #header>
            <TableFilterHeader label="状态" :filter="recordFilters.status" type="enum" :options="recordStatusOptions" :active="isRecordFilterActive('status')" />
          </template>
          <template #default="{ row }">
            <el-tag :type="tagTypeByStatus(row.status)" effect="plain">{{ recordStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column min-width="140" show-overflow-tooltip>
          <template #header>
            <TableFilterHeader label="审核意见" :filter="recordFilters.review" type="text" :active="isRecordFilterActive('review')" />
          </template>
          <template #default="{ row }">{{ row.reviewComment || '暂无' }}</template>
        </el-table-column>
        <el-table-column width="40" class-name="action-col">
          <template #header>
            <TableActionColumnHeader title="操作" :collapsed="recordActionCollapsed" @toggle="recordActionCollapsed = !recordActionCollapsed" />
          </template>
          <template #default="{ row }">
            <div class="table-action-cell">
              <div class="table-action-panel" :class="{ 'is-collapsed': recordActionCollapsed }">
                <el-button v-if="isWorker && row.status === 'SUBMITTED'" text type="primary" @click="openRecordReviewDialog(row)">审核</el-button>
                <span v-else class="volunteer-static-action table-action-cell-text">查看</span>
              </div>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <div class="user-admin-pagination">
        <el-pagination layout="prev, pager, next, total" :current-page="recordPage.page" :page-size="recordPage.size" :total="recordTotal" @current-change="changeRecordPage" />
      </div>
    </div>
  </section>

  <el-dialog v-model="shiftDialogVisible" :title="shiftForm.id ? '编辑志愿排班' : '安排志愿排班'" width="760px">
    <el-form ref="shiftFormRef" :model="shiftForm" :rules="shiftRules" label-position="top" class="pet-admin-form">
      <el-form-item label="志愿者" prop="volunteerId">
        <el-select v-model="shiftForm.volunteerId" placeholder="选择志愿者" filterable :loading="volunteerOptionsLoading">
          <el-option v-for="item in volunteerOptions" :key="item.userId" :label="`${item.realName || item.username} · ${item.phone || '无电话'}`" :value="item.userId" />
        </el-select>
      </el-form-item>
      <el-form-item label="任务类型" prop="taskType">
        <el-select v-model="shiftForm.taskType" placeholder="选择任务类型">
          <el-option v-for="item in taskTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="任务标题" prop="title" class="pet-admin-span-2">
        <el-input v-model="shiftForm.title" :disabled="Boolean(shiftForm.id)" />
      </el-form-item>
      <el-form-item label="排班时间" prop="shiftTimeRange">
        <el-date-picker
          v-model="shiftForm.shiftTimeRange"
          type="datetimerange"
          value-format="YYYY-MM-DD HH:mm:ss"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
          class="full-width-control"
        />
      </el-form-item>
      <el-form-item label="任务时间" prop="taskTimeRange">
        <el-date-picker
          v-model="shiftForm.taskTimeRange"
          type="datetimerange"
          value-format="YYYY-MM-DD HH:mm:ss"
          start-placeholder="任务开始"
          end-placeholder="任务结束"
          class="full-width-control"
          :disabled="Boolean(shiftForm.id)"
        />
      </el-form-item>
      <el-form-item label="省份" prop="province">
        <el-select v-model="shiftForm.province" placeholder="选择省份" filterable clearable @change="handleShiftFormProvinceChange">
          <el-option v-for="item in provinceOptions" :key="item" :label="item" :value="item" />
        </el-select>
      </el-form-item>
      <el-form-item label="城市" prop="city">
        <el-select v-model="shiftForm.city" placeholder="选择城市" filterable clearable :disabled="!shiftForm.province" @change="handleShiftFormCityChange">
          <el-option v-for="item in shiftFormCityOptions" :key="item" :label="item" :value="item" />
        </el-select>
      </el-form-item>
      <el-form-item label="区县" prop="district">
        <el-select v-model="shiftForm.district" placeholder="选择区县" filterable clearable :disabled="!shiftForm.city">
          <el-option v-for="item in shiftFormDistrictOptions" :key="item" :label="item" :value="item" />
        </el-select>
      </el-form-item>
      <el-form-item label="详细地点" prop="detailAddress" class="pet-admin-span-2">
        <el-input v-model="shiftForm.detailAddress" placeholder="请输入活动地点" />
      </el-form-item>
      <el-form-item label="任务内容" class="pet-admin-span-2">
        <el-input v-model="shiftForm.content" type="textarea" :autosize="{ minRows: 4, maxRows: 7 }" :disabled="Boolean(shiftForm.id)" />
      </el-form-item>
      <el-form-item label="备注" class="pet-admin-span-2">
        <el-input v-model="shiftForm.remark" type="textarea" :autosize="{ minRows: 3, maxRows: 6 }" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="shiftDialogVisible = false">取消</el-button>
      <el-button type="warning" :loading="savingShift" @click="saveShift">保存</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="shiftStatusDialogVisible" title="修改排班状态" width="520px">
    <el-form ref="shiftStatusFormRef" :model="shiftStatusForm" :rules="shiftStatusRules" label-position="top">
      <el-form-item label="排班状态" prop="status">
        <el-select v-model="shiftStatusForm.status" placeholder="请选择状态">
          <el-option v-for="item in availableShiftStatusOptions()" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="原因说明" prop="reason">
        <el-input v-model="shiftStatusForm.reason" type="textarea" :autosize="{ minRows: 4, maxRows: 7 }" />
      </el-form-item>
    </el-form>
    <div class="dialog-footer">
      <el-button @click="shiftStatusDialogVisible = false">取消</el-button>
      <el-button type="warning" :loading="savingShiftStatus" @click="saveShiftStatus">保存</el-button>
    </div>
    <AuditRecordList :records="shiftAuditRecords" type="shift" :target-id="shiftStatusForm.id" :status-labels="shiftStatusLabelMap" />
  </el-dialog>

  <el-dialog v-model="recordDialogVisible" title="提交活动报告" width="720px">
    <el-form ref="recordFormRef" :model="recordForm" :rules="recordRules" label-position="top" class="pet-admin-form">
      <el-form-item label="服务开始时间" prop="startTime">
        <el-date-picker v-model="recordForm.startTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" class="full-width-control" />
      </el-form-item>
      <el-form-item label="服务结束时间" prop="endTime">
        <el-date-picker v-model="recordForm.endTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" class="full-width-control" />
      </el-form-item>
      <el-form-item label="实际服务时长">
        <el-input-number v-model="recordForm.actualHours" :min="0" :precision="1" :step="0.5" :controls="false" class="full-width-control" />
      </el-form-item>
      <el-form-item label="服务摘要" prop="summary" class="pet-admin-span-2">
        <el-input v-model="recordForm.summary" />
      </el-form-item>
      <el-form-item label="服务内容" class="pet-admin-span-2">
        <el-input v-model="recordForm.content" type="textarea" :autosize="{ minRows: 4, maxRows: 7 }" />
      </el-form-item>
      <el-form-item label="问题反馈" class="pet-admin-span-2">
        <el-input v-model="recordForm.problem" type="textarea" :autosize="{ minRows: 3, maxRows: 6 }" />
      </el-form-item>
      <el-form-item label="改进建议" class="pet-admin-span-2">
        <el-input v-model="recordForm.suggestion" type="textarea" :autosize="{ minRows: 3, maxRows: 6 }" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="recordDialogVisible = false">取消</el-button>
      <el-button type="warning" :loading="savingRecord" @click="saveRecord">提交</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="recordReviewDialogVisible" title="审核活动报告" width="520px">
    <el-form ref="recordReviewFormRef" :model="recordReviewForm" :rules="recordReviewRules" label-position="top">
      <el-form-item label="审核结果" prop="status">
        <el-select v-model="recordReviewForm.status" placeholder="请选择结果">
          <el-option label="已通过" value="APPROVED" />
          <el-option label="已驳回" value="REJECTED" />
        </el-select>
      </el-form-item>
      <el-form-item label="审核意见" prop="reason">
        <el-input v-model="recordReviewForm.reason" type="textarea" :autosize="{ minRows: 4, maxRows: 7 }" placeholder="填写审核说明或驳回原因" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="recordReviewDialogVisible = false">取消</el-button>
      <el-button type="warning" :loading="savingRecordReview" @click="saveRecordReview">保存</el-button>
    </template>
  </el-dialog>
</template>
