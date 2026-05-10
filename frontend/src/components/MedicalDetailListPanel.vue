<template>
  <el-card class="profile-card pet-admin-card">
    <template #header>
      <div class="profile-card-header">
        <strong>病例</strong>
        <span>{{ petName ? `${petName} 的病例` : '所有病例记录' }}</span>
      </div>
    </template>
    <section class="pet-admin-section">
      <section class="filter-panel pet-directory-filter-panel">
        <div class="pet-filter-row medical-first-cols-search">
          <el-input v-model="searchKeyword" clearable placeholder="按摘要搜索" @keyup.enter="searchRecords" />
          <div class="pet-filter-action">
            <el-button class="warm-btn" :icon="Search" :loading="loading" @click="searchRecords">搜索</el-button>
          </div>
        </div>
      </section>
      <el-table :data="displayedRecords" v-loading="loading" class="user-admin-table">
        <el-table-column label="摘要" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            <button class="table-primary-link" type="button" @click="goDetail(row)">{{ row.summary || '未命名病例' }}</button>
          </template>
        </el-table-column>
        <el-table-column label="宠物" width="120">
          <template #default="{ row }">{{ row.petName || '' }}</template>
        </el-table-column>
        <el-table-column label="医生" width="100">
          <template #default="{ row }">{{ row.username || '' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="detailStatusTagType(row)" effect="plain">
              {{ detailStatusText(row) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" min-width="160">
          <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
        </el-table-column>
        <el-table-column width="40" class-name="action-col">
          <template #header><TableActionColumnHeader title="操作" :collapsed="actionCollapsed" @toggle="actionCollapsed = !actionCollapsed" /></template>
          <template #default="{ row }">
            <div class="table-action-cell">
              <div class="table-action-panel" :class="{ 'is-collapsed': actionCollapsed }">
                <el-button v-if="canCompleteDetail(row)" text type="success" :loading="actingId === row.id" @click="completeDetail(row)">完成</el-button>
                <el-button v-if="canDiscardDetail(row)" text type="danger" :loading="actingId === row.id" @click="discardDetail(row)">废弃</el-button>
                <el-button v-if="canTransferDetail(row)" text type="primary" @click="openTransferDialog(row)">移交</el-button>
              </div>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <div class="user-admin-pagination">
        <el-pagination
          layout="prev, pager, next, total"
          :current-page="page.page"
          :page-size="page.size"
          :total="filteredTotal"
          @current-change="changePage"
        />
      </div>
    </section>
  </el-card>

  <el-dialog v-model="transferDialogVisible" title="移交病例" width="560px" :close-on-click-modal="false">
    <section class="medical-transfer-panel" v-loading="loadingDoctors">
      <p class="medical-transfer-summary">{{ transferTarget?.summary || '未命名病例' }}</p>
      <el-radio-group v-model="transferDoctorId" class="medical-transfer-list">
        <el-radio-button
          v-for="doctor in doctorOptions"
          :key="doctor.id"
          :label="String(doctor.id || '')"
          :disabled="String(doctor.id || '') === loginUserId"
        >
          <span class="medical-transfer-doctor">
            <el-avatar :size="30" :src="doctor.avatar">
              {{ (doctor.username || '医').slice(0, 1) }}
            </el-avatar>
            <span>{{ doctor.username || '未命名兽医' }}</span>
          </span>
        </el-radio-button>
      </el-radio-group>
      <el-empty v-if="!loadingDoctors && !doctorOptions.length" description="暂无可移交的兽医账号" />
    </section>
    <template #footer>
      <el-button @click="transferDialogVisible = false">取消</el-button>
      <el-button class="warm-btn" :loading="transferring" :disabled="!transferDoctorId" @click="transferDetail">确认移交</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { completeMedicalDetail, discardMedicalDetail, getMedicalRecords, getMedicalDetails, updateMedicalDetail } from '../api/services'
import { getUsers } from '../api/user'
import { useUserStore } from '../stores/user'
import { ROLE, hasRole } from '../utils/roles'
import TableActionColumnHeader from './TableActionColumnHeader.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const loadingDoctors = ref(false)
const transferring = ref(false)
const allRecords = ref([])
const doctorOptions = ref([])
const searchKeyword = ref('')
const actionCollapsed = ref(false)
const actingId = ref('')
const transferDialogVisible = ref(false)
const transferTarget = ref(null)
const transferDoctorId = ref('')
const petId = computed(() => route.query.pet || '')
const petName = computed(() => route.query.name || '')
const page = reactive({ page: 1, size: 10 })
const loginRole = computed(() => Number(userStore.profile?.role || 0))
const loginUserId = computed(() => String(userStore.profile?.id || ''))
const isDoctor = computed(() => hasRole(loginRole.value, ROLE.DOCTOR))

const displayedRecords = computed(() => {
  let list = allRecords.value
  if (searchKeyword.value.trim()) {
    const kw = searchKeyword.value.trim().toLowerCase()
    list = list.filter((r) => (r.summary || '').toLowerCase().includes(kw))
  }
  const start = (page.page - 1) * page.size
  return list.slice(start, start + page.size)
})

const filteredTotal = computed(() => {
  if (!searchKeyword.value.trim()) return allRecords.value.length
  const kw = searchKeyword.value.trim().toLowerCase()
  return allRecords.value.filter((r) => (r.summary || '').toLowerCase().includes(kw)).length
})

function formatDate(value) {
  if (!value) return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return String(value)
  return date.toLocaleString('zh-CN')
}

function goDetail(row) {
  if (row?.id) router.push(`/medical/detail/${row.id}`)
}

function searchRecords() {
  page.page = 1
}

function changePage(p) {
  page.page = p
}

function isOwnedByLoginDoctor(row) {
  return isDoctor.value && String(row?.doctorId || '') === loginUserId.value
}

function canCompleteDetail(row) {
  return isOwnedByLoginDoctor(row) && !row?.isCompleted && !row?.isDiscard
}

function canDiscardDetail(row) {
  return isOwnedByLoginDoctor(row) && !row?.isDiscard
}

function canTransferDetail(row) {
  return canCompleteDetail(row)
}

function hasVisibleActions(row) {
  return canCompleteDetail(row) || canDiscardDetail(row) || canTransferDetail(row)
}

function detailStatusText(row) {
  if (row?.isDiscard) return '已废弃'
  if (row?.isCompleted) return '已完成'
  return '进行中'
}

function detailStatusTagType(row) {
  if (row?.isDiscard) return 'info'
  if (row?.isCompleted) return 'success'
  return 'warning'
}

function buildUpdatePayload(row, doctorId = row?.doctorId) {
  return {
    doctorId,
    summary: row?.summary || '',
    physicalExam: row?.physicalExam || undefined,
    diagnosis: row?.diagnosis || undefined,
    differential: row?.differential || undefined,
    exam: row?.exam || undefined,
    treatment: row?.treatment || undefined,
    advice: row?.advice || undefined,
  }
}

async function completeDetail(row) {
  try {
    await ElMessageBox.confirm(`确认完成「${row.summary || '未命名病例'}」？完成后将不能继续编辑。`, '完成病例', {
      type: 'warning',
      confirmButtonText: '确认完成',
      cancelButtonText: '取消',
    })
    actingId.value = row.id
    await completeMedicalDetail(row.id)
    ElMessage.success('病例已完成')
    await loadRecords()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') ElMessage.warning(error?.message || '完成失败')
  } finally {
    actingId.value = ''
  }
}

async function discardDetail(row) {
  try {
    await ElMessageBox.confirm(`确认废弃「${row.summary || '未命名病例'}」？`, '废弃病例', {
      type: 'warning',
      confirmButtonText: '确认废弃',
      cancelButtonText: '取消',
    })
    actingId.value = row.id
    await discardMedicalDetail(row.id)
    ElMessage.success('病例已废弃')
    await loadRecords()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') ElMessage.warning(error?.message || '废弃失败')
  } finally {
    actingId.value = ''
  }
}

async function loadDoctors() {
  loadingDoctors.value = true
  try {
    const result = await getUsers({ page: 1, size: 200 })
    doctorOptions.value = (result?.records || []).filter((user) => hasRole(user.role, ROLE.DOCTOR))
  } catch (error) {
    doctorOptions.value = []
    ElMessage.warning(error?.message || '加载兽医列表失败')
  } finally {
    loadingDoctors.value = false
  }
}

async function openTransferDialog(row) {
  transferTarget.value = row
  transferDoctorId.value = ''
  transferDialogVisible.value = true
  if (!doctorOptions.value.length) await loadDoctors()
}

async function transferDetail() {
  if (!transferTarget.value || !transferDoctorId.value || transferring.value) return
  transferring.value = true
  try {
    await updateMedicalDetail(transferTarget.value.id, buildUpdatePayload(transferTarget.value, transferDoctorId.value))
    ElMessage.success('病例已移交')
    transferDialogVisible.value = false
    await loadRecords()
  } catch (error) {
    ElMessage.warning(error?.message || '移交失败')
  } finally {
    transferring.value = false
  }
}

async function loadRecords() {
  loading.value = true
  try {
    const recordQuery = { sort: 'create_time', order: 'desc', size: 200 }
    if (petId.value) recordQuery.pet = petId.value
    const recordRes = await getMedicalRecords(recordQuery)
    const recordList = recordRes?.records || []
    const recordIds = recordList.map((r) => r.id).filter(Boolean)

    const detailList = []
    if (recordIds.length) {
      const detailRes = await getMedicalDetails({ record: recordIds, size: 200 })
      const details = detailRes?.records || []
      for (const d of details) {
        const rec = recordList.find((r) => String(r.id) === String(d.recordId))
        detailList.push({ ...d, recordStatus: rec?.status, recordType: rec?.type, petName: rec?.petName })
      }
    }
    allRecords.value = detailList
  } catch (error) {
    ElMessage.warning(error?.message || '加载病例失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => loadRecords())
</script>

<style scoped>
.medical-transfer-panel {
  min-height: 160px;
}

.medical-transfer-summary {
  margin: 0 0 14px;
  color: var(--muted);
  line-height: 1.6;
}

.medical-transfer-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
  width: 100%;
}

.medical-transfer-list :deep(.el-radio-button__inner) {
  width: 100%;
  border-radius: 12px;
  border: 1px solid var(--line);
  background: rgba(255, 253, 249, 0.92);
  padding: 10px 12px;
  text-align: left;
  box-shadow: none;
}

.medical-transfer-list :deep(.el-radio-button:first-child .el-radio-button__inner),
.medical-transfer-list :deep(.el-radio-button:last-child .el-radio-button__inner) {
  border-radius: 12px;
}

.medical-transfer-doctor {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

@media (max-width: 640px) {
  .medical-transfer-list {
    grid-template-columns: 1fr;
  }
}
</style>
