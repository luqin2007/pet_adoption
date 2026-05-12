<template>
  <el-card class="profile-card pet-admin-card">
    <template #header>
      <div class="profile-card-header">
        <strong>库存管理</strong>
        <span>跟踪和查找物资入库、出库、销毁记录</span>
      </div>
    </template>

    <section class="pet-admin-section">
      <section class="filter-panel pet-directory-filter-panel">
        <div class="pet-filter-row item-record-filter-row">
          <el-select v-model="search.action" class="filter-field-sm" clearable placeholder="操作类型">
            <el-option label="入库" value="IN" />
            <el-option label="出库" value="OUT" />
            <el-option label="销毁" value="DESTROY" />
          </el-select>
          <el-select v-model="search.sourceType" class="filter-field-sm" clearable placeholder="来源">
            <el-option label="捐赠" value="DONATION" />
            <el-option label="采购" value="PURCHASE" />
          </el-select>
          <el-input v-model="search.stockId" class="filter-field-sm" clearable placeholder="库存批次 ID" @input="normalizeStockId" />
          <el-input v-model="search.purpose" class="filter-field-xl" clearable placeholder="用途/说明" @keyup.enter="searchRows" />
          <el-date-picker
            v-model="search.timeRange"
            type="daterange"
            value-format="YYYY-MM-DD HH:mm:ss"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            class="filter-field-lg"
          />
          <div class="pet-filter-action">
            <el-button class="warm-btn" :icon="Search" :loading="loading" @click="searchRows">搜索</el-button>
          </div>
        </div>
      </section>

      <el-table :data="rows" v-loading="loading" class="user-admin-table">
        <el-table-column label="物资" min-width="180">
          <template #default="{ row }">
            <button class="table-primary-link" type="button" @click="openStock(row)">{{ row.itemName || '物资' }}</button>
            <span class="item-record-subtext">{{ row.categoryName || '' }} · 批次 #{{ row.stockId }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
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
import { onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { getStock, getStockRecords } from '../api/services'

const route = useRoute()
const loading = ref(false)
const rows = ref([])
const total = ref(0)
const stockVisible = ref(false)
const stockDetail = ref(null)
const page = reactive({ page: 1, size: 10 })
const search = reactive({
  action: '',
  sourceType: '',
  stockId: '',
  purpose: '',
  timeRange: [],
})

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

function normalizeStockId(value) {
  search.stockId = String(value || '').replace(/\D/g, '')
}

function buildQuery() {
  return {
    page: page.page,
    size: page.size,
    stock: search.stockId ? [search.stockId] : undefined,
    action: search.action ? [search.action] : undefined,
    source: search.sourceType ? [search.sourceType] : undefined,
    purpose: search.purpose.trim() || undefined,
    time0: search.timeRange?.[0],
    time1: search.timeRange?.[1],
  }
}

async function loadRows() {
  loading.value = true
  try {
    const result = await getStockRecords(buildQuery())
    rows.value = Array.isArray(result?.records) ? result.records : []
    total.value = Number(result?.total || rows.value.length)
  } catch (error) {
    ElMessage.warning(error?.message || '加载库存记录失败')
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
  loadRows()
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
