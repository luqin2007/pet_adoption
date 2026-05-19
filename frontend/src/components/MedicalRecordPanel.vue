<template>
  <el-card class="profile-card pet-admin-card">
    <template #header>
      <div class="profile-card-header">
        <strong>就诊记录</strong>
        <div class="profile-actions">
          <el-button-group class="console-btn-group">
            <el-button class="warm-btn" :icon="Plus" @click="petPickerVisible = true"/>
            <el-button v-if="canManageMedical && petId" class="soft-btn" :icon="Plus" @click="goCreateRecord" />
            <el-button class="warm-btn" :icon="RefreshRight" :loading="loadingRecords" @click="loadRecords" />
          </el-button-group>
        </div>
      </div>
    </template>
    <section class="pet-admin-section">
      <el-table :data="displayedRecords" v-loading="loadingRecords" class="user-admin-table">
        <el-table-column min-width="120">
          <template #header><TableFilterHeader label="宠物" :filter="filters.petName" type="text" :active="isActive('petName')" /></template>
          <template #default="{ row }">
            <div>
              <button class="pet-admin-name-button" type="button" @click="goMedicalRecordDetail(row)">{{ row.petName || '未命名' }}</button>
              <span style="display:block;font-size:12px;color:var(--muted)">{{ row.petType || '宠物' }} · {{ row.petSex || '未知' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="类型" width="100">
          <template #header><TableFilterHeader label="类型" :filter="filters.type" type="enum" :active="isActive('type')" :options="recordTypeOptions" /></template>
          <template #default="{ row }"><el-tag effect="plain" type="warning">{{ recordTypeText(row.type) }}</el-tag></template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #header><TableFilterHeader label="状态" :filter="filters.status" type="enum" :active="isActive('status')" :options="recordStatusOptions" /></template>
          <template #default="{ row }"><el-tag effect="plain" :type="recordStatusTagType(row.status)">{{ recordStatusText(row.status) }}</el-tag></template>
        </el-table-column>
        <el-table-column label="兽医" min-width="120">
          <template #header><TableFilterHeader label="兽医" :filter="filters.username" type="text" :active="isActive('username')" /></template>
          <template #default="{ row }">{{ row.username || '—' }}</template>
        </el-table-column>
        <el-table-column label="就诊时间" min-width="160">
          <template #header><TableFilterHeader label="就诊时间" :filter="filters.startTime" type="time" :active="isActive('startTime')" /></template>
          <template #default="{ row }">{{ formatDate(row.startTime) }}</template>
        </el-table-column>
        <el-table-column label="创建时间" min-width="160">
          <template #header><TableFilterHeader label="创建时间" :filter="filters.createTime" type="time" :active="isActive('createTime')" /></template>
          <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
        </el-table-column>
        <el-table-column width="40" class-name="action-col">
          <template #header><TableActionColumnHeader title="操作" :collapsed="actionCollapsed" @toggle="actionCollapsed = !actionCollapsed" /></template>
          <template #default="{ row }">
            <div class="table-action-cell">
              <div class="table-action-panel" :class="{ 'is-collapsed': actionCollapsed }">
                <el-button v-if="canEditRecord(row)" text type="warning" @click="openEditDialog(row)">编辑</el-button>
                <el-button v-if="canCancelRecord(row)" text type="danger" @click="cancelRecord(row)">取消</el-button>
                <el-button v-if="canViewMedicalDetail(row)" text type="primary" @click="goMedicalDetail(row)">病历</el-button>
                <el-button text type="primary" @click="goFirstRegistration(row)">初诊</el-button>
              </div>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <div class="user-admin-pagination">
        <el-pagination layout="prev, pager, next, total" :current-page="page.page" :page-size="page.size" :total="total" @current-change="(p) => page.page = p" />
      </div>
    </section>

    <el-dialog v-model="editDialogVisible" title="编辑就诊记录" width="600px">
      <el-form ref="editFormRef" :model="editForm" :rules="editRules" label-position="top" class="pet-admin-form">
        <el-form-item label="就诊类型" prop="type">
          <el-select v-model="editForm.type">
            <el-option v-for="item in recordTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="就诊状态" prop="status">
          <el-select v-model="editForm.status">
            <el-option v-for="item in recordStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始时间">
          <el-date-picker v-model="editForm.startTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss.SSS" placeholder="选择开始时间" class="full-width-control" />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker v-model="editForm.endTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss.SSS" placeholder="选择结束时间" class="full-width-control" />
        </el-form-item>
        <el-form-item label="预计费用">
          <el-input v-model="editForm.price" placeholder="预计费用" />
        </el-form-item>
        <el-form-item label="实际费用">
          <el-input v-model="editForm.cost" placeholder="实际费用" />
        </el-form-item>
        <el-form-item label="领养人电话">
          <el-input v-model="editForm.ownerPhone" placeholder="领养人联系电话" />
        </el-form-item>
      </el-form>
      <div class="dialog-footer">
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="warning" :loading="saving" @click="saveEdit">保存</el-button>
      </div>
    </el-dialog>

    <MedicalRecordCreateDialog
      v-model="createDialogVisible"
      :pet-id="createDialogPetId"
      :pet-name="createDialogPetName"
      :pet-age="createDialogPetAge"
      @created="onRecordCreated"
    />

    <PetPickerDialog v-model:visible="petPickerVisible" @select="onPetSelected" />
  </el-card>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, RefreshRight } from '@element-plus/icons-vue'
import { getMedicalRecords, updateMedicalRecord, getMedicalDetails, getFirstVisitRegistrations } from '../api/services'
import { formatDate } from '../utils/format'
import { useConsoleGuards } from '../composables/useConsoleGuards'
import { useTableFilters } from '../composables/useTableFilters'
import { ROLE, hasRole } from '../utils/roles'
import TableActionColumnHeader from './TableActionColumnHeader.vue'
import TableFilterHeader from './TableFilterHeader.vue'
import MedicalRecordCreateDialog from './MedicalRecordCreateDialog.vue'
import PetPickerDialog from './PetPickerDialog.vue'

const route = useRoute()
const router = useRouter()
const { canManageMedical, loginRole } = useConsoleGuards()

const isDoctor = computed(() => hasRole(loginRole.value, ROLE.DOCTOR))

const petId = computed(() => route.query.pet || '')
const petName = computed(() => route.query.name || '')

const loadingRecords = ref(false)
const saving = ref(false)
const actionCollapsed = ref(false)
const editDialogVisible = ref(false)
const recordRows = ref([])
const page = reactive({ page: 1, size: 10 })
const editFormRef = ref()

const detailIdMap = ref({})

const createDialogVisible = ref(false)
const createDialogPetId = ref('')
const createDialogPetName = ref('')
const createDialogPetAge = ref(null)
const petPickerVisible = ref(false)

const { filters, isActive, applyFilter } = useTableFilters({
  petName: { type: 'text' },
  type: { type: 'enum' },
  status: { type: 'enum' },
  username: { type: 'text' },
  startTime: { type: 'time' },
  createTime: { type: 'time' },
})

const filteredRecords = computed(() => applyFilter(recordRows.value || []))

const displayedRecords = computed(() => {
  const start = (page.page - 1) * page.size
  return filteredRecords.value.slice(start, start + page.size)
})

const total = computed(() => filteredRecords.value.length)

const editForm = reactive({
  id: '', type: '', status: '', startTime: '', endTime: '', price: '', cost: '', ownerPhone: '',
})

const recordStatusOptions = [
  { label: '待接诊', value: 'WAITING' },
  { label: '接诊中', value: 'PROCESSING' },
  { label: '待缴费', value: 'PAYING' },
  { label: '完成', value: 'COMPLETED' },
  { label: '取消', value: 'CANCELED' },
]

const recordTypeOptions = [
  { label: '初诊', value: 'FIRST' },
  { label: '复诊', value: 'REVISIT' },
  { label: '急诊', value: 'EMERGENCY' },
  { label: '体检', value: 'EXAMINATION' },
]

const recordStatusMap = Object.fromEntries(recordStatusOptions.map((o) => [o.value, o.label]))
const recordTypeMap = Object.fromEntries(recordTypeOptions.map((o) => [o.value, o.label]))

function recordStatusText(v) { return recordStatusMap[v] || v || '—' }
function recordTypeText(v) { return recordTypeMap[v] || v || '—' }
function recordStatusTagType(v) {
  if (v === 'COMPLETED') return 'success'
  if (v === 'CANCELED') return 'info'
  if (v === 'PROCESSING') return 'primary'
  return 'warning'
}
function formatDateTime(v) { return formatDate(v) }
function canEditRecord(row) { return canManageMedical.value && row.status !== 'COMPLETED' && row.status !== 'CANCELED' }
function canCancelRecord(row) { return canManageMedical.value && row.status !== 'COMPLETED' && row.status !== 'CANCELED' }

function canViewMedicalDetail(row) {
  if (row.status === 'WAITING') return false
  const hasDetail = detailIdMap.value[row.id]
  if (hasDetail) return true
  return isDoctor.value
}

function goMedicalDetail(row) {
  const detailId = detailIdMap.value[row.id]
  if (detailId) {
    router.push(`/medical/detail/${detailId}`)
  } else {
    router.push(`/medical/detail/new?recordId=${row.id}`)
  }
}

function goMedicalRecordDetail(row) {
  if (row?.id) router.push(`/console/medical/records/${row.id}`)
}

async function goFirstRegistration(row) {
  if (!row.petId) {
    ElMessage.warning('无法找到宠物信息')
    return
  }
  try {
    const res = await getFirstVisitRegistrations({ pet: row.petId, page: 1, size: 1 })
    const record = res?.records?.[0]
    if (record?.id) {
      router.push(`/medical/first/${record.id}`)
    } else {
      ElMessage.info('该宠物暂无初诊登记')
    }
  } catch (error) {
    ElMessage.warning(error?.message || '查找初诊登记失败')
  }
}

function goCreateRecord() {
  createDialogPetId.value = petId.value || ''
  createDialogPetName.value = petName.value || ''
  createDialogPetAge.value = null
  createDialogVisible.value = true
}

function onPetSelected(pet) {
  router.push(`/console/medical/records?pet=${pet.id}&name=${encodeURIComponent(pet.name || '')}`)
}

onMounted(() => { loadRecords() })
</script>

<style scoped>
.medical-record-cols-search {
  grid-template-columns: 1fr auto;
}
</style>
