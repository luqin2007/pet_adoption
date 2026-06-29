<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, RefreshRight } from '@element-plus/icons-vue'
import {
  createRecruitment,
  getRecruitments,
  getVolunteerProfiles,
  updateRecruitment,
  updateRecruitmentStatus,
} from '../../api/volunteer'
import { useInformationCatalog } from '../../composables/useInformationCatalog'
import { useTableFilters } from '../../composables/useTableFilters'
import { useUserStore } from '../../stores/user'
import TableFilterHeader from '../TableFilterHeader.vue'
import TableActionColumnHeader from '../TableActionColumnHeader.vue'
import { useRoute, useRouter } from 'vue-router'
import { ROLE, hasRole } from '../../utils/roles'

const props = defineProps({
  section: { type: String, default: 'recruitments' },
  hideTabs: { type: Boolean, default: false },
  allowedSections: { type: Array, default: null },
})

const emit = defineEmits(['update:section'])

const userStore = useUserStore()
const router = useRouter()
const route = useRoute()

const loginRole = computed(() => Number(userStore.profile.role || 0))
const isAdmin = computed(() => hasRole(loginRole.value, ROLE.ADMIN))
const isWorker = computed(() => isAdmin.value || hasRole(loginRole.value, ROLE.WORKER))
const isVolunteer = computed(() => hasRole(loginRole.value, ROLE.VOLUNTEER))
const loginUserId = computed(() => String(userStore.profile.id || ''))

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

const recruitmentStatusOptions = [
  { label: '草稿', value: 'DRAFT' },
  { label: '招募中', value: 'PUBLISHED' },
  { label: '已关闭', value: 'CLOSED' },
]

function recruitmentStatusText(value) {
  return recruitmentStatusOptions.find((item) => item.value === value)?.label || value || '待补充'
}

function tagTypeByStatus(value) {
  if (['APPROVED', 'ACTIVE', 'ISSUED', 'COMPLETED'].includes(value)) return 'success'
  if (['UNDER_REVIEW', 'CONFIRMED', 'IN_PROGRESS'].includes(value)) return 'primary'
  if (['REJECTED', 'CANCELED', 'DISABLED', 'CANCELLED', 'ABSENT'].includes(value)) return 'info'
  return 'warning'
}

function locationText(row) {
  return [row.province, row.city, row.district, row.serviceAddress || row.address || row.detailAddress].filter(Boolean).join(' · ') || '位置待补充'
}

function goRecruitmentDetail(id) {
  if (id) router.push(`/volunteers/recruitments/${id}`)
}

const recruitmentLoading = ref(false)
const recruitmentActionCollapsed = ref(false)
const recruitmentRows = ref([])
const recruitmentTotal = ref(0)
const recruitmentDialogVisible = ref(false)
const recruitmentStatusDialogVisible = ref(false)
const recruitmentFormRef = ref()
const savingRecruitment = ref(false)
const volunteerOptions = ref([])
const volunteerOptionsLoading = ref(false)

const recruitmentPage = reactive({ page: 1, size: 10 })

const recruitmentSearch = reactive({
  title: '',
  province: '',
  city: '',
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

const { filters: recruitmentFilters, isActive: isRecruitmentFilterActive, applyFilter: applyRecruitmentFilter } = useTableFilters({
  title: { type: 'text' },
  location: { type: 'text' },
  time: { type: 'time' },
  status: { type: 'enum' },
  applied: { type: 'text' },
})

const filteredRecruitments = computed(() => applyRecruitmentFilter(recruitmentRows.value || []))

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

function handleRecruitmentSearchProvinceChange() {
  recruitmentSearch.city = ''
  if (recruitmentSearch.province) {
    ensureCityOptions(recruitmentSearch.province)
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

function handlePlusClick() {
  openRecruitmentDialog()
}

function searchCurrentSection() {
  searchRecruitments()
}

defineExpose({ handlePlusClick, searchCurrentSection })

onMounted(async () => {
  await ensureInformationCatalog()
  await loadRecruitmentsData()
})
</script>

<template>
  <section class="pet-admin-section">
    <el-table :data="filteredRecruitments" v-loading="recruitmentLoading" class="user-admin-table">
      <el-table-column min-width="220" show-overflow-tooltip>
        <template #header>
          <TableFilterHeader label="招募标题" :filter="recruitmentFilters.title" type="text" :active="isRecruitmentFilterActive('title')" />
        </template>
        <template #default="{ row }">
          <button class="table-primary-link" type="button" @click="goRecruitmentDetail(row.id)">{{ row.title || '未命名招募' }}</button>
        </template>
      </el-table-column>
      <el-table-column min-width="220">
        <template #header>
          <TableFilterHeader label="地点" :filter="recruitmentFilters.location" type="text" :active="isRecruitmentFilterActive('location')" />
        </template>
        <template #default="{ row }">{{ locationText(row) }}</template>
      </el-table-column>
      <el-table-column min-width="180">
        <template #header>
          <TableFilterHeader label="时间" :filter="recruitmentFilters.time" type="time" :active="isRecruitmentFilterActive('time')" />
        </template>
        <template #default="{ row }">{{ formatDate(row.startTime) }} - {{ formatDate(row.endTime) }}</template>
      </el-table-column>
      <el-table-column width="100">
        <template #header>
          <TableFilterHeader label="状态" :filter="recruitmentFilters.status" type="enum" :options="recruitmentStatusOptions" :active="isRecruitmentFilterActive('status')" />
        </template>
        <template #default="{ row }">
          <el-tag :type="tagTypeByStatus(row.status)" effect="plain">{{ recruitmentStatusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column width="100">
        <template #header>
          <TableFilterHeader label="报名" :filter="recruitmentFilters.applied" type="text" :active="isRecruitmentFilterActive('applied')" />
        </template>
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
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="recruitmentStatusDialogVisible = false">取消</el-button>
      <el-button type="warning" :loading="savingRecruitment" @click="saveRecruitmentStatus">保存</el-button>
    </template>
  </el-dialog>
</template>
