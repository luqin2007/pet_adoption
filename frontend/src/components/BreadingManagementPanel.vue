<template>
  <el-card class="profile-card pet-admin-card">
    <template #header>
      <div class="profile-card-header">
        <strong>寄养管理</strong>
        <span>{{ isWorker ? '查看与审核全部寄养申请' : '查看我的寄养申请' }}</span>
      </div>
    </template>

    <section class="pet-admin-section">
      <section class="filter-panel pet-directory-filter-panel">
        <div class="pet-filter-row adoption-filter-row">
          <el-select v-model="statusFilter" class="filter-field-sm" clearable placeholder="申请状态">
            <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
          <el-date-picker
            v-model="timeRange"
            type="daterange"
            value-format="YYYY-MM-DD HH:mm:ss"
            start-placeholder="申请开始"
            end-placeholder="申请结束"
            class="filter-field-lg"
          />
          <div class="pet-filter-action">
            <el-button class="soft-btn" :icon="Plus" @click="goCreateBreading">申请寄养</el-button>
            <el-button class="warm-btn" :icon="Search" :loading="loading" @click="searchRows">搜索</el-button>
          </div>
        </div>
      </section>

      <el-table :data="rows" v-loading="loading" class="user-admin-table">
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
          <template #default="{ row }">
            <div class="adoption-person-cell">
              <strong>{{ row.applicantName || '未命名用户' }}</strong>
              <span>{{ row.applicantPhone || '' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" effect="plain">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="审核人" min-width="120">
          <template #default="{ row }">{{ row.reviewerName || '' }}</template>
        </el-table-column>
        <el-table-column label="申请时间" min-width="160">
          <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
        </el-table-column>
        <el-table-column width="40" class-name="action-col">
          <template #header>
            <TableActionColumnHeader title="操作" :collapsed="actionCollapsed" @toggle="actionCollapsed = !actionCollapsed" />
          </template>
          <template #default="{ row }">
            <div class="table-action-cell">
              <div class="table-action-panel" :class="{ 'is-collapsed': actionCollapsed }">
                <el-button v-if="canReview(row)" text type="primary" @click="openReviewDialog(row)">审核</el-button>
                <el-button v-if="canCancel(row)" text type="danger" @click="cancelBreading(row)">取消</el-button>
                <el-button v-if="canCreateAgreement(row)" text type="success" @click="openAgreementChoice(row)">协议</el-button>
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
          :total="total"
          @current-change="changePage"
        />
      </div>
    </section>
  </el-card>

  <el-dialog v-model="reviewDialogVisible" title="审核寄养申请" width="520px" :close-on-click-modal="false">
    <section v-if="reviewTarget" class="adoption-review-summary">
      <strong>{{ reviewTarget.petName || '未命名宠物' }}</strong>
      <span>{{ reviewTarget.applicantName || '申请人' }} · {{ reviewTarget.applicantPhone || '' }}</span>
    </section>
    <el-form label-position="top">
      <el-form-item label="审核结果">
        <el-radio-group v-model="reviewStatus">
          <el-radio-button label="PASS">通过</el-radio-button>
          <el-radio-button label="REJECT">拒绝</el-radio-button>
        </el-radio-group>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="reviewDialogVisible = false">取消</el-button>
      <el-button class="warm-btn" :loading="acting" @click="submitReview">保存</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="agreementDialogVisible" title="选择协议类型" width="520px" :close-on-click-modal="false">
    <section v-if="agreementTarget" class="adoption-review-summary">
      <strong>{{ agreementTarget.petName || '未命名宠物' }}</strong>
      <span>{{ agreementTarget.applicantName || '申请人' }} · 寄养协议</span>
    </section>
    <el-radio-group v-model="agreementType" class="agreement-type-grid">
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
      <el-button @click="agreementDialogVisible = false">取消</el-button>
      <el-button class="warm-btn" @click="goAgreementDraft">继续</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import { getBreadingApplications, updateBreadingStatus } from '../api/services'
import { useUserStore } from '../stores/user'
import { ROLE, hasRole } from '../utils/roles'
import TableActionColumnHeader from './TableActionColumnHeader.vue'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const acting = ref(false)
const actionCollapsed = ref(false)
const rows = ref([])
const total = ref(0)
const statusFilter = ref('')
const timeRange = ref([])
const reviewDialogVisible = ref(false)
const reviewTarget = ref(null)
const reviewStatus = ref('PASS')
const agreementDialogVisible = ref(false)
const agreementTarget = ref(null)
const agreementType = ref('ELECTRONIC')
const page = reactive({ page: 1, size: 10 })

const loginRole = computed(() => Number(userStore.profile?.role || 0))
const loginUserId = computed(() => String(userStore.profile?.id || ''))
const isWorker = computed(() => hasRole(loginRole.value, ROLE.WORKER) || hasRole(loginRole.value, ROLE.ADMIN))

const statusOptions = [
  { label: '已提交', value: 'CREATE' },
  { label: '审核通过', value: 'PASS' },
  { label: '审核拒绝', value: 'REJECT' },
  { label: '协议草拟中', value: 'AGREEMENT_DRAFT' },
  { label: '待确认', value: 'AGREEMENT_PENDING_CONFIRM' },
  { label: '协议已签署', value: 'AGREEMENT_SIGNED' },
  { label: '回访中', value: 'TRACKING' },
  { label: '流程完成', value: 'FINISH' },
  { label: '已取消', value: 'CANCEL' },
]

const statusMap = Object.fromEntries(statusOptions.map((item) => [item.value, item.label]))

function statusText(value) {
  return statusMap[value] || value || ''
}

function statusTagType(value) {
  if (value === 'PASS' || value === 'AGREEMENT_SIGNED' || value === 'FINISH') return 'success'
  if (value === 'REJECT' || value === 'CANCEL') return 'info'
  if (value === 'AGREEMENT_DRAFT' || value === 'TRACKING') return 'primary'
  if (value === 'AGREEMENT_PENDING_CONFIRM') return 'warning'
  return 'warning'
}

function formatDate(value) {
  if (!value) return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return String(value)
  return date.toLocaleString('zh-CN')
}

function buildQuery() {
  const [time0, time1] = timeRange.value || []
  return {
    page: page.page,
    size: page.size,
    sort: 'create_time',
    order: 'desc',
    applicant: isWorker.value ? undefined : [loginUserId.value],
    status: statusFilter.value ? [statusFilter.value] : undefined,
    time0: time0 || undefined,
    time1: time1 || undefined,
  }
}

async function loadRows() {
  if (!isWorker.value && !loginUserId.value) return
  loading.value = true
  try {
    const result = await getBreadingApplications(buildQuery())
    rows.value = Array.isArray(result?.records) ? result.records : []
    total.value = Number(result?.total || rows.value.length)
  } catch (error) {
    ElMessage.warning(error?.message || '加载寄养申请失败')
  } finally {
    loading.value = false
  }
}

function searchRows() {
  page.page = 1
  loadRows()
}

function changePage(value) {
  page.page = value
  loadRows()
}

function goCreateBreading() {
  router.push({ name: 'breading-create' })
}

function goBreadingDetail(row) {
  if (!row?.id) return
  router.push({
    name: 'console-adoption-breading-detail',
    params: { id: String(row.id) },
  })
}

function canReview(row) {
  return isWorker.value && row?.status === 'CREATE'
}

function canCancel(row) {
  if (!row || row.status === 'FINISH' || row.status === 'CANCEL') return false
  return isWorker.value || String(row.applicantId || '') === loginUserId.value
}

function canCreateAgreement(row) {
  return isWorker.value && row?.status === 'PASS'
}

function openReviewDialog(row) {
  reviewTarget.value = row
  reviewStatus.value = 'PASS'
  reviewDialogVisible.value = true
}

async function submitReview() {
  if (!reviewTarget.value || acting.value) return
  acting.value = true
  try {
    await updateBreadingStatus(reviewTarget.value.id, reviewStatus.value)
    ElMessage.success(reviewStatus.value === 'PASS' ? '寄养申请已通过' : '寄养申请已拒绝')
    reviewDialogVisible.value = false
    await loadRows()
  } catch (error) {
    ElMessage.warning(error?.message || '审核失败')
  } finally {
    acting.value = false
  }
}

async function cancelBreading(row) {
  try {
    await ElMessageBox.confirm(`确认取消「${row.petName || '未命名宠物'}」的寄养申请？`, '取消寄养申请', {
      type: 'warning',
      confirmButtonText: '确认取消',
      cancelButtonText: '返回',
    })
    await updateBreadingStatus(row.id, 'CANCEL')
    ElMessage.success('寄养申请已取消')
    await loadRows()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.warning(error?.message || '取消失败')
    }
  }
}

function openAgreementChoice(row) {
  agreementTarget.value = row
  agreementType.value = 'ELECTRONIC'
  agreementDialogVisible.value = true
}

function goAgreementDraft() {
  if (!agreementTarget.value) return
  const path = agreementType.value === 'PAPER'
    ? '/console/adoption/agreements/new-paper'
    : '/console/adoption/agreements/new-electronic'
  router.push({
    path,
    query: {
      parentId: agreementTarget.value.id,
      parentType: 'BREADING',
      pet: agreementTarget.value.petName || '',
      applicant: agreementTarget.value.applicantName || '',
    },
  })
}

onMounted(() => {
  loadRows()
})
</script>

<style scoped>
.adoption-filter-row {
  grid-template-columns: minmax(180px, 240px) auto;
}

.adoption-person-cell {
  display: grid;
  gap: 3px;
}

.adoption-person-cell strong {
  color: var(--text);
}

.adoption-pet-detail-cell {
  display: inline-flex;
  width: 100%;
  padding: 0;
  text-align: left;
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
  .adoption-filter-row,
  .agreement-type-grid {
    grid-template-columns: 1fr;
  }
}
</style>
