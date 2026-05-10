<template>
  <el-card class="profile-card pet-admin-card">
    <template #header>
      <div class="profile-card-header">
        <strong>就诊记录</strong>
        <span>{{ petName ? `${petName}的就诊历史` : '查看就诊历史' }}</span>
      </div>
    </template>
    <section class="pet-admin-section">
      <section class="filter-panel pet-directory-filter-panel">
        <div class="pet-filter-row medical-record-cols-search">
          <el-select v-model="statusFilter" clearable placeholder="就诊状态">
            <el-option v-for="item in recordStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
          <div class="pet-filter-action">
            <el-button class="warm-btn" :icon="Search" circle :loading="loadingRecords" @click="searchRecords" />
            <el-button v-if="canManageMedical && petId" class="soft-btn" :icon="Plus" @click="goCreateRecord" />
          </div>
        </div>
      </section>
      <el-table :data="recordRows" v-loading="loadingRecords" class="user-admin-table">
        <el-table-column label="宠物" min-width="120">
          <template #default="{ row }">
            <div class="pet-admin-pet">
              <button class="pet-admin-cover-button" type="button" @click="goMedicalRecordDetail(row)">
                <img :src="row.cover || 'https://images.pexels.com/photos/1170986/pexels-photo-1170986.jpeg?auto=compress&cs=tinysrgb&w=320'" :alt="row.petName" />
              </button>
              <div>
                <button class="pet-admin-name-button" type="button" @click="goMedicalRecordDetail(row)">{{ row.petName || '未命名' }}</button>
                <span>{{ row.petType || '宠物' }} · {{ row.petSex || '未知' }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="类型" width="100">
          <template #default="{ row }"><el-tag effect="plain" type="warning">{{ recordTypeText(row.type) }}</el-tag></template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }"><el-tag effect="plain" :type="recordStatusTagType(row.status)">{{ recordStatusText(row.status) }}</el-tag></template>
        </el-table-column>
        <el-table-column label="接诊医生" min-width="120">
          <template #default="{ row }">{{ row.username || '—' }}</template>
        </el-table-column>
        <el-table-column label="就诊时间" min-width="160">
          <template #default="{ row }">{{ formatDateTime(row.startTime) }}</template>
        </el-table-column>
        <el-table-column label="创建时间" min-width="160">
          <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
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
        <el-pagination layout="prev, pager, next, total" :current-page="page.page" :page-size="page.size" :total="total" @current-change="changePage" />
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
          <el-date-picker v-model="editForm.startTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择开始时间" class="full-width-control" />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker v-model="editForm.endTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择结束时间" class="full-width-control" />
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
  </el-card>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import { getMedicalRecords, updateMedicalRecord, getMedicalDetails, getFirstVisitRegistrations } from '../api/services'
import { useConsoleGuards } from '../composables/useConsoleGuards'
import { ROLE, hasRole } from '../utils/roles'
import TableActionColumnHeader from './TableActionColumnHeader.vue'
import MedicalRecordCreateDialog from './MedicalRecordCreateDialog.vue'

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
const total = ref(0)
const statusFilter = ref('')
const page = reactive({ page: 1, size: 10 })
const editFormRef = ref()

const detailIdMap = ref({})

const createDialogVisible = ref(false)
const createDialogPetId = ref('')
const createDialogPetName = ref('')
const createDialogPetAge = ref(null)

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
function formatDateTime(v) { return v ? new Date(v).toLocaleString('zh-CN') : '—' }
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
  createDialogPetId.value = petId.value
  createDialogPetName.value = petName.value
  createDialogPetAge.value = null
  createDialogVisible.value = true
}

function onRecordCreated() {
  loadRecords()
}

const editRules = {
  type: [{ required: true, message: '请选择就诊类型', trigger: 'change' }],
  status: [{ required: true, message: '请选择就诊状态', trigger: 'change' }],
}

async function loadRecords() {
  loadingRecords.value = true
  try {
    const query = { page: page.page, size: page.size, sort: 'create_time', order: 'desc' }
    if (petId.value) query.pet = petId.value
    if (statusFilter.value) query.status = statusFilter.value
    const result = await getMedicalRecords(query)
    recordRows.value = Array.isArray(result?.records) ? result.records : []
    total.value = Number(result?.total || recordRows.value.length)
    loadDetailExistence()
  } catch (error) { ElMessage.warning(error?.message || '加载就诊记录失败') }
  finally { loadingRecords.value = false }
}

async function loadDetailExistence() {
  const recordIds = recordRows.value
    .filter((r) => r.status !== 'WAITING' && r.id)
    .map((r) => r.id)
  if (!recordIds.length) { detailIdMap.value = {}; return }
  try {
    const res = await getMedicalDetails({ record: recordIds, page: 1, size: 100 })
    const details = res?.records || []
    const map = {}
    for (const d of details) {
      if (d.recordId) map[d.recordId] = d.id
    }
    detailIdMap.value = map
  } catch { /* ignore */ }
}

function searchRecords() { page.page = 1; loadRecords() }
function changePage(p) { page.page = p; loadRecords() }

function openEditDialog(row) {
  Object.assign(editForm, {
    id: String(row.id), type: row.type || '', status: row.status || '',
    startTime: row.startTime || '', endTime: row.endTime || '',
    price: row.price || '', cost: row.cost || '', ownerPhone: row.ownerPhone || '',
  })
  editDialogVisible.value = true
}

async function saveEdit() {
  if (!editFormRef.value || saving.value) return
  saving.value = true
  try {
    await editFormRef.value.validate()
    const payload = { type: editForm.type, status: editForm.status }
    if (editForm.startTime) payload.startTime = editForm.startTime
    if (editForm.endTime) payload.endTime = editForm.endTime
    if (editForm.price) payload.price = editForm.price
    if (editForm.cost) payload.cost = editForm.cost
    if (editForm.ownerPhone) payload.ownerPhone = editForm.ownerPhone
    await updateMedicalRecord(editForm.id, payload)
    ElMessage.success('就诊记录已更新')
    editDialogVisible.value = false
    await loadRecords()
  } catch (error) { ElMessage.warning(error?.message || '保存失败') }
  finally { saving.value = false }
}

async function cancelRecord(row) {
  try {
    await ElMessageBox.confirm(`确认取消「${row.petName || row.id}」的就诊记录？`, '取消就诊', { type: 'warning', confirmButtonText: '确认取消', cancelButtonText: '返回' })
    await updateMedicalRecord(row.id, { type: row.type, status: 'CANCELED' })
    ElMessage.success('就诊记录已取消')
    await loadRecords()
  } catch (error) { if (error !== 'cancel' && error !== 'close') ElMessage.warning(error?.message || '取消失败') }
}

onMounted(() => { loadRecords() })
</script>

<style scoped>
.medical-record-cols-search {
  grid-template-columns: 1fr auto;
}
</style>
