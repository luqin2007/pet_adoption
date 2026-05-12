<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Check, Plus, Search } from '@element-plus/icons-vue'
import {
  createRecruitment,
  createVolunteerReward,
  createVolunteerServiceRecord,
  createVolunteerShift,
  getRecruitments,
  getVolunteerApplications,
  getVolunteerProfile,
  getVolunteerProfiles,
  getVolunteerRewards,
  getVolunteerServiceRecords,
  getVolunteerShifts,
  issueVolunteerReward,
  updateRecruitment,
  updateRecruitmentStatus,
  updateVolunteerApplicationStatus,
  updateVolunteerProfile,
  updateVolunteerProfileStatus,
  updateVolunteerServiceRecordStatus,
  updateVolunteerShift,
  updateVolunteerShiftStatus,
} from '../api/volunteer'
import { useInformationCatalog } from '../composables/useInformationCatalog'
import { useUserStore } from '../stores/user'
import TableActionColumnHeader from './TableActionColumnHeader.vue'
import AuditRecordList from './AuditRecordList.vue'
import { useRoute, useRouter } from 'vue-router'

const router = useRouter()
const route = useRoute()

const props = defineProps({
  section: {
    type: String,
    default: 'applications',
  },
  hideTabs: {
    type: Boolean,
    default: false,
  },
})

const userStore = useUserStore()

const ROLE = {
  VOLUNTEER: 1,
  WORKER: 2,
  DONOR: 4,
  DOCTOR: 8,
  ADMIN: 16,
}

const recruitmentLoading = ref(false)
const applicationLoading = ref(false)
const profileLoading = ref(false)
const rewardLoading = ref(false)
const shiftLoading = ref(false)
const recordLoading = ref(false)
const volunteerOptionsLoading = ref(false)
const recruitmentActionCollapsed = ref(false)
const applicationActionCollapsed = ref(false)
const profileActionCollapsed = ref(false)
const rewardActionCollapsed = ref(false)
const shiftActionCollapsed = ref(false)
const recordActionCollapsed = ref(false)

const activeSection = ref(props.section)

const recruitmentRows = ref([])
const recruitmentTotal = ref(0)
const applicationRows = ref([])
const applicationTotal = ref(0)
const profileRows = ref([])
const profileTotal = ref(0)
const rewardRows = ref([])
const rewardTotal = ref(0)
const shiftRows = ref([])
const shiftTotal = ref(0)
const recordRows = ref([])
const recordTotal = ref(0)
const volunteerOptions = ref([])

const recruitmentDialogVisible = ref(false)
const recruitmentStatusDialogVisible = ref(false)
const applicationReviewDialogVisible = ref(false)
const profileDialogVisible = ref(false)
const rewardDialogVisible = ref(false)
const shiftDialogVisible = ref(false)
const shiftStatusDialogVisible = ref(false)
const shiftAuditRecords = ref([])
const recordDialogVisible = ref(false)
const recordReviewDialogVisible = ref(false)

const recruitmentFormRef = ref()
const applicationReviewFormRef = ref()
const profileFormRef = ref()
const rewardFormRef = ref()
const shiftFormRef = ref()
const shiftStatusFormRef = ref()
const recordFormRef = ref()
const recordReviewFormRef = ref()

const savingRecruitment = ref(false)
const savingApplicationReview = ref(false)
const savingProfile = ref(false)
const savingReward = ref(false)
const savingShift = ref(false)
const savingShiftStatus = ref(false)
const savingRecord = ref(false)
const savingRecordReview = ref(false)

function hasRole(role, bit) {
  return (Number(role || 0) & bit) === bit
}

const loginRole = computed(() => Number(userStore.profile.role || 0))
const isAdmin = computed(() => hasRole(loginRole.value, ROLE.ADMIN))
const isWorker = computed(() => isAdmin.value || hasRole(loginRole.value, ROLE.WORKER))
const isVolunteer = computed(() => hasRole(loginRole.value, ROLE.VOLUNTEER))
const loginUserId = computed(() => String(userStore.profile.id || ''))
const activityVolunteerId = computed(() => String(route.query.volunteer || ''))
const activityShiftId = computed(() => String(route.query.shift || ''))

const sections = computed(() => {
  const items = [{ label: '招募申请', value: 'applications' }]
  if (isWorker.value) {
    items.unshift({ label: '招募计划', value: 'recruitments' })
    items.push({ label: '志愿者档案', value: 'profiles' })
  }
  if (isWorker.value || isVolunteer.value) {
    items.push({ label: '志愿者激励', value: 'rewards' })
    items.push({ label: '志愿活动', value: 'activities' })
  }
  return items
})

const recruitmentStatusOptions = [
  { label: '草稿', value: 'DRAFT' },
  { label: '招募中', value: 'PUBLISHED' },
  { label: '已关闭', value: 'CLOSED' },
]

const applicationStatusOptions = [
  { label: '已提交', value: 'SUBMITTED' },
  { label: '审核中', value: 'UNDER_REVIEW' },
  { label: '已通过', value: 'APPROVED' },
  { label: '已拒绝', value: 'REJECTED' },
  { label: '已撤回', value: 'CANCELED' },
]

const profileStatusOptions = [
  { label: '启用', value: 'ACTIVE' },
  { label: '停用', value: 'DISABLED' },
]

const rewardStatusOptions = [
  { label: '待发放', value: 'PENDING' },
  { label: '已发放', value: 'ISSUED' },
  { label: '已取消', value: 'CANCELLED' },
]

const rewardTypeOptions = [
  { label: '物资奖励', value: 'MATERIAL' },
  { label: '荣誉证书', value: 'CERTIFICATE' },
  { label: '积分', value: 'POINTS' },
  { label: '公益时长证明', value: 'SERVICE_HOURS' },
  { label: '现金/补贴', value: 'CASH' },
  { label: '其他', value: 'OTHER' },
]

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

const recruitmentPage = reactive({ page: 1, size: 10 })
const applicationPage = reactive({ page: 1, size: 10 })
const profilePage = reactive({ page: 1, size: 10 })
const rewardPage = reactive({ page: 1, size: 10 })
const shiftPage = reactive({ page: 1, size: 10 })
const recordPage = reactive({ page: 1, size: 10 })

const recruitmentSearch = reactive({
  title: '',
  province: '',
  city: '',
  status: '',
  timeRange: [],
})

const applicationSearch = reactive({
  status: '',
  province: '',
  city: '',
})

const profileSearch = reactive({
  keyword: '',
  status: '',
})

const rewardSearch = reactive({
  status: '',
  type: '',
  timeRange: [],
})

const shiftSearch = reactive({
  status: '',
  taskType: '',
  timeRange: [],
})

const recordSearch = reactive({
  status: '',
  timeRange: [],
})

const recruitmentForm = reactive({
  id: '',
  title: '',
  description: '',
  requirement: '',
  headcount: 1,
  province: '',
  city: '',
  district: '',
  detailAddress: '',
  timeRange: [],
})

const recruitmentStatusForm = reactive({
  id: '',
  status: '',
  reason: '',
  autoReason: false,
})

const applicationReviewForm = reactive({
  id: '',
  status: '',
  reason: '',
})

const profileForm = reactive({
  id: '',
  realName: '',
  sex: '',
  phone: '',
  province: '',
  city: '',
  district: '',
  detailAddress: '',
  skills: '',
  serviceIntention: '',
  availableTimeDesc: '',
  remark: '',
})

const rewardForm = reactive({
  volunteerId: '',
  periodRange: [],
  serviceCount: undefined,
  totalHours: undefined,
  rewardType: '',
  rewardValue: '',
  rewardReason: '',
  remark: '',
})

const shiftForm = reactive({
  id: '',
  volunteerId: '',
  taskType: 'EVENT',
  taskId: '',
  title: '',
  content: '',
  province: '',
  city: '',
  district: '',
  detailAddress: '',
  shiftTimeRange: [],
  taskTimeRange: [],
  remark: '',
})

const shiftStatusForm = reactive({
  id: '',
  status: '',
  currentStatus: '',
  reason: '',
})

const recordForm = reactive({
  shiftId: '',
  startTime: '',
  endTime: '',
  actualHours: undefined,
  summary: '',
  content: '',
  problem: '',
  suggestion: '',
})

const recordReviewForm = reactive({
  id: '',
  status: '',
  reason: '',
})

const {
  ensureInformationCatalog,
  ensureCityOptions,
  ensureDistrictOptions,
  provinceOptions,
  getCityOptions,
  getDistrictOptions,
} = useInformationCatalog()

const recruitmentCityOptions = computed(() => getCityOptions(recruitmentSearch.province))
const recruitmentFormCityOptions = computed(() => getCityOptions(recruitmentForm.province))
const recruitmentFormDistrictOptions = computed(() => getDistrictOptions(recruitmentForm.province, recruitmentForm.city))
const applicationCityOptions = computed(() => getCityOptions(applicationSearch.province))
const profileFormCityOptions = computed(() => getCityOptions(profileForm.province))
const profileFormDistrictOptions = computed(() => getDistrictOptions(profileForm.province, profileForm.city))
const shiftFormCityOptions = computed(() => getCityOptions(shiftForm.province))
const shiftFormDistrictOptions = computed(() => getDistrictOptions(shiftForm.province, shiftForm.city))

const recruitmentRules = {
  title: [{ required: true, message: '请输入招募标题', trigger: 'blur' }],
  description: [{ required: true, message: '请输入招募说明', trigger: 'blur' }],
  requirement: [{ required: true, message: '请输入参与要求', trigger: 'blur' }],
  headcount: [{ required: true, message: '请输入招募人数', trigger: 'change' }],
  province: [{ required: true, message: '请选择省份', trigger: 'change' }],
  city: [{ required: true, message: '请选择城市', trigger: 'change' }],
  district: [{ required: true, message: '请选择区县', trigger: 'change' }],
  detailAddress: [{ required: true, message: '请输入详细地点', trigger: 'blur' }],
  timeRange: [{ required: true, message: '请选择招募时间', trigger: 'change' }],
}

const applicationReviewRules = {
  status: [{ required: true, message: '请选择审核状态', trigger: 'change' }],
  reason: [{ required: true, message: '请填写审核说明', trigger: 'blur' }],
}

const profileRules = {
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  sex: [{ required: true, message: '请选择性别', trigger: 'change' }],
  phone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }],
  province: [{ required: true, message: '请选择省份', trigger: 'change' }],
  city: [{ required: true, message: '请选择城市', trigger: 'change' }],
  district: [{ required: true, message: '请选择区县', trigger: 'change' }],
  detailAddress: [{ required: true, message: '请输入详细地点', trigger: 'blur' }],
}

const rewardRules = {
  volunteerId: [{ required: true, message: '请选择志愿者', trigger: 'change' }],
  periodRange: [{ required: true, message: '请选择统计周期', trigger: 'change' }],
  rewardType: [{ required: true, message: '请选择激励类型', trigger: 'change' }],
  rewardReason: [{ required: true, message: '请输入激励原因', trigger: 'blur' }],
}

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

function formatDate(value, withTime = false) {
  if (!value) return '待补充'
  const text = String(value)
  return withTime ? text.slice(0, 16).replace('T', ' ') : text.slice(0, 10)
}

function formatDateTimeString(value) {
  if (!value) return ''
  const date = new Date(value)
  const year = date.getFullYear()
  const month = `${date.getMonth() + 1}`.padStart(2, '0')
  const day = `${date.getDate()}`.padStart(2, '0')
  const hour = `${date.getHours()}`.padStart(2, '0')
  const minute = `${date.getMinutes()}`.padStart(2, '0')
  const second = `${date.getSeconds()}`.padStart(2, '0')
  return `${year}-${month}-${day} ${hour}:${minute}:${second}`
}

function toApiDateTime(value) {
  if (!value) return undefined
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? value : date.toISOString()
}

function canDirectCloseRecruitment(row) {
  if (!row?.endTime) return false
  return new Date().getTime() >= new Date(row.endTime).getTime()
}

function recruitmentStatusText(value) {
  return recruitmentStatusOptions.find((item) => item.value === value)?.label || value || '待补充'
}

function applicationStatusText(value) {
  return applicationStatusOptions.find((item) => item.value === value)?.label || value || '待补充'
}

function profileStatusText(value) {
  return profileStatusOptions.find((item) => item.value === value)?.label || value || '待补充'
}

function tagTypeByStatus(value) {
  if (['APPROVED', 'ACTIVE', 'ISSUED', 'COMPLETED'].includes(value)) return 'success'
  if (['UNDER_REVIEW', 'CONFIRMED', 'IN_PROGRESS'].includes(value)) return 'primary'
  if (['REJECTED', 'CANCELED', 'DISABLED', 'CANCELLED', 'ABSENT'].includes(value)) return 'info'
  return 'warning'
}

function rewardStatusText(value) {
  return rewardStatusOptions.find((item) => item.value === value)?.label || value || '待补充'
}

function rewardTypeText(value) {
  return rewardTypeOptions.find((item) => item.value === value)?.label || value || '待补充'
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

function locationText(row) {
  return [row.province, row.city, row.district, row.serviceAddress || row.address || row.detailAddress].filter(Boolean).join(' · ') || '位置待补充'
}

function goRecruitmentDetail(id) {
  if (id) router.push(`/volunteers/recruitments/${id}`)
}

function goApplicationDetail(row) {
  if (row?.id) router.push(`/console/volunteer/applications/${row.id}`)
}

function goVolunteerActivity(row) {
  const volunteerId = row?.volunteerId || row?.userId
  if (!volunteerId) return
  router.push({ path: '/console/volunteer/activities', query: { volunteer: volunteerId } })
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

function handleRecruitmentSearchProvinceChange() {
  recruitmentSearch.city = ''
  if (recruitmentSearch.province) {
    ensureCityOptions(recruitmentSearch.province)
  }
}

function handleApplicationProvinceChange() {
  applicationSearch.city = ''
  if (applicationSearch.province) {
    ensureCityOptions(applicationSearch.province)
  }
}

function handleProfileFormProvinceChange() {
  profileForm.city = ''
  profileForm.district = ''
  if (profileForm.province) {
    ensureCityOptions(profileForm.province)
  }
}

function handleProfileFormCityChange() {
  profileForm.district = ''
  if (profileForm.province && profileForm.city) {
    ensureDistrictOptions(profileForm.province, profileForm.city)
  }
}

function handleRecruitmentFormProvinceChange() {
  recruitmentForm.city = ''
  recruitmentForm.district = ''
  if (recruitmentForm.province) {
    ensureCityOptions(recruitmentForm.province)
  }
}

function handleRecruitmentFormCityChange() {
  recruitmentForm.district = ''
  if (recruitmentForm.province && recruitmentForm.city) {
    ensureDistrictOptions(recruitmentForm.province, recruitmentForm.city)
  }
}

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

function buildRecruitmentQuery() {
  const [time0, time1] = recruitmentSearch.timeRange || []
  return {
    page: recruitmentPage.page,
    size: recruitmentPage.size,
    title: recruitmentSearch.title.trim() || undefined,
    province: recruitmentSearch.province || undefined,
    city: recruitmentSearch.city || undefined,
    status: recruitmentSearch.status ? [recruitmentSearch.status] : undefined,
    time0: toApiDateTime(time0),
    time1: toApiDateTime(time1),
  }
}

async function loadRecruitmentsData() {
  recruitmentLoading.value = true
  try {
    const result = await getRecruitments(buildRecruitmentQuery())
    recruitmentRows.value = Array.isArray(result?.records) ? result.records : []
    recruitmentTotal.value = Number(result?.total || 0)
  } catch (error) {
    ElMessage.warning(error?.message || '加载招募计划失败')
  } finally {
    recruitmentLoading.value = false
  }
}

function searchRecruitments() {
  recruitmentPage.page = 1
  loadRecruitmentsData()
}

function changeRecruitmentPage(page) {
  recruitmentPage.page = page
  loadRecruitmentsData()
}

function resetRecruitmentForm() {
  recruitmentForm.id = ''
  recruitmentForm.title = ''
  recruitmentForm.description = ''
  recruitmentForm.requirement = ''
  recruitmentForm.headcount = 1
  recruitmentForm.province = ''
  recruitmentForm.city = ''
  recruitmentForm.district = ''
  recruitmentForm.detailAddress = ''
  recruitmentForm.timeRange = []
}

function openRecruitmentDialog(row = null) {
  if (row) {
    recruitmentForm.id = row.id
    recruitmentForm.title = row.title || ''
    recruitmentForm.description = row.description || ''
    recruitmentForm.requirement = row.requirement || ''
    recruitmentForm.headcount = Number(row.headcount || 1)
    recruitmentForm.province = row.province || ''
    recruitmentForm.city = row.city || ''
    recruitmentForm.district = row.district || ''
    recruitmentForm.detailAddress = row.serviceAddress || ''
    recruitmentForm.timeRange = [row.startTime || '', row.endTime || '']
  } else {
    resetRecruitmentForm()
  }
  recruitmentDialogVisible.value = true
}

async function saveRecruitment() {
  if (!recruitmentFormRef.value) return
  try {
    await recruitmentFormRef.value.validate()
  } catch {
    return
  }
  savingRecruitment.value = true
  try {
    const payload = {
      title: recruitmentForm.title.trim(),
      description: recruitmentForm.description.trim(),
      requirement: recruitmentForm.requirement.trim(),
      headcount: Number(recruitmentForm.headcount || 1),
      province: recruitmentForm.province,
      city: recruitmentForm.city,
      district: recruitmentForm.district,
      detailAddress: recruitmentForm.detailAddress.trim(),
      startTime: toApiDateTime(recruitmentForm.timeRange?.[0]),
      endTime: toApiDateTime(recruitmentForm.timeRange?.[1]),
    }
    if (recruitmentForm.id) {
      await updateRecruitment(recruitmentForm.id, payload)
      ElMessage.success('招募计划已更新')
    } else {
      await createRecruitment(payload)
      ElMessage.success('招募计划已创建')
    }
    recruitmentDialogVisible.value = false
    loadRecruitmentsData()
  } catch (error) {
    ElMessage.warning(error?.message || '保存招募计划失败')
  } finally {
    savingRecruitment.value = false
  }
}

function openRecruitmentStatusDialog(row, status = 'CLOSED') {
  recruitmentStatusForm.id = row.id
  recruitmentStatusForm.status = status
  recruitmentStatusForm.autoReason = status === 'CLOSED' && canDirectCloseRecruitment(row)
  if (recruitmentStatusForm.autoReason) {
    recruitmentStatusForm.reason = `~ ${formatDateTimeString(new Date())}`
  } else if (status === 'PUBLISHED') {
    recruitmentStatusForm.reason = `发布招募 ${formatDateTimeString(new Date())}`
  } else {
    recruitmentStatusForm.reason = ''
  }
  recruitmentStatusDialogVisible.value = true
}

async function saveRecruitmentStatus() {
  if (!recruitmentStatusForm.id || !recruitmentStatusForm.status) return
  if (!recruitmentStatusForm.reason.trim()) {
    ElMessage.warning('请填写原因')
    return
  }
  savingRecruitment.value = true
  try {
    await updateRecruitmentStatus(recruitmentStatusForm.id, {
      status: recruitmentStatusForm.status,
      reason: recruitmentStatusForm.reason.trim(),
    })
    ElMessage.success('招募状态已更新')
    recruitmentStatusDialogVisible.value = false
    loadRecruitmentsData()
  } catch (error) {
    ElMessage.warning(error?.message || '更新招募状态失败')
  } finally {
    savingRecruitment.value = false
  }
}

function buildApplicationQuery() {
  return {
    page: applicationPage.page,
    size: applicationPage.size,
    province: applicationSearch.province || undefined,
    city: applicationSearch.city || undefined,
    status: applicationSearch.status ? [applicationSearch.status] : undefined,
  }
}

async function loadApplications() {
  applicationLoading.value = true
  try {
    const result = await getVolunteerApplications(buildApplicationQuery())
    applicationRows.value = Array.isArray(result?.records) ? result.records : []
    applicationTotal.value = Number(result?.total || 0)
  } catch (error) {
    ElMessage.warning(error?.message || '加载招募申请失败')
  } finally {
    applicationLoading.value = false
  }
}

function searchApplications() {
  applicationPage.page = 1
  loadApplications()
}

function changeApplicationPage(page) {
  applicationPage.page = page
  loadApplications()
}

function buildProfileQuery() {
  return {
    page: profilePage.page,
    size: profilePage.size,
    keyword: profileSearch.keyword.trim() || undefined,
    status: profileSearch.status ? [profileSearch.status] : undefined,
  }
}

async function loadProfiles() {
  if (!isWorker.value) return
  profileLoading.value = true
  try {
    const result = await getVolunteerProfiles(buildProfileQuery())
    profileRows.value = Array.isArray(result?.records) ? result.records : []
    profileTotal.value = Number(result?.total || 0)
  } catch (error) {
    ElMessage.warning(error?.message || '加载志愿者档案失败')
  } finally {
    profileLoading.value = false
  }
}

function searchProfiles() {
  profilePage.page = 1
  loadProfiles()
}

function changeProfilePage(page) {
  profilePage.page = page
  loadProfiles()
}

function resetProfileForm() {
  profileForm.id = ''
  profileForm.realName = ''
  profileForm.sex = ''
  profileForm.phone = ''
  profileForm.province = ''
  profileForm.city = ''
  profileForm.district = ''
  profileForm.detailAddress = ''
  profileForm.skills = ''
  profileForm.serviceIntention = ''
  profileForm.availableTimeDesc = ''
  profileForm.remark = ''
}

async function openProfileDialog(row) {
  resetProfileForm()
  profileDialogVisible.value = true
  profileLoading.value = true
  try {
    const detail = await getVolunteerProfile(row.id)
    Object.assign(profileForm, {
      id: detail.id || row.id,
      realName: detail.realName || '',
      sex: detail.sex || '',
      phone: detail.phone || '',
      province: detail.province || '',
      city: detail.city || '',
      district: detail.district || '',
      detailAddress: detail.address || '',
      skills: detail.skills || '',
      serviceIntention: detail.serviceIntention || '',
      availableTimeDesc: detail.availableTimeDesc || '',
      remark: detail.remark || '',
    })
    if (profileForm.province) ensureCityOptions(profileForm.province)
    if (profileForm.province && profileForm.city) ensureDistrictOptions(profileForm.province, profileForm.city)
  } catch (error) {
    profileDialogVisible.value = false
    ElMessage.warning(error?.message || '加载志愿者档案详情失败')
  } finally {
    profileLoading.value = false
  }
}

async function saveProfile() {
  if (!profileFormRef.value) return
  try {
    await profileFormRef.value.validate()
  } catch {
    return
  }
  savingProfile.value = true
  try {
    await updateVolunteerProfile(profileForm.id, {
      realName: profileForm.realName.trim(),
      sex: profileForm.sex,
      phone: profileForm.phone.trim(),
      province: profileForm.province,
      city: profileForm.city,
      district: profileForm.district,
      detailAddress: profileForm.detailAddress.trim(),
      skills: profileForm.skills.trim() || undefined,
      serviceIntention: profileForm.serviceIntention.trim() || undefined,
      availableTimeDesc: profileForm.availableTimeDesc.trim() || undefined,
      remark: profileForm.remark.trim() || undefined,
    })
    ElMessage.success('志愿者档案已保存')
    profileDialogVisible.value = false
    loadProfiles()
    loadVolunteerOptions()
  } catch (error) {
    ElMessage.warning(error?.message || '保存志愿者档案失败')
  } finally {
    savingProfile.value = false
  }
}

async function toggleProfileStatus(row) {
  const nextStatus = row.status === 'ACTIVE' ? 'DISABLED' : 'ACTIVE'
  const actionText = nextStatus === 'ACTIVE' ? '启用' : '停用'
  try {
    await ElMessageBox.confirm(`确认${actionText}「${row.realName || row.username || '该志愿者'}」的志愿者档案？`, `${actionText}档案`, {
      type: 'warning',
      confirmButtonText: actionText,
      cancelButtonText: '取消',
    })
    await updateVolunteerProfileStatus(row.id, nextStatus)
    ElMessage.success(`志愿者档案已${actionText}`)
    loadProfiles()
    loadVolunteerOptions()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.warning(error?.message || `${actionText}档案失败`)
    }
  }
}

function openApplicationReviewDialog(row) {
  applicationReviewForm.id = row.id
  applicationReviewForm.status = row.status === 'SUBMITTED' ? 'UNDER_REVIEW' : ''
  applicationReviewForm.reason = row.reviewComment || ''
  applicationReviewDialogVisible.value = true
}

function applicationReviewStatusOptions() {
  const current = applicationRows.value.find((item) => item.id === applicationReviewForm.id)?.status
  if (current === 'SUBMITTED') {
    return applicationStatusOptions.filter((item) => ['UNDER_REVIEW', 'APPROVED', 'REJECTED'].includes(item.value))
  }
  if (current === 'UNDER_REVIEW') {
    return applicationStatusOptions.filter((item) => ['APPROVED', 'REJECTED'].includes(item.value))
  }
  return applicationStatusOptions.filter((item) => ['APPROVED', 'REJECTED'].includes(item.value))
}

async function saveApplicationReview() {
  if (!applicationReviewFormRef.value) return
  try {
    await applicationReviewFormRef.value.validate()
  } catch {
    return
  }
  savingApplicationReview.value = true
  try {
    await updateVolunteerApplicationStatus(applicationReviewForm.id, {
      status: applicationReviewForm.status,
      reason: applicationReviewForm.reason.trim(),
    })
    ElMessage.success('申请状态已更新')
    applicationReviewDialogVisible.value = false
    loadApplications()
  } catch (error) {
    ElMessage.warning(error?.message || '更新申请状态失败')
  } finally {
    savingApplicationReview.value = false
  }
}

function canReviewApplication(row) {
  return isWorker.value && ['SUBMITTED', 'UNDER_REVIEW'].includes(row?.status)
}

function canCancelApplication(row) {
  return !isWorker.value && ['SUBMITTED', 'UNDER_REVIEW'].includes(row?.status) && String(row?.userId || '') === loginUserId.value
}

async function cancelApplication(row) {
  try {
    await ElMessageBox.confirm(`确认撤回「${row.recruitmentTitle || '该招募'}」的志愿者申请？`, '撤回申请', {
      type: 'warning',
      confirmButtonText: '撤回',
      cancelButtonText: '取消',
    })
    await updateVolunteerApplicationStatus(row.id, {
      status: 'CANCELED',
      reason: `${Date.now()} 用户撤回申请`,
    })
    ElMessage.success('志愿者申请已撤回')
    loadApplications()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.warning(error?.message || '撤回申请失败')
    }
  }
}

function buildRewardQuery() {
  const [time0, time1] = rewardSearch.timeRange || []
  return {
    page: rewardPage.page,
    size: rewardPage.size,
    status: rewardSearch.status ? [rewardSearch.status] : undefined,
    type: rewardSearch.type ? [rewardSearch.type] : undefined,
    time0: toApiDateTime(time0),
    time1: toApiDateTime(time1),
  }
}

async function loadRewards() {
  rewardLoading.value = true
  try {
    const result = await getVolunteerRewards(buildRewardQuery())
    rewardRows.value = Array.isArray(result?.records) ? result.records : []
    rewardTotal.value = Number(result?.total || 0)
  } catch (error) {
    ElMessage.warning(error?.message || '加载激励记录失败')
  } finally {
    rewardLoading.value = false
  }
}

function searchRewards() {
  rewardPage.page = 1
  loadRewards()
}

function changeRewardPage(page) {
  rewardPage.page = page
  loadRewards()
}

function resetRewardForm() {
  rewardForm.volunteerId = ''
  rewardForm.periodRange = []
  rewardForm.serviceCount = undefined
  rewardForm.totalHours = undefined
  rewardForm.rewardType = ''
  rewardForm.rewardValue = ''
  rewardForm.rewardReason = ''
  rewardForm.remark = ''
}

function openRewardDialog() {
  resetRewardForm()
  rewardDialogVisible.value = true
  loadVolunteerOptions()
}

async function saveReward() {
  if (!rewardFormRef.value) return
  try {
    await rewardFormRef.value.validate()
  } catch {
    return
  }
  savingReward.value = true
  try {
    await createVolunteerReward({
      volunteerId: rewardForm.volunteerId,
      periodStart: toApiDateTime(rewardForm.periodRange?.[0]),
      periodEnd: toApiDateTime(rewardForm.periodRange?.[1]),
      serviceCount: rewardForm.serviceCount ? Number(rewardForm.serviceCount) : undefined,
      totalHours: rewardForm.totalHours,
      rewardType: rewardForm.rewardType,
      rewardValue: rewardForm.rewardValue.trim() || undefined,
      rewardReason: rewardForm.rewardReason.trim(),
      remark: rewardForm.remark.trim() || undefined,
    })
    ElMessage.success('激励记录已创建')
    rewardDialogVisible.value = false
    loadRewards()
  } catch (error) {
    ElMessage.warning(error?.message || '创建激励记录失败')
  } finally {
    savingReward.value = false
  }
}

async function issueReward(row) {
  try {
    await issueVolunteerReward(row.id)
    ElMessage.success('激励已发放')
    loadRewards()
  } catch (error) {
    ElMessage.warning(error?.message || '发放激励失败')
  }
}

function buildShiftQuery() {
  const [time0, time1] = shiftSearch.timeRange || []
  return {
    page: shiftPage.page,
    size: shiftPage.size,
    volunteer: activityVolunteerId.value || undefined,
    status: shiftSearch.status ? [shiftSearch.status] : undefined,
    taskType: shiftSearch.taskType ? [shiftSearch.taskType] : undefined,
    time0: toApiDateTime(time0),
    time1: toApiDateTime(time1),
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
  try {
    await shiftFormRef.value.validate()
  } catch {
    return
  }
  savingShift.value = true
  try {
    if (shiftForm.id) {
      await updateVolunteerShift(shiftForm.id, {
        volunteerId: shiftForm.volunteerId,
        province: shiftForm.province,
        city: shiftForm.city,
        district: shiftForm.district,
        detailAddress: shiftForm.detailAddress.trim(),
        startTime: toApiDateTime(shiftForm.shiftTimeRange?.[0]),
        endTime: toApiDateTime(shiftForm.shiftTimeRange?.[1]),
      })
      ElMessage.success('排班已更新')
    } else {
      await createVolunteerShift({
        volunteerId: shiftForm.volunteerId,
        taskType: shiftForm.taskType,
        taskId: shiftForm.taskId || undefined,
        title: shiftForm.title.trim(),
        content: shiftForm.content.trim() || undefined,
        province: shiftForm.province,
        city: shiftForm.city,
        district: shiftForm.district,
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
  try {
    await shiftStatusFormRef.value.validate()
  } catch {
    return
  }
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
      type: 'warning',
      inputType: 'textarea',
      inputPlaceholder: '请说明无法参与本次排班的原因',
      inputValidator: (value) => Boolean(String(value || '').trim()) || '请输入原因',
      confirmButtonText: '提交',
      cancelButtonText: '取消',
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
    page: recordPage.page,
    size: recordPage.size,
    volunteer: activityVolunteerId.value || undefined,
    shift: activityShiftId.value || undefined,
    status: recordSearch.status ? [recordSearch.status] : undefined,
    time0: toApiDateTime(time0),
    time1: toApiDateTime(time1),
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
  try {
    await recordFormRef.value.validate()
  } catch {
    return
  }
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
  try {
    await recordReviewFormRef.value.validate()
  } catch {
    return
  }
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
    const result = await getVolunteerProfiles({
      size: 100,
      status: ['ACTIVE'],
    })
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

async function loadBySection(section) {
  if (section === 'recruitments' && isWorker.value) {
    await loadRecruitmentsData()
  } else if (section === 'applications') {
    await loadApplications()
  } else if (section === 'profiles' && isWorker.value) {
    await loadProfiles()
  } else if (section === 'rewards' && (isWorker.value || isVolunteer.value)) {
    await loadRewards()
  } else if (section === 'activities' && (isWorker.value || isVolunteer.value)) {
    await Promise.all([loadShifts(), loadRecords()])
  }
}

watch(
  sections,
  (value) => {
    if (!value.some((item) => item.value === activeSection.value)) {
      activeSection.value = value[0]?.value || 'applications'
    }
  },
  { immediate: true },
)

watch(
  () => props.section,
  (value) => {
    if (value && value !== activeSection.value) {
      activeSection.value = value
    }
  },
  { immediate: true },
)

watch(
  activeSection,
  (value) => {
    loadBySection(value)
  },
  { immediate: false },
)

watch(
  () => [route.query.volunteer, route.query.shift],
  () => {
    if (activeSection.value === 'activities') {
      shiftPage.page = 1
      recordPage.page = 1
      loadBySection('activities')
    }
  },
)

onMounted(async () => {
  await ensureInformationCatalog()
  await loadBySection(activeSection.value)
})
</script>

<template>
  <el-card class="profile-card volunteer-admin-card">
    <template #header>
      <div class="profile-card-header">
        <strong>志愿者管理</strong>
        <span>招募、申请、激励和排班都在这里</span>
      </div>
    </template>

    <div class="volunteer-admin-shell">
      <el-segmented v-if="!props.hideTabs" v-model="activeSection" :options="sections" block class="volunteer-admin-tabs" />

      <section v-if="activeSection === 'recruitments'" class="pet-admin-section">
        <section class="filter-panel pet-directory-filter-panel">
          <div class="pet-filter-row pet-filter-row-primary volunteer-filter-row-primary">
            <el-input v-model="recruitmentSearch.title" clearable placeholder="招募标题" />
            <div class="pet-cascader-group pet-cascader-group-2">
              <el-select v-model="recruitmentSearch.province" clearable filterable placeholder="省份" @change="handleRecruitmentSearchProvinceChange">
                <el-option v-for="item in provinceOptions" :key="item" :label="item" :value="item" />
              </el-select>
              <el-select v-model="recruitmentSearch.city" clearable filterable placeholder="城市" :disabled="!recruitmentSearch.province">
                <el-option v-for="item in recruitmentCityOptions" :key="item" :label="item" :value="item" />
              </el-select>
            </div>
            <el-select v-model="recruitmentSearch.status" clearable placeholder="状态">
              <el-option v-for="item in recruitmentStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </div>
          <div class="pet-filter-row pet-filter-row-secondary volunteer-filter-row-secondary">
            <el-date-picker
              v-model="recruitmentSearch.timeRange"
              type="daterange"
              value-format="YYYY-MM-DD HH:mm:ss"
              start-placeholder="开始时间"
              end-placeholder="结束时间"
              class="full-width-control"
            />
            <div class="pet-filter-action">
              <el-button class="soft-btn" :icon="Plus" @click="openRecruitmentDialog()">新增招募</el-button>
              <el-button class="warm-btn" :icon="Search" :loading="recruitmentLoading" @click="searchRecruitments">搜索</el-button>
            </div>
          </div>
        </section>

        <el-table :data="recruitmentRows" v-loading="recruitmentLoading" class="user-admin-table">
          <el-table-column label="招募标题" min-width="220" show-overflow-tooltip>
            <template #default="{ row }">
              <button class="table-primary-link" type="button" @click="goRecruitmentDetail(row.id)">{{ row.title || '未命名招募' }}</button>
            </template>
          </el-table-column>
          <el-table-column label="地点" min-width="220">
            <template #default="{ row }">{{ locationText(row) }}</template>
          </el-table-column>
          <el-table-column label="时间" min-width="180">
            <template #default="{ row }">{{ formatDate(row.startTime) }} - {{ formatDate(row.endTime) }}</template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="tagTypeByStatus(row.status)" effect="plain">{{ recruitmentStatusText(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="报名" width="100">
            <template #default="{ row }">{{ row.appliedCount || 0 }}/{{ row.headcount || 0 }}</template>
          </el-table-column>
          <el-table-column width="40" class-name="action-col">
            <template #header>
              <TableActionColumnHeader title="操作" :collapsed="recruitmentActionCollapsed" @toggle="recruitmentActionCollapsed = !recruitmentActionCollapsed" />
            </template>
            <template #default="{ row }">
              <div class="table-action-cell">
                <div class="table-action-panel" :class="{ 'is-collapsed': recruitmentActionCollapsed }">
                  <el-button v-if="row.status === 'DRAFT'" text type="warning" @click="openRecruitmentDialog(row)">编辑</el-button>
                  <el-button v-if="row.status === 'DRAFT'" text type="success" @click="openRecruitmentStatusDialog(row, 'PUBLISHED')">发布</el-button>
                  <el-button v-if="row.status !== 'CLOSED'" text type="danger" @click="openRecruitmentStatusDialog(row, 'CLOSED')">关闭招募</el-button>
                </div>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <div class="user-admin-pagination">
          <el-pagination layout="prev, pager, next, total" :current-page="recruitmentPage.page" :page-size="recruitmentPage.size" :total="recruitmentTotal" @current-change="changeRecruitmentPage" />
        </div>
      </section>

      <section v-else-if="activeSection === 'applications'" class="pet-admin-section">
        <section class="filter-panel pet-directory-filter-panel">
          <div class="pet-filter-row pet-filter-row-primary volunteer-filter-row-primary">
            <el-select v-model="applicationSearch.status" clearable placeholder="申请状态">
              <el-option v-for="item in applicationStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
            <div class="pet-cascader-group pet-cascader-group-2">
              <el-select v-model="applicationSearch.province" clearable filterable placeholder="省份" @change="handleApplicationProvinceChange">
                <el-option v-for="item in provinceOptions" :key="item" :label="item" :value="item" />
              </el-select>
              <el-select v-model="applicationSearch.city" clearable filterable placeholder="城市" :disabled="!applicationSearch.province">
                <el-option v-for="item in applicationCityOptions" :key="item" :label="item" :value="item" />
              </el-select>
            </div>
            <div class="pet-filter-action volunteer-inline-actions">
              <el-button class="warm-btn" :icon="Search" :loading="applicationLoading" @click="searchApplications">搜索</el-button>
            </div>
          </div>
        </section>

        <el-table :data="applicationRows" v-loading="applicationLoading" class="user-admin-table">
          <el-table-column label="招募计划" min-width="220" show-overflow-tooltip>
            <template #default="{ row }">
              <button class="table-primary-link" type="button" @click="goApplicationDetail(row)">{{ row.recruitmentTitle || '未命名招募' }}</button>
            </template>
          </el-table-column>
          <el-table-column label="申请人" min-width="160">
            <template #default="{ row }">
              <div class="volunteer-person-cell">
                <strong>{{ row.realName || row.username || '未命名' }}</strong>
                <span>{{ row.phone || '联系方式待补充' }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="地区" min-width="180">
            <template #default="{ row }">{{ locationText(row) }}</template>
          </el-table-column>
          <el-table-column label="状态" width="110">
            <template #default="{ row }">
              <el-tag :type="tagTypeByStatus(row.status)" effect="plain">{{ applicationStatusText(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="审核意见" min-width="200" show-overflow-tooltip>
            <template #default="{ row }">{{ row.reviewComment || '暂无' }}</template>
          </el-table-column>
          <el-table-column width="40" class-name="action-col">
            <template #header>
              <TableActionColumnHeader title="操作" :collapsed="applicationActionCollapsed" @toggle="applicationActionCollapsed = !applicationActionCollapsed" />
            </template>
            <template #default="{ row }">
              <div class="table-action-cell">
                <div class="table-action-panel" :class="{ 'is-collapsed': applicationActionCollapsed }">
                  <el-button text type="primary" @click="goApplicationDetail(row)">详情</el-button>
                  <el-button v-if="canReviewApplication(row)" text type="primary" @click="openApplicationReviewDialog(row)">审核</el-button>
                  <el-button v-if="canCancelApplication(row)" text type="danger" @click="cancelApplication(row)">撤回</el-button>
                </div>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <div class="user-admin-pagination">
          <el-pagination layout="prev, pager, next, total" :current-page="applicationPage.page" :page-size="applicationPage.size" :total="applicationTotal" @current-change="changeApplicationPage" />
        </div>
      </section>

      <section v-else-if="activeSection === 'profiles'" class="pet-admin-section">
        <section class="filter-panel pet-directory-filter-panel">
          <div class="pet-filter-row pet-filter-row-primary volunteer-filter-row-primary">
            <el-input v-model="profileSearch.keyword" clearable placeholder="姓名、用户名、电话或技能" />
            <el-select v-model="profileSearch.status" clearable placeholder="档案状态">
              <el-option v-for="item in profileStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
            <div class="pet-filter-action volunteer-inline-actions">
              <el-button class="warm-btn" :icon="Search" :loading="profileLoading" @click="searchProfiles">搜索</el-button>
            </div>
          </div>
        </section>

        <el-table :data="profileRows" v-loading="profileLoading" class="user-admin-table">
          <el-table-column label="志愿者" min-width="180">
            <template #default="{ row }">
              <div class="volunteer-person-cell">
                <strong>{{ row.realName || row.username || '未命名' }}</strong>
                <span>{{ row.phone || '联系方式待补充' }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="用户名" min-width="130">
            <template #default="{ row }">{{ row.username || '' }}</template>
          </el-table-column>
          <el-table-column label="地区" min-width="180">
            <template #default="{ row }">{{ locationText(row) }}</template>
          </el-table-column>
          <el-table-column label="技能" min-width="220" show-overflow-tooltip>
            <template #default="{ row }">{{ row.skills || '' }}</template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="tagTypeByStatus(row.status)" effect="plain">{{ profileStatusText(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column width="40" class-name="action-col">
            <template #header>
              <TableActionColumnHeader title="操作" :collapsed="profileActionCollapsed" @toggle="profileActionCollapsed = !profileActionCollapsed" />
            </template>
            <template #default="{ row }">
              <div class="table-action-cell">
                <div class="table-action-panel" :class="{ 'is-collapsed': profileActionCollapsed }">
                  <el-button text type="warning" @click="openProfileDialog(row)">修改</el-button>
                  <el-button text :type="row.status === 'ACTIVE' ? 'danger' : 'success'" @click="toggleProfileStatus(row)">
                    {{ row.status === 'ACTIVE' ? '停用' : '启用' }}
                  </el-button>
                  <el-button text type="primary" @click="goVolunteerActivity(row)">排班</el-button>
                </div>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <div class="user-admin-pagination">
          <el-pagination layout="prev, pager, next, total" :current-page="profilePage.page" :page-size="profilePage.size" :total="profileTotal" @current-change="changeProfilePage" />
        </div>
      </section>

      <section v-else-if="activeSection === 'rewards'" class="pet-admin-section">
        <section class="filter-panel pet-directory-filter-panel">
          <div class="pet-filter-row pet-filter-row-primary volunteer-filter-row-primary">
            <el-select v-model="rewardSearch.status" clearable placeholder="激励状态">
              <el-option v-for="item in rewardStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
            <el-select v-model="rewardSearch.type" clearable placeholder="激励类型">
              <el-option v-for="item in rewardTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
            <el-date-picker
              v-model="rewardSearch.timeRange"
              type="daterange"
              value-format="YYYY-MM-DD HH:mm:ss"
              start-placeholder="开始时间"
              end-placeholder="结束时间"
              class="full-width-control"
            />
            <div class="pet-filter-action">
              <el-button v-if="isWorker" class="soft-btn" :icon="Plus" @click="openRewardDialog">新增激励</el-button>
              <el-button class="warm-btn" :icon="Search" :loading="rewardLoading" @click="searchRewards">搜索</el-button>
            </div>
          </div>
        </section>

        <el-table :data="rewardRows" v-loading="rewardLoading" class="user-admin-table">
          <el-table-column label="志愿者" min-width="160">
            <template #default="{ row }">
              <button class="table-primary-link" type="button" @click="goVolunteerActivity(row)">{{ row.volunteerName || '未命名' }}</button>
            </template>
          </el-table-column>
          <el-table-column label="统计周期" min-width="200">
            <template #default="{ row }">{{ formatDate(row.periodStart) }} - {{ formatDate(row.periodEnd) }}</template>
          </el-table-column>
          <el-table-column label="类型" width="120">
            <template #default="{ row }">{{ rewardTypeText(row.rewardType) }}</template>
          </el-table-column>
          <el-table-column label="内容" min-width="160" show-overflow-tooltip>
            <template #default="{ row }">{{ row.rewardValue || row.rewardReason || '待补充' }}</template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="tagTypeByStatus(row.status)" effect="plain">{{ rewardStatusText(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column width="40" class-name="action-col">
            <template #header>
              <TableActionColumnHeader title="操作" :collapsed="rewardActionCollapsed" @toggle="rewardActionCollapsed = !rewardActionCollapsed" />
            </template>
            <template #default="{ row }">
              <div class="table-action-cell">
                <div class="table-action-panel" :class="{ 'is-collapsed': rewardActionCollapsed }">
                  <el-button v-if="isWorker && row.status === 'PENDING'" text type="primary" @click="issueReward(row)">发放</el-button>
                  <span v-else class="volunteer-static-action table-action-cell-text">查看记录</span>
                </div>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <div class="user-admin-pagination">
          <el-pagination layout="prev, pager, next, total" :current-page="rewardPage.page" :page-size="rewardPage.size" :total="rewardTotal" @current-change="changeRewardPage" />
        </div>
      </section>

      <section v-else-if="activeSection === 'activities'" class="volunteer-activity-stack">
        <div class="volunteer-activity-block">
          <div class="volunteer-block-head">
            <strong>志愿活动排班</strong>
            <span>排班安排与确认</span>
          </div>
          <section class="filter-panel pet-directory-filter-panel">
            <div class="pet-filter-row pet-filter-row-primary volunteer-filter-row-primary">
              <el-select v-model="shiftSearch.status" clearable placeholder="排班状态">
                <el-option v-for="item in shiftStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
              <el-select v-model="shiftSearch.taskType" clearable placeholder="任务类型">
                <el-option v-for="item in taskTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
              <el-date-picker
                v-model="shiftSearch.timeRange"
                type="daterange"
                value-format="YYYY-MM-DD HH:mm:ss"
                start-placeholder="开始时间"
                end-placeholder="结束时间"
                class="full-width-control"
              />
              <div class="pet-filter-action">
                <el-button v-if="isWorker" class="soft-btn" :icon="Plus" @click="openShiftDialog()">安排排班</el-button>
                <el-button class="warm-btn" :icon="Search" :loading="shiftLoading" @click="searchShifts">搜索</el-button>
              </div>
            </div>
          </section>

          <el-table :data="shiftRows" v-loading="shiftLoading" class="user-admin-table">
            <el-table-column label="活动标题" min-width="140" show-overflow-tooltip>
              <template #default="{ row }">
                <button class="table-primary-link" type="button" @click="goShiftPage(row)">{{ row.title || '未命名活动' }}</button>
              </template>
            </el-table-column>
            <el-table-column label="志愿者" min-width="100">
              <template #default="{ row }">{{ row.volunteerName || '待指派' }}</template>
            </el-table-column>
            <el-table-column label="类型" width="100">
              <template #default="{ row }">{{ taskTypeText(row.taskType) }}</template>
            </el-table-column>
            <el-table-column label="时间" min-width="150">
              <template #default="{ row }">{{ formatDate(row.startTime, true) }} - {{ formatDate(row.endTime, true) }}</template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="tagTypeByStatus(row.status)" effect="plain">{{ shiftStatusText(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="地点" min-width="160" show-overflow-tooltip>
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

          <section class="filter-panel pet-directory-filter-panel">
            <div class="pet-filter-row pet-filter-row-primary volunteer-filter-row-primary">
              <el-select v-model="recordSearch.status" clearable placeholder="报告状态">
                <el-option v-for="item in recordStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
              <el-date-picker
                v-model="recordSearch.timeRange"
                type="daterange"
                value-format="YYYY-MM-DD HH:mm:ss"
                start-placeholder="开始时间"
                end-placeholder="结束时间"
                class="full-width-control"
              />
              <div class="pet-filter-action">
                <el-button class="warm-btn" :icon="Search" :loading="recordLoading" @click="searchRecords">搜索</el-button>
              </div>
            </div>
          </section>

          <el-table :data="recordRows" v-loading="recordLoading" class="user-admin-table">
            <el-table-column label="排班标题" min-width="140" show-overflow-tooltip>
              <template #default="{ row }">
                <button class="table-primary-link" type="button" @click="goRecordShift(row)">{{ row.shiftTitle || '未命名排班' }}</button>
              </template>
            </el-table-column>
            <el-table-column label="志愿者" min-width="100">
              <template #default="{ row }">{{ row.volunteerName || '未命名' }}</template>
            </el-table-column>
            <el-table-column prop="summary" label="服务摘要" min-width="160" show-overflow-tooltip />
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="tagTypeByStatus(row.status)" effect="plain">{{ recordStatusText(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="审核意见" min-width="140" show-overflow-tooltip>
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
    </div>
  </el-card>

  <el-dialog v-model="recruitmentDialogVisible" :title="recruitmentForm.id ? '编辑招募计划' : '新增招募计划'" width="760px">
    <el-form ref="recruitmentFormRef" :model="recruitmentForm" :rules="recruitmentRules" label-position="top" class="pet-admin-form">
      <el-form-item label="招募标题" prop="title" class="pet-admin-span-2">
        <el-input v-model="recruitmentForm.title" placeholder="请输入招募标题" />
      </el-form-item>
      <el-form-item label="招募人数" prop="headcount">
        <el-input-number v-model="recruitmentForm.headcount" :min="1" :controls="false" class="full-width-control" />
      </el-form-item>
      <el-form-item label="招募时间" prop="timeRange">
        <el-date-picker
          v-model="recruitmentForm.timeRange"
          type="datetimerange"
          value-format="YYYY-MM-DD HH:mm:ss"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
          class="full-width-control"
        />
      </el-form-item>
      <el-form-item label="省份" prop="province">
        <el-select v-model="recruitmentForm.province" filterable clearable placeholder="省份" @change="handleRecruitmentFormProvinceChange">
          <el-option v-for="item in provinceOptions" :key="item" :label="item" :value="item" />
        </el-select>
      </el-form-item>
      <el-form-item label="城市" prop="city">
        <el-select v-model="recruitmentForm.city" filterable clearable placeholder="城市" :disabled="!recruitmentForm.province" @change="handleRecruitmentFormCityChange">
          <el-option v-for="item in recruitmentFormCityOptions" :key="item" :label="item" :value="item" />
        </el-select>
      </el-form-item>
      <el-form-item label="区县" prop="district">
        <el-select v-model="recruitmentForm.district" filterable clearable placeholder="区县" :disabled="!recruitmentForm.city">
          <el-option v-for="item in recruitmentFormDistrictOptions" :key="item" :label="item" :value="item" />
        </el-select>
      </el-form-item>
      <el-form-item label="详细地点" prop="detailAddress" class="pet-admin-span-2">
        <el-input v-model="recruitmentForm.detailAddress" placeholder="请输入服务地点" />
      </el-form-item>
      <el-form-item label="招募说明" prop="description" class="pet-admin-span-2">
        <el-input v-model="recruitmentForm.description" type="textarea" :autosize="{ minRows: 4, maxRows: 7 }" />
      </el-form-item>
      <el-form-item label="参与要求" prop="requirement" class="pet-admin-span-2">
        <el-input v-model="recruitmentForm.requirement" type="textarea" :autosize="{ minRows: 4, maxRows: 7 }" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="recruitmentDialogVisible = false">取消</el-button>
      <el-button type="warning" :loading="savingRecruitment" @click="saveRecruitment">保存</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="recruitmentStatusDialogVisible" title="修改招募状态" width="520px">
    <el-form label-position="top">
      <el-form-item label="目标状态">
        <el-input :model-value="recruitmentStatusText(recruitmentStatusForm.status)" disabled />
      </el-form-item>
      <el-form-item :label="recruitmentStatusForm.status === 'CLOSED' ? (recruitmentStatusForm.autoReason ? '关闭备注' : '关闭原因') : '状态备注'" required>
        <el-input
          v-model="recruitmentStatusForm.reason"
          type="textarea"
          :autosize="{ minRows: 4, maxRows: 7 }"
          :disabled="recruitmentStatusForm.autoReason"
          :placeholder="recruitmentStatusForm.status === 'CLOSED' ? (recruitmentStatusForm.autoReason ? '关闭时间会自动保存' : '请填写关闭原因') : '请填写状态变更说明'"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="recruitmentStatusDialogVisible = false">取消</el-button>
      <el-button type="warning" :loading="savingRecruitment" @click="saveRecruitmentStatus">保存</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="applicationReviewDialogVisible" title="审核志愿申请" width="520px">
    <el-form ref="applicationReviewFormRef" :model="applicationReviewForm" :rules="applicationReviewRules" label-position="top">
      <el-form-item label="申请状态" prop="status">
        <el-select v-model="applicationReviewForm.status" placeholder="请选择状态">
          <el-option v-for="item in applicationReviewStatusOptions()" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="审核意见" prop="reason">
        <el-input v-model="applicationReviewForm.reason" type="textarea" :autosize="{ minRows: 4, maxRows: 7 }" placeholder="请填写通过、拒绝或进入审核的说明" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="applicationReviewDialogVisible = false">取消</el-button>
      <el-button type="warning" :loading="savingApplicationReview" @click="saveApplicationReview">保存</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="profileDialogVisible" title="修改志愿者档案" width="760px">
    <el-form ref="profileFormRef" :model="profileForm" :rules="profileRules" label-position="top" class="pet-admin-form">
      <el-form-item label="真实姓名" prop="realName">
        <el-input v-model="profileForm.realName" placeholder="请输入真实姓名" />
      </el-form-item>
      <el-form-item label="性别" prop="sex">
        <el-select v-model="profileForm.sex" placeholder="请选择性别">
          <el-option label="未知" value="未知" />
          <el-option label="男" value="男" />
          <el-option label="女" value="女" />
        </el-select>
      </el-form-item>
      <el-form-item label="联系电话" prop="phone">
        <el-input v-model="profileForm.phone" placeholder="请输入联系电话" />
      </el-form-item>
      <el-form-item label="省份" prop="province">
        <el-select v-model="profileForm.province" filterable clearable placeholder="省份" @change="handleProfileFormProvinceChange">
          <el-option v-for="item in provinceOptions" :key="item" :label="item" :value="item" />
        </el-select>
      </el-form-item>
      <el-form-item label="城市" prop="city">
        <el-select v-model="profileForm.city" filterable clearable placeholder="城市" :disabled="!profileForm.province" @change="handleProfileFormCityChange">
          <el-option v-for="item in profileFormCityOptions" :key="item" :label="item" :value="item" />
        </el-select>
      </el-form-item>
      <el-form-item label="区县" prop="district">
        <el-select v-model="profileForm.district" filterable clearable placeholder="区县" :disabled="!profileForm.city">
          <el-option v-for="item in profileFormDistrictOptions" :key="item" :label="item" :value="item" />
        </el-select>
      </el-form-item>
      <el-form-item label="详细地点" prop="detailAddress" class="pet-admin-span-2">
        <el-input v-model="profileForm.detailAddress" placeholder="请输入详细地点" />
      </el-form-item>
      <el-form-item label="技能说明" class="pet-admin-span-2">
        <el-input v-model="profileForm.skills" type="textarea" :autosize="{ minRows: 3, maxRows: 6 }" />
      </el-form-item>
      <el-form-item label="服务意向" class="pet-admin-span-2">
        <el-input v-model="profileForm.serviceIntention" type="textarea" :autosize="{ minRows: 3, maxRows: 6 }" />
      </el-form-item>
      <el-form-item label="可服务时间" class="pet-admin-span-2">
        <el-input v-model="profileForm.availableTimeDesc" type="textarea" :autosize="{ minRows: 3, maxRows: 6 }" />
      </el-form-item>
      <el-form-item label="备注" class="pet-admin-span-2">
        <el-input v-model="profileForm.remark" type="textarea" :autosize="{ minRows: 3, maxRows: 6 }" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="profileDialogVisible = false">取消</el-button>
      <el-button type="warning" :loading="savingProfile" @click="saveProfile">保存</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="rewardDialogVisible" title="新增志愿者激励" width="720px">
    <el-form ref="rewardFormRef" :model="rewardForm" :rules="rewardRules" label-position="top" class="pet-admin-form">
      <el-form-item label="志愿者" prop="volunteerId">
        <el-select v-model="rewardForm.volunteerId" placeholder="选择志愿者" filterable :loading="volunteerOptionsLoading">
          <el-option v-for="item in volunteerOptions" :key="item.userId" :label="`${item.realName || item.username} · ${item.phone || '无电话'}`" :value="item.userId" />
        </el-select>
      </el-form-item>
      <el-form-item label="激励类型" prop="rewardType">
        <el-select v-model="rewardForm.rewardType" placeholder="选择激励类型">
          <el-option v-for="item in rewardTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="统计周期" prop="periodRange">
        <el-date-picker
          v-model="rewardForm.periodRange"
          type="datetimerange"
          value-format="YYYY-MM-DD HH:mm:ss"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
          class="full-width-control"
        />
      </el-form-item>
      <el-form-item label="服务次数">
        <el-input-number v-model="rewardForm.serviceCount" :min="0" :controls="false" class="full-width-control" />
      </el-form-item>
      <el-form-item label="累计服务时长">
        <el-input-number v-model="rewardForm.totalHours" :min="0" :precision="1" :step="0.5" :controls="false" class="full-width-control" />
      </el-form-item>
      <el-form-item label="激励内容">
        <el-input v-model="rewardForm.rewardValue" placeholder="例如：50 积分、证书、猫砂礼包" />
      </el-form-item>
      <el-form-item label="激励原因" prop="rewardReason" class="pet-admin-span-2">
        <el-input v-model="rewardForm.rewardReason" type="textarea" :autosize="{ minRows: 4, maxRows: 7 }" />
      </el-form-item>
      <el-form-item label="备注" class="pet-admin-span-2">
        <el-input v-model="rewardForm.remark" type="textarea" :autosize="{ minRows: 3, maxRows: 6 }" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="rewardDialogVisible = false">取消</el-button>
      <el-button type="warning" :loading="savingReward" @click="saveReward">保存</el-button>
    </template>
  </el-dialog>

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
        <el-input v-model="shiftForm.title" placeholder="例如：周末巡护、开放日接待、运输协助" :disabled="Boolean(shiftForm.id)" />
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
        <el-input v-model="shiftStatusForm.reason" type="textarea" :autosize="{ minRows: 4, maxRows: 7 }" placeholder="例如：志愿者缺勤、活动结束、临时取消" />
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
        <el-input v-model="recordForm.summary" placeholder="一句话概括本次服务情况" />
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
