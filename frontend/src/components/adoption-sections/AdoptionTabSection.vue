<template>
  <section class="pet-admin-section">
    <el-table :data="adoptFilteredRows" v-loading="adoptLoading" class="user-admin-table">
      <el-table-column min-width="180" show-overflow-tooltip>
        <template #header>
          <TableFilterHeader label="宠物" :filter="adoptFilters.petName" type="text" :active="adoptIsActive('petName')" />
        </template>
        <template #default="{ row }">
          <button class="table-primary-link adoption-pet-cell" type="button" @click="goAdoptDetail(row)">
            <el-avatar :size="34" :src="row.petCover">
              {{ (row.petName || '宠').slice(0, 1) }}
            </el-avatar>
            <span>{{ row.petName || '未命名宠物' }}</span>
          </button>
        </template>
      </el-table-column>
      <el-table-column label="申请人" min-width="160">
        <template #header>
          <TableFilterHeader label="申请人" :filter="adoptFilters.applicantName" type="text" :active="adoptIsActive('applicantName')" />
        </template>
        <template #default="{ row }">
          <div class="adoption-person-cell">
            <strong>{{ row.applicantName || '未命名用户' }}</strong>
            <span>{{ row.applicantPhone || '' }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column width="120">
        <template #header>
          <TableFilterHeader label="状态" :filter="adoptFilters.status" type="enum" :active="adoptIsActive('status')" :options="adoptStatusFilterOptions" />
        </template>
        <template #default="{ row }">
          <el-tag :type="adoptStatusTagType(row.status)" effect="plain">{{ adoptStatusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="审核人" min-width="120">
        <template #default="{ row }">{{ row.reviewerName || '' }}</template>
      </el-table-column>
      <el-table-column min-width="160">
        <template #header>
          <TableFilterHeader label="申请时间" :filter="adoptFilters.createTime" type="time" :active="adoptIsActive('createTime')" />
        </template>
        <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
      </el-table-column>
      <el-table-column width="40" class-name="action-col">
        <template #header>
          <TableActionColumnHeader title="操作" :collapsed="adoptActionCollapsed" @toggle="adoptActionCollapsed = !adoptActionCollapsed" />
        </template>
        <template #default="{ row }">
          <div class="table-action-cell">
            <div class="table-action-panel" :class="{ 'is-collapsed': adoptActionCollapsed }">
              <el-button v-if="adoptCanReview(row)" text type="primary" @click="openAdoptReviewDialog(row)">审核</el-button>
              <el-button v-if="adoptCanCancel(row)" text type="danger" @click="adoptCancel(row)">取消</el-button>
              <el-button v-if="adoptCanCreateAgreement(row)" text type="success" @click="openAdoptAgreementChoice(row)">协议</el-button>
              <el-button v-if="adoptCanCreateFollowTask(row)" text type="warning" :loading="adoptFollowLoadingId === String(row.id)" @click="goAdoptFollowTask(row)">回访</el-button>
            </div>
          </div>
        </template>
      </el-table-column>
    </el-table>
    <div class="user-admin-pagination">
      <el-pagination
        layout="prev, pager, next, total"
        :current-page="adoptPage.page"
        :page-size="adoptPage.size"
        :total="adoptTotal"
        @current-change="adoptChangePage"
      />
    </div>
  </section>

  <!-- Adoption review dialog -->
  <el-dialog v-model="adoptReviewDialogVisible" title="审核领养申请" width="520px" :close-on-click-modal="false">
    <section v-if="adoptReviewTarget" class="adoption-review-summary">
      <strong>{{ adoptReviewTarget.petName || '未命名宠物' }}</strong>
      <span>{{ adoptReviewTarget.applicantName || '申请人' }} · {{ adoptReviewTarget.applicantPhone || '' }}</span>
    </section>
    <el-form label-position="top">
      <el-form-item label="审核结果">
        <el-radio-group v-model="adoptReviewStatus">
          <el-radio-button label="PASS">通过</el-radio-button>
          <el-radio-button label="REJECT">拒绝</el-radio-button>
        </el-radio-group>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="adoptReviewDialogVisible = false">取消</el-button>
      <el-button class="warm-btn" :loading="adoptActing" @click="submitAdoptReview">保存</el-button>
    </template>
  </el-dialog>

  <!-- Adoption agreement choice dialog -->
  <el-dialog v-model="adoptAgreementDialogVisible" title="选择协议类型" width="520px" :close-on-click-modal="false">
    <section v-if="adoptAgreementTarget" class="adoption-review-summary">
      <strong>{{ adoptAgreementTarget.petName || '未命名宠物' }}</strong>
      <span>{{ adoptAgreementTarget.applicantName || '申请人' }} · 领养协议</span>
    </section>
    <el-radio-group v-model="adoptAgreementType" class="agreement-type-grid">
      <el-radio-button label="ELECTRONIC">
        <span class="agreement-type-card">
          <strong>电子协议</strong>
          <small>在线起草协议正文</small>
        </span>
      </el-radio-button>
      <el-radio-button label="PAPER">
        <span class="agreement-type-card">
          <strong>纸质协议</strong>
          <small>上传扫描图片并排序</small>
        </span>
      </el-radio-button>
    </el-radio-group>
    <template #footer>
      <el-button @click="adoptAgreementDialogVisible = false">取消</el-button>
      <el-button class="warm-btn" @click="goAdoptAgreementDraft">继续</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Check, Edit, Plus, RefreshRight } from '@element-plus/icons-vue'
import { deleteAgreementFile, getAdoptApplication, getAdoptApplications, getAgreement, getAgreements, reorderAgreementFiles, updateAdoptStatus, updateAgreement, uploadAgreement } from '../../api/adoption'
import { useTableFilters } from '../../composables/useTableFilters'
import { useUserStore } from '../../stores/user'
import TableFilterHeader from '../TableFilterHeader.vue'
import TableActionColumnHeader from '../TableActionColumnHeader.vue'
import { useRoute, useRouter } from 'vue-router'
import { ROLE, hasRole } from '../../utils/roles'
import { adoptionStatusText, adoptionStatusTagType, formatDate } from '../../utils/format'

const userStore = useUserStore()
const router = useRouter()
const route = useRoute()

const loginRole = computed(() => Number(userStore.profile?.role || 0))
const loginUserId = computed(() => String(userStore.profile?.id || ''))
const isWorker = computed(() => hasRole(loginRole.value, ROLE.WORKER) || hasRole(loginRole.value, ROLE.ADMIN))
const isAdmin = computed(() => hasRole(loginRole.value, ROLE.ADMIN))
const isVolunteerRole = computed(() => hasRole(loginRole.value, ROLE.VOLUNTEER))
const canManageUsers = computed(() => isAdmin.value || hasRole(loginRole.value, ROLE.WORKER))

const adoptLoading = ref(false)
const adoptActing = ref(false)
const adoptActionCollapsed = ref(false)
const adoptRows = ref([])
const adoptTotal = ref(0)
const adoptReviewDialogVisible = ref(false)
const adoptReviewTarget = ref(null)
const adoptReviewStatus = ref('PASS')
const adoptAgreementDialogVisible = ref(false)
const adoptAgreementTarget = ref(null)
const adoptAgreementType = ref('ELECTRONIC')
const adoptFollowLoadingId = ref('')
const adoptPage = reactive({ page: 1, size: 10 })

const { filters: adoptFilters, isActive: adoptIsActive, applyFilter: adoptApplyFilter } = useTableFilters({
  applicantName: { type: 'text' },
  petName: { type: 'text' },
  status: { type: 'enum' },
  createTime: { type: 'time' },
})

const adoptStatusFilterOptions = [
  { value: 'CREATE', label: '已创建' },
  { value: 'PASS', label: '已通过' },
  { value: 'REJECT', label: '已拒绝' },
  { value: 'FINISH', label: '已完成' },
  { value: 'CANCEL', label: '已取消' },
]

const adoptFilteredRows = computed(() => adoptApplyFilter(adoptRows.value || []))

function adoptStatusText(value) { return adoptionStatusText(value) }
function adoptStatusTagType(value) { return adoptionStatusTagType(value) }

function adoptBuildQuery() {
  return {
    page: adoptPage.page,
    size: adoptPage.size,
    sort: 'create_time',
    order: 'desc',
    user: isWorker.value ? undefined : [loginUserId.value],
  }
}

async function loadAdoptRows() {
  if (!isWorker.value && !loginUserId.value) return
  adoptLoading.value = true
  try {
    const result = await getAdoptApplications(adoptBuildQuery())
    adoptRows.value = Array.isArray(result?.records) ? result.records : []
    adoptTotal.value = Number(result?.total || adoptRows.value.length)
  } catch (error) {
    ElMessage.warning(error?.message || '加载领养申请失败')
  } finally {
    adoptLoading.value = false
  }
}

function handleAdoptRefresh() { adoptPage.page = 1; loadAdoptRows() }
function adoptChangePage(value) { adoptPage.page = value; loadAdoptRows() }

function goAdoptDetail(row) {
  if (row?.id) {
    router.push({ name: 'console-adoption-adopt-detail', params: { id: String(row.id) } })
  }
}

function adoptCanReview(row) { return isWorker.value && row?.status === 'CREATE' }
function adoptCanCancel(row) {
  if (!row || row.status === 'FINISH' || row.status === 'CANCEL') return false
  return isWorker.value || String(row.applicantId || '') === loginUserId.value
}
function adoptCanCreateAgreement(row) { return isWorker.value && row?.status === 'PASS' }
function adoptCanCreateFollowTask(row) { return isWorker.value && ['AGREEMENT_SIGNED', 'TRACKING'].includes(row?.status) }

function openAdoptReviewDialog(row) {
  adoptReviewTarget.value = row
  adoptReviewStatus.value = 'PASS'
  adoptReviewDialogVisible.value = true
}

async function submitAdoptReview() {
  if (!adoptReviewTarget.value || adoptActing.value) return
  adoptActing.value = true
  try {
    await updateAdoptStatus(adoptReviewTarget.value.id, adoptReviewStatus.value)
    ElMessage.success(adoptReviewStatus.value === 'PASS' ? '领养申请已通过' : '领养申请已拒绝')
    adoptReviewDialogVisible.value = false
    await loadAdoptRows()
  } catch (error) {
    ElMessage.warning(error?.message || '审核失败')
  } finally {
    adoptActing.value = false
  }
}

async function adoptCancel(row) {
  try {
    await ElMessageBox.confirm(`确认取消「${row.petName || '未命名宠物'}」的领养申请？`, '取消领养申请', {
      type: 'warning', confirmButtonText: '确认取消', cancelButtonText: '返回',
    })
    await updateAdoptStatus(row.id, 'CANCEL')
    ElMessage.success('领养申请已取消')
    await loadAdoptRows()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') ElMessage.warning(error?.message || '取消失败')
  }
}

function openAdoptAgreementChoice(row) {
  adoptAgreementTarget.value = row
  adoptAgreementType.value = 'ELECTRONIC'
  adoptAgreementDialogVisible.value = true
}

async function goAdoptFollowTask(row) {
  if (!row?.id || adoptFollowLoadingId.value) return
  adoptFollowLoadingId.value = String(row.id)
  try {
    const detail = await getAdoptApplication(row.id)
    const ongoing = Array.isArray(detail?.followTasks)
      ? [...detail.followTasks]
          .filter((item) => item.status !== 'FINISH')
          .sort((a, b) => new Date(b?.updateTime || b?.createTime || 0) - new Date(a?.updateTime || a?.createTime || 0))
      : []
    if (ongoing.length) {
      router.push({ name: 'console-adoption-follow-task-detail', params: { id: String(ongoing[0].id) } })
      return
    }
    router.push({
      name: 'console-adoption-follow-create',
      params: { id: String(row.id) },
      query: { pet: detail?.petName || row.petName || '', applicant: detail?.applicantName || row.applicantName || '' },
    })
  } catch (error) {
    ElMessage.warning(error?.message || '获取回访任务失败')
  } finally {
    adoptFollowLoadingId.value = ''
  }
}

function goAdoptAgreementDraft() {
  if (!adoptAgreementTarget.value) return
  const path = adoptAgreementType.value === 'PAPER'
    ? '/console/adoption/agreements/new-paper'
    : '/console/adoption/agreements/new-electronic'
  router.push({
    path,
    query: {
      parentId: adoptAgreementTarget.value.id, parentType: 'ADOPT',
      pet: adoptAgreementTarget.value.petName || '', applicant: adoptAgreementTarget.value.applicantName || '',
    },
  })
}

defineExpose({ loadData: loadAdoptRows, refresh: handleAdoptRefresh })

onMounted(() => {
  loadAdoptRows()
})
</script>

<style scoped>
.adoption-pet-cell {
  display: inline-flex;
  align-items: center;
  gap: 10px;
}

.adoption-person-cell {
  display: grid;
  gap: 3px;
}

.adoption-person-cell strong {
  color: var(--text);
}

.adoption-person-cell span,
.agreement-type-card small {
  color: var(--muted);
  font-size: 12px;
}

.adoption-review-summary {
  display: grid;
  gap: 4px;
  margin-bottom: 16px;
  padding: 12px 14px;
  border: 1px solid var(--line);
  border-radius: 12px;
  background: rgba(255, 253, 249, 0.92);
}

.adoption-review-summary span {
  color: var(--muted);
}

.agreement-type-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  width: 100%;
}

.agreement-type-grid :deep(.el-radio-button__inner) {
  width: 100%;
  border: 1px solid var(--line);
  border-radius: 12px;
  padding: 12px;
  text-align: left;
  background: rgba(255, 253, 249, 0.92);
  box-shadow: none;
}

.agreement-type-grid :deep(.el-radio-button:first-child .el-radio-button__inner),
.agreement-type-grid :deep(.el-radio-button:last-child .el-radio-button__inner) {
  border-radius: 12px;
}

.agreement-type-card {
  display: grid;
  gap: 4px;
}

@media (max-width: 720px) {
  .agreement-type-grid {
    grid-template-columns: 1fr;
  }
}
</style>
