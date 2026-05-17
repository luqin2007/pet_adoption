<template>
  <el-card class="profile-card pet-admin-card">
    <template #header>
      <div class="profile-card-header">
        <strong>捐赠</strong>
        <span>{{ isWorker ? '查看并处理全部物资捐赠' : '查看我的捐赠记录和使用状态' }}</span>
        <div class="profile-actions">
          <el-button class="warm-btn" :icon="RefreshRight" :loading="loading" @click="handleRefresh">刷新</el-button>
        </div>
      </div>
    </template>

    <section class="pet-admin-section">
      <el-table :data="filteredRows" v-loading="loading" class="user-admin-table">
        <el-table-column label="捐赠物资" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">
            <button class="table-primary-link" type="button" @click="openDetail(row)">
              {{ donationTitle(row) }}
            </button>
            <span class="item-donation-subtext">{{ itemSummary(row) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="捐赠人" min-width="130">
          <template #header>
            <TableFilterHeader label="捐赠人" :filter="filters.username" type="text" :active="isActive('username')" />
          </template>
          <template #default="{ row }">{{ row.username || '未命名用户' }}</template>
        </el-table-column>
        <el-table-column label="交付方式" width="110">
          <template #default="{ row }">{{ deliveryText(row.delivery) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #header>
            <TableFilterHeader label="状态" :filter="filters.status" type="enum" :active="isActive('status')" :options="statusOptions" />
          </template>
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" effect="plain">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="登记时间" min-width="160">
          <template #header>
            <TableFilterHeader label="时间" :filter="filters.createTime" type="time" :active="isActive('createTime')" />
          </template>
          <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
        </el-table-column>
        <el-table-column width="40" class-name="action-col">
          <template #header>
            <TableActionColumnHeader title="操作" :collapsed="actionCollapsed" @toggle="actionCollapsed = !actionCollapsed" />
          </template>
          <template #default="{ row }">
            <div class="table-action-cell">
              <div class="table-action-panel" :class="{ 'is-collapsed': actionCollapsed }">
                <el-button text type="primary" @click="openDetail(row)">详情</el-button>
                <el-button v-if="nextStatuses(row).length" text type="warning" @click="openStatusDialog(row)">状态</el-button>
              </div>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="user-admin-pagination">
        <el-pagination layout="prev, pager, next, total" :current-page="page.page" :page-size="page.size" :total="total" @current-change="changePage" />
      </div>
    </section>
  </el-card>

  <el-drawer v-model="detailVisible" title="捐赠详情" size="680px">
    <section v-if="detail" class="item-detail-stack">
      <div class="item-detail-hero">
        <div>
          <span>当前状态</span>
          <strong>{{ statusText(detail.status) }}</strong>
        </div>
        <el-button v-if="isWorker && detail.status === 'RECEIVED'" class="warm-btn" @click="goStockIn">入库登记</el-button>
      </div>

      <section class="item-detail-block">
        <h3>交付信息</h3>
        <dl class="item-detail-list">
          <div><dt>捐赠人</dt><dd>{{ detail.username || '未命名用户' }}</dd></div>
          <div><dt>交付方式</dt><dd>{{ deliveryText(detail.delivery) }}</dd></div>
          <div><dt>取货地址</dt><dd>{{ detail.address || '' }}</dd></div>
          <div><dt>快递单号</dt><dd>{{ detail.trackingNumber || '' }}</dd></div>
          <div><dt>备注</dt><dd>{{ detail.description || '' }}</dd></div>
        </dl>
      </section>

      <section class="item-detail-block">
        <h3>物资明细</h3>
        <el-table :data="detail.items || []" class="user-admin-table compact-table">
          <el-table-column label="物资" min-width="150">
            <template #default="{ row }">{{ row.itemName || '物资' }}</template>
          </el-table-column>
          <el-table-column label="分类" min-width="120">
            <template #default="{ row }">{{ row.categoryName || '' }}</template>
          </el-table-column>
          <el-table-column label="数量" width="110">
            <template #default="{ row }">{{ row.count }}{{ row.itemUnit || '' }}</template>
          </el-table-column>
          <el-table-column label="说明" min-width="160" show-overflow-tooltip>
            <template #default="{ row }">{{ row.description || '' }}</template>
          </el-table-column>
        </el-table>
      </section>

      <section v-if="detail.files?.length" class="item-detail-block">
        <h3>捐赠影像</h3>
        <div class="item-media-grid">
          <a v-for="file in detail.files" :key="file.id || file.assetUrl" :href="file.assetUrl" target="_blank" rel="noreferrer">
            <img v-if="file.type === 'IMAGE'" :src="file.assetUrl" alt="捐赠影像" />
            <span v-else>查看文件</span>
          </a>
        </div>
      </section>

      <section class="item-detail-block">
        <h3>状态记录</h3>
        <el-timeline>
          <el-timeline-item v-for="record in detail.records || []" :key="record.id" :timestamp="formatDate(record.createTime)">
            {{ statusText(record.oldStatus) }} -> {{ statusText(record.newStatus) }}
            <span class="item-donation-subtext">{{ record.reason || '' }} {{ record.username ? `· ${record.username}` : '' }}</span>
          </el-timeline-item>
        </el-timeline>
        <el-empty v-if="!detail.records?.length" description="暂无状态变更记录" />
      </section>
    </section>
  </el-drawer>

  <el-dialog v-model="statusDialogVisible" title="修改捐赠状态" width="520px" :close-on-click-modal="false">
    <section v-if="statusTarget" class="adoption-review-summary">
      <strong>{{ donationTitle(statusTarget) }}</strong>
      <span>{{ statusText(statusTarget.status) }}</span>
    </section>
    <el-form label-position="top">
      <el-form-item label="新状态">
        <el-select v-model="statusForm.status" placeholder="选择新状态">
          <el-option v-for="item in nextStatuses(statusTarget)" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="原因/备注">
        <el-input v-model="statusForm.reason" type="textarea" :rows="3" placeholder="填写处理说明" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="statusDialogVisible = false">取消</el-button>
      <el-button class="warm-btn" :loading="acting" @click="submitStatus">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { RefreshRight } from '@element-plus/icons-vue'
import { getDonation, getDonations, updateDonationStatus } from '../api/services'
import { useUserStore } from '../stores/user'
import { ROLE, hasRole } from '../utils/roles'
import TableActionColumnHeader from './TableActionColumnHeader.vue'
import TableFilterHeader from './TableFilterHeader.vue'
import { useTableFilters } from '../composables/useTableFilters'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const acting = ref(false)
const actionCollapsed = ref(false)
const rows = ref([])
const total = ref(0)
const detailVisible = ref(false)
const detail = ref(null)
const statusDialogVisible = ref(false)
const statusTarget = ref(null)
const page = reactive({ page: 1, size: 10 })
const statusForm = reactive({ status: '', reason: '' })

const loginRole = computed(() => Number(userStore.profile?.role || 0))
const loginUserId = computed(() => String(userStore.profile?.id || ''))
const isWorker = computed(() => hasRole(loginRole.value, ROLE.WORKER) || hasRole(loginRole.value, ROLE.ADMIN))

const { filters, isActive, applyFilter } = useTableFilters({
  username: { type: 'text' },
  status: { type: 'enum' },
  createTime: { type: 'time' },
})

const filteredRows = computed(() => applyFilter(rows.value || []))

const statusOptions = [
  { label: '已创建', value: 'CREATED' },
  { label: '待处理', value: 'PENDING' },
  { label: '运输中', value: 'TRANSFERRING' },
  { label: '已接收', value: 'RECEIVED' },
  { label: '已入库', value: 'STOCKED' },
  { label: '已拒绝', value: 'REFUSED' },
  { label: '退回中', value: 'BACKING' },
  { label: '已关闭', value: 'CLOSED' },
  { label: '已取消', value: 'CANCELED' },
]
const statusMap = Object.fromEntries(statusOptions.map((item) => [item.value, item.label]))
const transitions = {
  CREATED: ['PENDING', 'TRANSFERRING', 'RECEIVED', 'REFUSED', 'CANCELED'],
  PENDING: ['TRANSFERRING', 'RECEIVED', 'REFUSED', 'CANCELED'],
  TRANSFERRING: ['RECEIVED', 'REFUSED'],
  RECEIVED: ['STOCKED', 'REFUSED'],
  REFUSED: ['BACKING'],
  BACKING: ['CLOSED'],
}

function formatDate(value) {
  if (!value) return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return String(value)
  return date.toLocaleString('zh-CN')
}

function deliveryText(value) {
  return { FACE: '到站面交', EXPRESS: '快递寄送', ADDRESS: '定点取货', OTHER: '其他方式' }[value] || value || ''
}

function statusText(value) {
  return statusMap[value] || value || ''
}

function statusTagType(value) {
  if (['RECEIVED', 'STOCKED', 'CLOSED'].includes(value)) return 'success'
  if (['REFUSED', 'CANCELED'].includes(value)) return 'info'
  if (value === 'BACKING') return 'danger'
  return 'warning'
}

function donationTitle(row) {
  return row?.items?.[0]?.itemName || `捐赠 #${row?.id || ''}`
}

function itemSummary(row) {
  const items = row?.items || []
  return items.map((item) => `${item.itemName || '物资'} ${item.count || ''}${item.itemUnit || ''}`).join('、')
}

function buildQuery() {
  return {
    page: page.page,
    size: page.size,
    user: isWorker.value ? undefined : [loginUserId.value],
  }
}

async function loadRows() {
  if (!isWorker.value && !loginUserId.value) return
  loading.value = true
  try {
    const result = await getDonations(buildQuery())
    rows.value = Array.isArray(result?.records) ? result.records : []
    total.value = Number(result?.total || rows.value.length)
  } catch (error) {
    ElMessage.warning(error?.message || '加载捐赠记录失败')
  } finally {
    loading.value = false
  }
}

function handleRefresh() {
  page.page = 1
  loadRows()
}

function changePage(value) {
  page.page = value
  loadRows()
}

async function openDetail(row) {
  if (!row?.id) return
  detailVisible.value = true
  try {
    detail.value = await getDonation(row.id)
  } catch (error) {
    detail.value = row
    ElMessage.warning(error?.message || '加载捐赠详情失败')
  }
}

function nextStatuses(row) {
  if (!row) return []
  const values = transitions[row.status] || []
  if (!isWorker.value && !(row.status === 'BACKING' && String(row.userId || '') === loginUserId.value)) return []
  return statusOptions.filter((item) => values.includes(item.value))
}

function openStatusDialog(row) {
  const options = nextStatuses(row)
  statusTarget.value = row
  statusForm.status = options[0]?.value || ''
  statusForm.reason = ''
  statusDialogVisible.value = true
}

async function submitStatus() {
  if (!statusTarget.value?.id || !statusForm.status || acting.value) return
  acting.value = true
  try {
    await updateDonationStatus(statusTarget.value.id, {
      status: statusForm.status,
      reason: statusForm.reason.trim() || undefined,
    })
    ElMessage.success('捐赠状态已更新')
    statusDialogVisible.value = false
    await loadRows()
    if (detailVisible.value) await openDetail(statusTarget.value)
  } catch (error) {
    ElMessage.warning(error?.message || '更新捐赠状态失败')
  } finally {
    acting.value = false
  }
}

function goStockIn() {
  if (!detail.value?.id) return
  router.push({ name: 'console-items-stocks', query: { donationId: String(detail.value.id) } })
}

onMounted(() => {
  loadRows()
})
</script>

<style scoped>
.item-donation-filter-row {
  grid-template-columns: minmax(240px, 1fr) auto;
}

.item-donation-subtext {
  display: block;
  margin-top: 3px;
  color: var(--muted);
  font-size: 12px;
  line-height: 1.5;
}

.item-detail-stack {
  display: grid;
  gap: 18px;
}

.item-detail-hero {
  display: flex;
  justify-content: space-between;
  gap: 14px;
  align-items: center;
  border: 1px solid rgba(179, 124, 82, 0.22);
  border-radius: 8px;
  padding: 18px;
  background: #fffaf3;
}

.item-detail-hero span,
.item-detail-list dt {
  color: var(--muted);
  font-size: 12px;
}

.item-detail-hero strong {
  display: block;
  margin-top: 6px;
  color: #5d3927;
  font-size: 22px;
}

.item-detail-block h3 {
  margin: 0 0 12px;
  color: #5d3927;
  font-size: 16px;
}

.item-detail-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.item-detail-list div {
  min-width: 0;
}

.item-detail-list dd {
  margin: 4px 0 0;
  color: #3f2a1f;
  overflow-wrap: anywhere;
}

.item-media-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 10px;
}

.item-media-grid a {
  display: grid;
  place-items: center;
  min-height: 92px;
  border-radius: 8px;
  border: 1px solid rgba(179, 124, 82, 0.24);
  background: #fffaf3;
  color: #8a5a3c;
  overflow: hidden;
}

.item-media-grid img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
</style>
