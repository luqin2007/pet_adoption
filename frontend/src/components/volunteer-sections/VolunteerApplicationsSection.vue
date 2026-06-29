<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, RefreshRight } from '@element-plus/icons-vue'
import { getVolunteerApplications, updateVolunteerApplicationStatus } from '../../api/volunteer'
import { useInformationCatalog } from '../../composables/useInformationCatalog'
import { useTableFilters } from '../../composables/useTableFilters'
import { useUserStore } from '../../stores/user'
import TableFilterHeader from '../TableFilterHeader.vue'
import TableActionColumnHeader from '../TableActionColumnHeader.vue'
import { useRoute, useRouter } from 'vue-router'
import { ROLE, hasRole } from '../../utils/roles'

const props = defineProps({
  section: { type: String, default: 'applications' },
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

const applicationStatusOptions = [
  { label: '已提交', value: 'SUBMITTED' },
  { label: '审核中', value: 'UNDER_REVIEW' },
  { label: '已通过', value: 'APPROVED' },
  { label: '已拒绝', value: 'REJECTED' },
  { label: '已撤回', value: 'CANCELED' },
]

function applicationStatusText(value) {
  return applicationStatusOptions.find((item) => item.value === value)?.label || value || '待补充'
}

function goApplicationDetail(row) {
  if (row?.id) router.push(`/console/volunteer/applications/${row.id}`)
}

const applicationLoading = ref(false)
const applicationActionCollapsed = ref(false)
const applicationRows = ref([])
const applicationTotal = ref(0)

const applicationPage = reactive({ page: 1, size: 10 })

const applicationSearch = reactive({
  status: '',
  province: '',
  city: '',
})

const applicationReviewDialogVisible = ref(false)
const applicationReviewFormRef = ref()
const savingApplicationReview = ref(false)

const applicationReviewForm = reactive({
  id: '',
  status: '',
  reason: '',
})

const applicationReviewRules = {
  status: [{ required: true, message: '请选择审核状态', trigger: 'change' }],
  reason: [{ required: true, message: '请填写审核说明', trigger: 'blur' }],
}

const { filters: applicationFilters, isActive: isApplicationFilterActive, applyFilter: applyApplicationFilter } = useTableFilters({
  plan: { type: 'text' },
  applicant: { type: 'text' },
  region: { type: 'text' },
  status: { type: 'enum' },
  review: { type: 'text' },
})

const filteredApplications = computed(() => applyApplicationFilter(applicationRows.value || []))

const {
  ensureInformationCatalog,
  ensureCityOptions,
  provinceOptions,
  getCityOptions,
} = useInformationCatalog()

const applicationCityOptions = computed(() => getCityOptions(applicationSearch.province))

function handleApplicationProvinceChange() {
  applicationSearch.city = ''
  if (applicationSearch.province) {
    ensureCityOptions(applicationSearch.province)
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

function handlePlusClick() {
  router.push('/volunteers/recruitments')
}

function searchCurrentSection() {
  searchApplications()
}

defineExpose({ handlePlusClick, searchCurrentSection })

onMounted(async () => {
  await ensureInformationCatalog()
  await loadApplications()
})
</script>

<template>
  <section class="pet-admin-section" v-loading="applicationLoading">
    <el-table :data="filteredApplications" class="user-admin-table">
      <el-table-column min-width="220" show-overflow-tooltip>
        <template #header>
          <TableFilterHeader label="招募计划" :filter="applicationFilters.plan" type="text" :active="isApplicationFilterActive('plan')" />
        </template>
        <template #default="{ row }">
          <button class="table-primary-link" type="button" @click="goApplicationDetail(row)">{{ row.recruitmentTitle || '未命名招募' }}</button>
        </template>
      </el-table-column>
      <el-table-column min-width="160">
        <template #header>
          <TableFilterHeader label="申请人" :filter="applicationFilters.applicant" type="text" :active="isApplicationFilterActive('applicant')" />
        </template>
        <template #default="{ row }">
          <div class="volunteer-person-cell">
            <strong>{{ row.realName || row.username || '未命名' }}</strong>
            <span>{{ row.phone || '联系方式待补充' }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column min-width="180">
        <template #header>
          <TableFilterHeader label="地区" :filter="applicationFilters.region" type="text" :active="isApplicationFilterActive('region')" />
        </template>
        <template #default="{ row }">{{ locationText(row) }}</template>
      </el-table-column>
      <el-table-column width="110">
        <template #header>
          <TableFilterHeader label="状态" :filter="applicationFilters.status" type="enum" :options="applicationStatusOptions" :active="isApplicationFilterActive('status')" />
        </template>
        <template #default="{ row }">
          <el-tag :type="tagTypeByStatus(row.status)" effect="plain">{{ applicationStatusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column min-width="200" show-overflow-tooltip>
        <template #header>
          <TableFilterHeader label="审核意见" :filter="applicationFilters.review" type="text" :active="isApplicationFilterActive('review')" />
        </template>
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

  <el-dialog v-model="applicationReviewDialogVisible" title="审核志愿申请" width="520px">
    <el-form ref="applicationReviewFormRef" :model="applicationReviewForm" :rules="applicationReviewRules" label-position="top">
      <el-form-item label="申请状态" prop="status">
        <el-select v-model="applicationReviewForm.status" placeholder="请选择状态">
          <el-option v-for="item in applicationReviewStatusOptions()" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="审核意见" prop="reason">
        <el-input v-model="applicationReviewForm.reason" type="textarea" :autosize="{ minRows: 4, maxRows: 7 }" placeholder="审核的说明" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="applicationReviewDialogVisible = false">取消</el-button>
      <el-button type="warning" :loading="savingApplicationReview" @click="saveApplicationReview">保存</el-button>
    </template>
  </el-dialog>
</template>
