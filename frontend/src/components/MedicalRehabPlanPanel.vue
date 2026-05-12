<template>
  <el-card class="profile-card pet-admin-card">
    <template #header>
      <div class="profile-card-header">
        <strong>康复计划</strong>
        <span>跟踪宠物康复计划与执行记录</span>
      </div>
    </template>

    <section class="pet-admin-section">
      <section class="filter-panel pet-directory-filter-panel">
        <div class="pet-filter-row medical-rehab-cols-search">
          <el-input v-model="keyword" class="filter-field-lg" clearable placeholder="按宠物、计划、频率或兽医搜索" @keyup.enter="searchPlans" />
          <el-select v-model="statusFilter" class="filter-field-sm" clearable placeholder="状态">
            <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
          <div class="pet-filter-action">
            <el-button class="warm-btn" :icon="Search" :loading="loading" @click="searchPlans">搜索</el-button>
          </div>
        </div>
      </section>

      <el-table :data="displayedRows" v-loading="loading" class="user-admin-table" row-key="id">
        <el-table-column label="康复计划" min-width="210" show-overflow-tooltip>
          <template #default="{ row }">
            <button class="table-primary-link" type="button" @click="goDetail(row)">{{ row.title || '未命名计划' }}</button>
            <span class="medical-rehab-subtext">{{ row.frequency || '' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="宠物" min-width="150">
          <template #default="{ row }">
            <button class="pet-admin-name-button" type="button" @click="goPet(row)">{{ row.petName || '未命名' }}</button>
            <span class="medical-rehab-subtext">{{ row.petType || '宠物' }} · {{ row.petSex || '未知' }} · {{ row.petAge ?? 0 }} 月</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag size="small" :type="statusTagType(row.status)" effect="plain">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="负责兽医" min-width="120">
          <template #default="{ row }">{{ row.username || '' }}</template>
        </el-table-column>
        <el-table-column label="计划时间" min-width="190">
          <template #default="{ row }">{{ formatDate(row.startTime) }} 至 {{ formatDate(row.endTime) }}</template>
        </el-table-column>
        <el-table-column width="40" class-name="action-col">
          <template #header><TableActionColumnHeader title="操作" :collapsed="actionCollapsed" @toggle="actionCollapsed = !actionCollapsed" /></template>
          <template #default="{ row }">
            <div class="table-action-cell">
              <div class="table-action-panel" :class="{ 'is-collapsed': actionCollapsed }">
                <el-button v-if="canUpdatePlan(row)" text type="primary" @click="goDetail(row, 'status')">修改</el-button>
                <el-button text type="success" @click="goDetail(row, 'record')">记录</el-button>
              </div>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="user-admin-pagination">
        <el-pagination layout="prev, pager, next, total" :current-page="page.page" :page-size="page.size" :total="filteredRows.length" @current-change="changePage" />
      </div>
    </section>
  </el-card>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { getRehabPlans } from '../api/services'
import { useUserStore } from '../stores/user'
import { ROLE, hasRole } from '../utils/roles'
import TableActionColumnHeader from './TableActionColumnHeader.vue'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const rows = ref([])
const keyword = ref('')
const statusFilter = ref('')
const actionCollapsed = ref(false)
const page = reactive({ page: 1, size: 10 })

const statusOptions = [
  { label: '进行中', value: 'ACTIVE' },
  { label: '已完成', value: 'COMPLETED' },
  { label: '已废弃', value: 'DISCARD' },
]

const filteredRows = computed(() => {
  const text = keyword.value.trim().toLowerCase()
  return rows.value.filter((row) => {
    if (statusFilter.value && row.status !== statusFilter.value) return false
    if (!text) return true
    return [row.title, row.content, row.frequency, row.petName, row.petType, row.username]
      .filter(Boolean)
      .some((value) => String(value).toLowerCase().includes(text))
  })
})

const displayedRows = computed(() => {
  const start = (page.page - 1) * page.size
  return filteredRows.value.slice(start, start + page.size)
})

function statusText(value) {
  const item = statusOptions.find((option) => option.value === value)
  return item?.label || value || ''
}

function statusTagType(value) {
  if (value === 'COMPLETED') return 'success'
  if (value === 'DISCARD') return 'info'
  return 'warning'
}

function formatDate(value) {
  if (!value) return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return String(value)
  return date.toLocaleString('zh-CN')
}

function goPet(row) {
  if (row?.petId) router.push(`/pets/${row.petId}`)
}

function canUpdatePlan(row) {
  return hasRole(userStore.profile?.role, ROLE.DOCTOR) && String(row?.doctorId || '') === String(userStore.profile?.id || '')
}

function goDetail(row, action = '') {
  if (!row?.id) return
  router.push({
    path: `/console/medical/rehab/${row.id}`,
    query: action ? { action } : {},
  })
}

function searchPlans() {
  page.page = 1
}

function changePage(value) {
  page.page = value
}

async function loadPlans() {
  loading.value = true
  try {
    const result = await getRehabPlans({ page: 1, size: 500, sort: 'create_time', order: 'desc' })
    rows.value = Array.isArray(result?.records) ? result.records : []
  } catch (error) {
    ElMessage.warning(error?.message || '加载康复计划失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadPlans()
})
</script>

<style scoped>
.medical-rehab-cols-search {
  grid-template-columns: 1fr 180px auto;
}

.medical-rehab-subtext {
  display: block;
  margin-top: 3px;
  color: var(--muted);
  font-size: 12px;
  line-height: 1.5;
}
</style>
