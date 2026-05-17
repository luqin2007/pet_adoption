<template>
  <el-card class="profile-card pet-admin-card">
    <template #header>
      <div class="profile-card-header">
        <strong>初诊登记</strong>
        <span>记录首次检查结果</span>
        <div class="profile-actions">
          <el-button class="warm-btn" :icon="RefreshRight" :loading="loadingFirstReg" @click="loadFirstRegistrations">刷新</el-button>
        </div>
      </div>
    </template>
    <section class="pet-admin-section">
      <el-table :data="displayedFirstRegs" v-loading="loadingFirstReg" class="user-admin-table">
        <el-table-column min-width="120" show-overflow-tooltip>
          <template #header><TableFilterHeader label="宠物名称" :filter="filters.petName" type="text" :active="isActive('petName')" placeholder="搜索宠物名称…" /></template>
          <template #default="{ row }">
            <button class="table-primary-link" type="button" @click="goFirstRegistrationDetail(row)">{{ row.name || '未命名' }}</button>
          </template>
        </el-table-column>
        <el-table-column label="年龄" width="80">
          <template #default="{ row }">{{ row.age }} 月</template>
        </el-table-column>
        <el-table-column label="类型" width="80">
          <template #default="{ row }">{{ row.type || '—' }}</template>
        </el-table-column>
        <el-table-column label="性别" width="70">
          <template #default="{ row }">{{ row.sex || '—' }}</template>
        </el-table-column>
        <el-table-column label="接诊人" min-width="120">
          <template #default="{ row }">{{ row.username || '—' }}</template>
        </el-table-column>
        <el-table-column label="登记时间" min-width="160">
          <template #default="{ row }">
            {{ row.createTime ? new Date(row.createTime).toLocaleString('zh-CN') : '—' }}
          </template>
        </el-table-column>
        <el-table-column width="40" class-name="action-col">
          <template #header><TableActionColumnHeader title="操作" :collapsed="actionCollapsed" @toggle="actionCollapsed = !actionCollapsed" /></template>
          <template #default="{ row }">
            <div class="table-action-cell">
              <div class="table-action-panel" :class="{ 'is-collapsed': actionCollapsed }">
                <el-button v-if="canManageMedical" text type="primary" @click="goTreatment(row)">就诊</el-button>
                <el-button v-if="isDoctor" text type="success" @click="goCaseList(row)">病历</el-button>
                <el-button v-if="isDoctor" text type="warning" @click="goRehab(row)">康复</el-button>
              </div>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <div class="user-admin-pagination">
        <el-pagination
          layout="prev, pager, next, total"
          :current-page="firstRegPage.page"
          :page-size="firstRegPage.size"
          :total="firstRegTotal"
          @current-change="(p) => firstRegPage.page = p"
        />
      </div>
    </section>
  </el-card>

  <MedicalRecordCreateDialog
    v-model="createDialogVisible"
    :pet-id="createDialogPetId"
    :pet-name="createDialogPetName"
    :pet-age="createDialogPetAge"
    @created="onRecordCreated"
  />
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { RefreshRight } from '@element-plus/icons-vue'
import { getFirstVisitRegistrations, getMedicalRecords, getMedicalDetails, getRehabPlans } from '../api/services'
import { useConsoleGuards } from '../composables/useConsoleGuards'
import { useTableFilters } from '../composables/useTableFilters'
import { ROLE, hasRole } from '../utils/roles'
import TableActionColumnHeader from './TableActionColumnHeader.vue'
import TableFilterHeader from './TableFilterHeader.vue'
import MedicalRecordCreateDialog from './MedicalRecordCreateDialog.vue'

const router = useRouter()
const { canManageMedical, loginRole } = useConsoleGuards()

const isDoctor = computed(() => hasRole(loginRole.value, ROLE.DOCTOR))

const actionCollapsed = ref(false)
const createDialogVisible = ref(false)
const createDialogPetId = ref('')
const createDialogPetName = ref('')
const createDialogPetAge = ref(null)

const loadingFirstReg = ref(false)
const firstRegRows = ref([])
const firstRegPage = reactive({ page: 1, size: 10 })

const { filters, isActive, applyFilter } = useTableFilters({
  petName: { type: 'text' },
  status: { type: 'enum' },
  createTime: { type: 'time' },
})

const statusOptions = [
  { label: '待接诊', value: 'WAITING' },
  { label: '接诊中', value: 'PROCESSING' },
  { label: '待缴费', value: 'PAYING' },
  { label: '完成', value: 'COMPLETED' },
  { label: '取消', value: 'CANCELED' },
]

const filteredFirstRegs = computed(() => applyFilter(firstRegRows.value || []))

const displayedFirstRegs = computed(() => {
  const start = (firstRegPage.page - 1) * firstRegPage.size
  return filteredFirstRegs.value.slice(start, start + firstRegPage.size)
})

const firstRegTotal = computed(() => filteredFirstRegs.value.length)

async function loadFirstRegistrations() {
  loadingFirstReg.value = true
  try {
    const result = await getFirstVisitRegistrations({ page: 1, size: 500, sort: 'create_time', order: 'desc' })
    firstRegRows.value = Array.isArray(result?.records) ? result.records : []
  } catch (error) {
    ElMessage.warning(error?.message || '加载初诊登记失败')
  } finally {
    loadingFirstReg.value = false
  }
}

function openCreateDialog(row) {
  createDialogPetId.value = row.petId || ''
  createDialogPetName.value = row.name || ''
  createDialogPetAge.value = row.age ?? null
  createDialogVisible.value = true
}

function onRecordCreated() {
  loadFirstRegistrations()
}

function goFirstRegistrationDetail(row) {
  if (row?.id) router.push(`/medical/first/${row.id}`)
}

function goCaseList(row) {
  if (row?.petId) router.push(`/console/medical/detail-list?pet=${row.petId}&name=${encodeURIComponent(row.name || '')}`)
}

async function goTreatment(row) {
  const petId = row.petId
  if (!petId) {
    ElMessage.warning('无法找到宠物信息')
    return
  }

  try {
    const res = await getMedicalRecords({ pet: petId, page: 1, size: 1, sort: 'create_time', order: 'desc' })
    const latestRecord = res?.records?.[0]

    if (!latestRecord) {
      openCreateDialog(row)
      return
    }

    const { status, id: recordId } = latestRecord

    if (status === 'COMPLETED' || status === 'CANCELED') {
      openCreateDialog(row)
      return
    }

    if (status === 'WAITING' && isDoctor.value) {
      router.push(`/medical/detail/new?recordId=${recordId}`)
      return
    }

    if (status === 'PROCESSING' && isDoctor.value) {
      const detailRes = await getMedicalDetails({ record: [recordId], page: 1, size: 1 })
      const detail = detailRes?.records?.[0]
      if (detail?.id) {
        router.push(`/medical/detail/${detail.id}`)
      } else {
        router.push(`/medical/detail/new?recordId=${recordId}`)
      }
      return
    }

    const detailRes = await getMedicalDetails({ record: [recordId], page: 1, size: 1 })
    const detail = detailRes?.records?.[0]
    if (detail?.id) {
      router.push(`/medical/detail/${detail.id}`)
    } else {
      ElMessage.info('该就诊记录暂无病历详情')
    }
  } catch (error) {
    ElMessage.warning(error?.message || '获取就诊信息失败')
  }
}

async function goRehab(row) {
  const petId = row.petId
  if (!petId) {
    ElMessage.warning('无法找到宠物信息')
    return
  }

  try {
    const res = await getRehabPlans({ pet: [petId], page: 1, size: 1, sort: 'create_time', order: 'desc' })
    const latestPlan = res?.records?.[0]
    if (latestPlan?.id) {
      router.push(`/console/medical/rehab/${latestPlan.id}`)
      return
    }
    router.push({
      path: '/console/medical/rehab/new',
      query: {
        pet: petId,
        name: row.name || '',
        age: row.age ?? 0,
      },
    })
  } catch (error) {
    ElMessage.warning(error?.message || '查找康复计划失败')
  }
}

onMounted(() => {
  loadFirstRegistrations()
})
</script>

<style scoped>
.medical-first-cols-search {
  grid-template-columns: 1fr 1fr auto;
}
</style>
