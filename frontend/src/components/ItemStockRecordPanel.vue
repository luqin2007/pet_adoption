<template>
  <el-card class="profile-card pet-admin-card">
    <template #header>
      <div class="profile-card-header">
        <strong>库存管理</strong>
        <div class="profile-actions">
          <el-button class="warm-btn" :icon="RefreshRight" :loading="loading" @click="handleRefresh">刷新</el-button>
        </div>
      </div>
    </template>

    <section class="pet-admin-section">
      <el-table :data="filteredRecords" v-loading="loading" class="user-admin-table">
        <el-table-column label="物资" min-width="180">
          <template #default="{ row }">
            <button class="table-primary-link" type="button" @click="openStock(row)">{{ row.itemName || '物资' }}</button>
            <span class="item-record-subtext">{{ row.categoryName || '' }} · 批次 #{{ row.stockId }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #header>
            <TableFilterHeader label="操作" :filter="filters.action" type="enum" :active="isActive('action')" :options="actionOptions" />
          </template>
          <template #default="{ row }">
            <el-tag :type="actionTagType(row.action)" effect="plain">{{ actionText(row.action) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="数量" width="110">
          <template #default="{ row }">{{ row.count }}</template>
        </el-table-column>
        <el-table-column label="剩余" width="110">
          <template #default="{ row }">{{ row.remainCount }}</template>
        </el-table-column>
        <el-table-column label="来源" width="100">
          <template #default="{ row }">{{ sourceText(row.sourceType) }}</template>
        </el-table-column>
        <el-table-column label="用途" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">{{ row.purpose || '' }}</template>
        </el-table-column>
        <el-table-column label="操作人" min-width="120">
          <template #default="{ row }">{{ row.username || '' }}</template>
        </el-table-column>
        <el-table-column label="时间" min-width="160">
          <template #header>
            <TableFilterHeader label="时间" :filter="filters.createTime" type="time" :active="isActive('createTime')" />
          </template>
          <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
        </el-table-column>
      </el-table>

      <div class="user-admin-pagination">
        <el-pagination layout="prev, pager, next, total" :current-page="page.page" :page-size="page.size" :total="total" @current-change="changePage" />
      </div>
    </section>
  </el-card>

  <el-drawer v-model="stockVisible" title="库存批次" size="520px">
    <section v-if="stockDetail" class="item-record-stock-detail">
      <div><dt>物资</dt><dd>{{ stockDetail.itemName }}</dd></div>
      <div><dt>分类</dt><dd>{{ stockDetail.categoryName }}</dd></div>
      <div><dt>余量</dt><dd>{{ stockDetail.count }}{{ stockDetail.unit || '' }}</dd></div>
      <div><dt>来源</dt><dd>{{ sourceText(stockDetail.sourceType) }}</dd></div>
      <div><dt>有效期</dt><dd>{{ formatDate(stockDetail.expireTime) }}</dd></div>
      <div><dt>创建时间</dt><dd>{{ formatDate(stockDetail.createTime) }}</dd></div>
    </section>
  </el-drawer>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { RefreshRight } from '@element-plus/icons-vue'
import { getStock, getStockRecords } from '../api/services'
import TableFilterHeader from './TableFilterHeader.vue'
import { useTableFilters } from '../composables/useTableFilters'

const route = useRoute()
const loading = ref(false)
const records = ref([])
const total = ref(0)
const stockVisible = ref(false)
const stockDetail = ref(null)
const page = reactive({ page: 1, size: 10 })
const search = reactive({
  action: '',
  sourceType: '',
  stockId: '',
  purpose: '',
})

const { filters, isActive, applyFilter } = useTableFilters({
  createTime: { type: 'time' },
  action: { type: 'enum' },
})

const actionOptions = [
  { value: 'IN', label: '入库' },
  { value: 'OUT', label: '出库' },
  { value: 'DESTROY', label: '销毁' },
]

const filteredRecords = computed(() => applyFilter(records.value || []))

function formatDate(value) {
  if (!value) return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return String(value)
  return date.toLocaleString('zh-CN')
}

function actionText(value) {
  return { IN: '入库', OUT: '出库', DESTROY: '销毁' }[value] || value || ''
}

function actionTagType(value) {
  if (value === 'IN') return 'success'
  if (value === 'DESTROY') return 'danger'
  return 'warning'
}

function sourceText(value) {
  return { DONATION: '捐赠', PURCHASE: '采购' }[value] || value || ''
}

function buildQuery() {
  return {
    page: page.page,
    size: page.size,
    stock: search.stockId ? [search.stockId] : undefined,
    action: search.action ? [search.action] : undefined,
    source: search.sourceType ? [search.sourceType] : undefined,
    purpose: search.purpose.trim() || undefined,
  }
}

async function loadRecords() {
  loading.value = true
  try {
    const result = await getStockRecords(buildQuery())
    records.value = Array.isArray(result?.records) ? result.records : []
    total.value = Number(result?.total || records.value.length)
  } catch (error) {
    ElMessage.warning(error?.message || '加载库存记录失败')
  } finally {
    loading.value = false
  }
}

function handleRefresh() {
  page.page = 1
  loadRecords()
}

function changePage(value) {
  page.page = value
  loadRecords()
}

async function openStock(row) {
  if (!row?.stockId) return
  stockVisible.value = true
  try {
    stockDetail.value = await getStock(row.stockId)
  } catch (error) {
    ElMessage.warning(error?.message || '加载库存批次失败')
  }
}

onMounted(() => {
  if (route.query.stockId) {
    search.stockId = String(route.query.stockId)
  }
  loadRecords()
})
</script>

<style scoped>
.item-record-filter-row {
  grid-template-columns: minmax(120px, 0.8fr) minmax(120px, 0.8fr) minmax(130px, 0.8fr) minmax(160px, 1fr) minmax(220px, 1.2fr) auto;
}

.item-record-subtext {
  display: block;
  margin-top: 3px;
  color: var(--muted);
  font-size: 12px;
}

.item-record-stock-detail {
  display: grid;
  gap: 14px;
}

.item-record-stock-detail div {
  border-bottom: 1px solid rgba(179, 124, 82, 0.18);
  padding-bottom: 12px;
}

.item-record-stock-detail dt {
  color: var(--muted);
  font-size: 12px;
}

.item-record-stock-detail dd {
  margin: 5px 0 0;
  color: #3f2a1f;
}

@media (max-width: 1100px) {
  .item-record-filter-row {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
