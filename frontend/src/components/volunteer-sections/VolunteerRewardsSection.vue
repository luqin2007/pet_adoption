<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, RefreshRight } from '@element-plus/icons-vue'
import { createVolunteerReward, getVolunteerProfiles, getVolunteerRewards, getVolunteerServiceRecords, issueVolunteerReward } from '../../api/volunteer'
import { useTableFilters } from '../../composables/useTableFilters'
import { useUserStore } from '../../stores/user'
import TableFilterHeader from '../TableFilterHeader.vue'
import TableActionColumnHeader from '../TableActionColumnHeader.vue'
import { useRoute, useRouter } from 'vue-router'
import { ROLE, hasRole } from '../../utils/roles'

const props = defineProps({
  section: { type: String, default: 'rewards' },
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

const rewardLoading = ref(false)
const rewardActionCollapsed = ref(false)
const rewardRows = ref([])
const rewardTotal = ref(0)
const rewardDialogVisible = ref(false)
const rewardFormRef = ref()
const savingReward = ref(false)
const volunteerOptions = ref([])
const volunteerOptionsLoading = ref(false)

const rewardPage = reactive({ page: 1, size: 10 })

const rewardSearch = reactive({
  status: '',
  type: '',
  timeRange: [],
})

const rewardForm = reactive({
  volunteerId: '',
  periodRange: [],
  serviceCount: undefined,
  totalHours: undefined,
  rewardValue: '',
  rewardReason: '',
  remark: '',
})

const { filters: rewardFilters, isActive: isRewardFilterActive, applyFilter: applyRewardFilter } = useTableFilters({
  volunteer: { type: 'text' },
  period: { type: 'time' },
  type: { type: 'enum' },
  content: { type: 'text' },
  status: { type: 'enum' },
})

const filteredRewards = computed(() => applyRewardFilter(rewardRows.value || []))

const rewardRules = {
  volunteerId: [{ required: true, message: '请选择志愿者', trigger: 'change' }],
  periodRange: [{ required: true, message: '请选择统计周期', trigger: 'change' }],
  rewardReason: [{ required: true, message: '请输入激励原因', trigger: 'blur' }],
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
  rewardForm.rewardValue = ''
  rewardForm.rewardReason = ''
  rewardForm.remark = ''
}

function openRewardDialog() {
  resetRewardForm()
  rewardDialogVisible.value = true
  loadVolunteerOptions()
}

async function autoCalculateReward() {
  if (!rewardForm.volunteerId || !rewardForm.periodRange?.[0] || !rewardForm.periodRange?.[1]) {
    rewardForm.serviceCount = undefined
    rewardForm.totalHours = undefined
    return
  }
  try {
    const result = await getVolunteerServiceRecords({
      volunteerId: rewardForm.volunteerId,
      time0: rewardForm.periodRange[0],
      time1: rewardForm.periodRange[1],
      size: 999,
    })
    const records = result?.records || result || []
    const count = records.length
    const hours = records.reduce((sum, r) => sum + (Number(r.actualHours) || 0), 0)
    rewardForm.serviceCount = count || undefined
    rewardForm.totalHours = hours || undefined
  } catch {
    rewardForm.serviceCount = undefined
    rewardForm.totalHours = undefined
  }
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

function goVolunteerActivity(row) {
  const volunteerId = row?.volunteerId || row?.userId
  if (!volunteerId) return
  router.push({ name: 'console-volunteer', query: { volunteer: volunteerId } })
}

function handlePlusClick() {
  openRewardDialog()
}

function searchCurrentSection() {
  searchRewards()
}

defineExpose({ handlePlusClick, searchCurrentSection })

onMounted(async () => {
  loadVolunteerOptions()
  loadRewards()
})
</script>

<template>
  <section class="pet-admin-section">
    <el-table :data="filteredRewards" v-loading="rewardLoading" class="user-admin-table">
      <el-table-column min-width="160">
        <template #header>
          <TableFilterHeader label="志愿者" :filter="rewardFilters.volunteer" type="text" :active="isRewardFilterActive('volunteer')" />
        </template>
        <template #default="{ row }">
          <button class="table-primary-link" type="button" @click="goVolunteerActivity(row)">{{ row.volunteerName || '未命名' }}</button>
        </template>
      </el-table-column>
      <el-table-column min-width="200">
        <template #header>
          <TableFilterHeader label="统计周期" :filter="rewardFilters.period" type="time" :active="isRewardFilterActive('period')" />
        </template>
        <template #default="{ row }">{{ formatDate(row.periodStart) }} - {{ formatDate(row.periodEnd) }}</template>
      </el-table-column>
      <el-table-column width="120">
        <template #header>
          <TableFilterHeader label="类型" :filter="rewardFilters.type" type="enum" :options="rewardTypeOptions" :active="isRewardFilterActive('type')" />
        </template>
        <template #default="{ row }">{{ rewardTypeText(row.rewardType) }}</template>
      </el-table-column>
      <el-table-column min-width="160" show-overflow-tooltip>
        <template #header>
          <TableFilterHeader label="内容" :filter="rewardFilters.content" type="text" :active="isRewardFilterActive('content')" />
        </template>
        <template #default="{ row }">{{ row.rewardValue || row.rewardReason || '待补充' }}</template>
      </el-table-column>
      <el-table-column width="100">
        <template #header>
          <TableFilterHeader label="状态" :filter="rewardFilters.status" type="enum" :options="rewardStatusOptions" :active="isRewardFilterActive('status')" />
        </template>
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

  <el-dialog v-model="rewardDialogVisible" title="新增志愿者激励" width="720px">
    <el-form ref="rewardFormRef" :model="rewardForm" :rules="rewardRules" label-position="top" class="pet-admin-form">
      <el-form-item label="志愿者" prop="volunteerId">
        <el-select v-model="rewardForm.volunteerId" placeholder="选择志愿者" filterable :loading="volunteerOptionsLoading" @change="autoCalculateReward">
          <el-option v-for="item in volunteerOptions" :key="item.userId" :label="`${item.realName || item.username} · ${item.phone || '无电话'}`" :value="item.userId" />
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
          @change="autoCalculateReward"
        />
      </el-form-item>
      <el-form-item label="服务次数">
        <el-input-number v-model="rewardForm.serviceCount" :min="0" :controls="false" class="full-width-control" />
      </el-form-item>
      <el-form-item label="累计服务时长">
        <el-input-number v-model="rewardForm.totalHours" :min="0" :precision="1" :step="0.5" :controls="false" class="full-width-control" />
      </el-form-item>
      <el-form-item label="激励内容">
        <el-input v-model="rewardForm.rewardValue" />
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
</template>
