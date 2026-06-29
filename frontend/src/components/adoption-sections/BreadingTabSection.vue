<template>
  <section class="pet-admin-section">
    <el-table :data="breadingFilteredRows" v-loading="breadingLoading" class="user-admin-table">
      <el-table-column label="宠物" min-width="200" show-overflow-tooltip>
        <template #default="{ row }">
          <button class="table-primary-link adoption-pet-detail-cell" type="button" @click="goBreadingDetail(row)">
            <div class="adoption-person-cell">
              <strong>{{ row.petName || '未命名宠物' }}</strong>
              <span>{{ [row.petType, row.petBreed, row.petAge != null ? `${row.petAge} 月` : ''].filter(Boolean).join(' · ') }}</span>
            </div>
          </button>
        </template>
      </el-table-column>
      <el-table-column label="申请人" min-width="160">
        <template #header>
          <TableFilterHeader label="申请人" :filter="breadingFilters.applicantName" type="text" :active="breadingIsActive('applicantName')" />
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
          <TableFilterHeader label="状态" :filter="breadingFilters.status" type="enum" :active="breadingIsActive('status')" :options="breadingStatusFilterOptions" />
        </template>
        <template #default="{ row }">
          <el-tag :type="breadingStatusTagType(row.status)" effect="plain">{{ breadingStatusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="审核人" min-width="120">
        <template #default="{ row }">{{ row.reviewerName || '' }}</template>
      </el-table-column>
      <el-table-column min-width="160">
        <template #header>
          <TableFilterHeader label="申请时间" :filter="breadingFilters.createTime" type="time" :active="breadingIsActive('createTime')" />
        </template>
        <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
      </el-table-column>
      <el-table-column width="40" class-name="action-col">
        <template #header>
          <TableActionColumnHeader title="操作" :collapsed="breadingActionCollapsed" @toggle="breadingActionCollapsed = !breadingActionCollapsed" />
        </template>
        <template #default="{ row }">
          <div class="table-action-cell">
            <div class="table-action-panel" :class="{ 'is-collapsed': breadingActionCollapsed }">
              <el-button v-if="breadingCanReview(row)" text type="primary" @click="openBreadingReviewDialog(row)">审核</el-button>
              <el-button v-if="breadingCanCancel(row)" text type="danger" @click="breadingCancel(row)">取消</el-button>
              <el-button v-if="breadingCanCreateAgreement(row)" text type="success" @click="openBreadingAgreementChoice(row)">协议</el-button>
            </div>
          </div>
        </template>
      </el-table-column>
    </el-table>
    <div class="user-admin-pagination">
      <el-pagination
        layout="prev, pager, next, total"
        :current-page="breadingPage.page"
        :page-size="breadingPage.size"
        :total="breadingTotal"
        @current-change="breadingChangePage"
      />
    </div>
  </section>

  <!-- Breading review dialog -->
  <el-dialog v-model="breadingReviewDialogVisible" title="审核寄养申请" width="520px" :close-on-click-modal="false">
    <section v-if="breadingReviewTarget" class="adoption-review-summary">
      <strong>{{ breadingReviewTarget.petName || '未命名宠物' }}</strong>
      <span>{{ breadingReviewTarget.applicantName || '申请人' }} · {{ breadingReviewTarget.applicantPhone || '' }}</span>
    </section>
    <el-form label-position="top">
      <el-form-item label="审核结果">
        <el-radio-group v-model="breadingReviewStatus">
          <el-radio-button label="PASS">通过</el-radio-button>
          <el-radio-button label="REJECT">拒绝</el-radio-button>
        </el-radio-group>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="breadingReviewDialogVisible = false">取消</el-button>
      <el-button class="warm-btn" :loading="breadingActing" @click="submitBreadingReview">保存</el-button>
    </template>
  </el-dialog>

  <!-- Breading agreement choice dialog -->
  <el-dialog v-model="breadingAgreementDialogVisible" title="选择协议类型" width="520px" :close-on-click-modal="false">
    <section v-if="breadingAgreementTarget" class="adoption-review-summary">
      <strong>{{ breadingAgreementTarget.petName || '未命名宠物' }}</strong>
      <span>{{ breadingAgreementTarget.applicantName || '申请人' }} · 寄养协议</span>
    </section>
    <el-radio-group v-model="breadingAgreementType" class="agreement-type-grid">
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
      <el-button @click="breadingAgreementDialogVisible = false">取消</el-button>
      <el-button class="warm-btn" @click="goBreadingAgreementDraft">继续</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Check, Edit, Plus, RefreshRight } from '@element-plus/icons-vue'
import { getBreadingApplications, updateBreadingStatus } from '../../api/adoption'
import { useTableFilters } from '../../composables/useTableFilters'
import { useUserStore } from '../../stores/user'
import TableFilterHeader from '../TableFilterHeader.vue'
import TableActionColumnHeader from '../TableActionColumnHeader.vue'
import { useRoute, useRouter } from 'vue-router'
import { ROLE, hasRole } from '../../utils/roles'
import { adoptionStatusText, adoptionStatusTagType, formatDate } from '../../utils/format'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loginRole = computed(() => Number(userStore.profile?.role || 0))
const loginUserId = computed(() => String(userStore.profile?.id || ''))
const isWorker = computed(() => hasRole(loginRole.value, ROLE.WORKER) || hasRole(loginRole.value, ROLE.ADMIN))
const isAdmin = computed(() => hasRole(loginRole.value, ROLE.ADMIN))
const isVolunteerRole = computed(() => hasRole(loginRole.value, ROLE.VOLUNTEER))
const canManageUsers = computed(() => isAdmin.value || hasRole(loginRole.value, ROLE.WORKER))

const breadingLoading = ref(false)
const breadingActing = ref(false)
const breadingActionCollapsed = ref(false)
const breadingRows = ref([])
const breadingTotal = ref(0)
const breadingReviewDialogVisible = ref(false)
const breadingReviewTarget = ref(null)
const breadingReviewStatus = ref('PASS')
const breadingAgreementDialogVisible = ref(false)
const breadingAgreementTarget = ref(null)
const breadingAgreementType = ref('ELECTRONIC')
const breadingPage = reactive({ page: 1, size: 10 })

const { filters: breadingFilters, isActive: breadingIsActive, applyFilter: breadingApplyFilter } = useTableFilters({
  applicantName: { type: 'text' },
  status: { type: 'enum' },
  createTime: { type: 'time' },
})

const breadingStatusFilterOptions = [
  { value: 'CREATE', label: '已创建' },
  { value: 'PASS', label: '已通过' },
  { value: 'REJECT', label: '已拒绝' },
  { value: 'FINISH', label: '已完成' },
  { value: 'CANCEL', label: '已取消' },
]

const breadingFilteredRows = computed(() => breadingApplyFilter(breadingRows.value || []))
function breadingStatusText(value) { return adoptionStatusText(value) }
function breadingStatusTagType(value) { return adoptionStatusTagType(value) }

function breadingBuildQuery() {
  return {
    page: breadingPage.page, size: breadingPage.size, sort: 'create_time', order: 'desc',
    applicant: isWorker.value ? undefined : [loginUserId.value],
  }
}

async function loadBreadingRows() {
  if (!isWorker.value && !loginUserId.value) return
  breadingLoading.value = true
  try {
    const result = await getBreadingApplications(breadingBuildQuery())
    breadingRows.value = Array.isArray(result?.records) ? result.records : []
    breadingTotal.value = Number(result?.total || breadingRows.value.length)
  } catch (error) {
    ElMessage.warning(error?.message || '加载寄养申请失败')
  } finally {
    breadingLoading.value = false
  }
}

function handleBreadingRefresh() { breadingPage.page = 1; loadBreadingRows() }
function breadingChangePage(value) { breadingPage.page = value; loadBreadingRows() }

function goBreadingDetail(row) {
  if (!row?.id) return
  router.push({ name: 'console-adoption-breading-detail', params: { id: String(row.id) } })
}

function breadingCanReview(row) { return isWorker.value && row?.status === 'CREATE' }
function breadingCanCancel(row) {
  if (!row || row.status === 'FINISH' || row.status === 'CANCEL') return false
  return isWorker.value || String(row.applicantId || '') === loginUserId.value
}
function breadingCanCreateAgreement(row) { return isWorker.value && row?.status === 'PASS' }

function openBreadingReviewDialog(row) {
  breadingReviewTarget.value = row; breadingReviewStatus.value = 'PASS'; breadingReviewDialogVisible.value = true
}

async function submitBreadingReview() {
  if (!breadingReviewTarget.value || breadingActing.value) return
  breadingActing.value = true
  try {
    await updateBreadingStatus(breadingReviewTarget.value.id, breadingReviewStatus.value)
    ElMessage.success(breadingReviewStatus.value === 'PASS' ? '寄养申请已通过' : '寄养申请已拒绝')
    breadingReviewDialogVisible.value = false
    await loadBreadingRows()
  } catch (error) {
    ElMessage.warning(error?.message || '审核失败')
  } finally {
    breadingActing.value = false
  }
}

async function breadingCancel(row) {
  try {
    await ElMessageBox.confirm(`确认取消「${row.petName || '未命名宠物'}」的寄养申请？`, '取消寄养申请', {
      type: 'warning', confirmButtonText: '确认取消', cancelButtonText: '返回',
    })
    await updateBreadingStatus(row.id, 'CANCEL')
    ElMessage.success('寄养申请已取消')
    await loadBreadingRows()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') ElMessage.warning(error?.message || '取消失败')
  }
}

function openBreadingAgreementChoice(row) {
  breadingAgreementTarget.value = row; breadingAgreementType.value = 'ELECTRONIC'; breadingAgreementDialogVisible.value = true
}

function goBreadingAgreementDraft() {
  if (!breadingAgreementTarget.value) return
  const path = breadingAgreementType.value === 'PAPER'
    ? '/console/adoption/agreements/new-paper'
    : '/console/adoption/agreements/new-electronic'
  router.push({
    path,
    query: {
      parentId: breadingAgreementTarget.value.id, parentType: 'BREADING',
      pet: breadingAgreementTarget.value.petName || '', applicant: breadingAgreementTarget.value.applicantName || '',
    },
  })
}

onMounted(() => { loadBreadingRows() })

defineExpose({ loadData: loadBreadingRows, refresh: handleBreadingRefresh })
</script>

<style scoped>
.adoption-pet-detail-cell {
  display: inline-flex;
  width: 100%;
  padding: 0;
  text-align: left;
}

.adoption-person-cell {
  display: grid;
  gap: 3px;
}

.adoption-person-cell strong {
  color: var(--text);
}

.adoption-person-cell span {
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

.agreement-type-card small {
  color: var(--muted);
  font-size: 12px;
}

@media (max-width: 720px) {
  .agreement-type-grid {
    grid-template-columns: 1fr;
  }
}
</style>
