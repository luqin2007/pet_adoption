<template>
  <el-card class="profile-card pet-admin-card">
    <template #header>
      <div class="profile-card-header">
        <strong>康复计划</strong>
        <div class="profile-actions">
          <el-button-group class="console-btn-group">
            <el-button class="warm-btn" :icon="Plus" @click="petPickerVisible = true"/>
            <el-button class="warm-btn" :icon="RefreshRight" :loading="loading" @click="loadPlans"/>
          </el-button-group>
        </div>
      </div>
    </template>

    <section class="pet-admin-section">
      <el-table :data="displayedRows" v-loading="loading" class="user-admin-table" row-key="id">
        <el-table-column label="康复计划" min-width="210" show-overflow-tooltip>
          <template #header><TableFilterHeader label="康复计划" :filter="filters.title" type="text" :active="isActive('title')" /></template>
          <template #default="{ row }">
            <button class="table-primary-link" type="button" @click="goDetail(row)">{{ row.title || '未命名计划' }}</button>
            <span class="medical-rehab-subtext">{{ row.frequency || '' }}</span>
          </template>
        </el-table-column>
        <el-table-column min-width="150">
          <template #header><TableFilterHeader label="宠物" :filter="filters.petName" type="text" :active="isActive('petName')" /></template>
          <template #default="{ row }">
            <button class="pet-admin-name-button" type="button" @click="goPet(row)">{{ row.petName || '未命名' }}</button>
            <span class="medical-rehab-subtext">{{ row.petType || '宠物' }} · {{ row.petSex || '未知' }} · {{ row.petAge ?? 0 }} 月</span>
          </template>
        </el-table-column>
        <el-table-column width="110">
          <template #header><TableFilterHeader label="状态" :filter="filters.status" type="enum" :active="isActive('status')" :options="statusOptions" /></template>
          <template #default="{ row }">
            <el-tag size="small" :type="statusTagType(row.status)" effect="plain">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="兽医" min-width="120">
          <template #header><TableFilterHeader label="兽医" :filter="filters.username" type="text" :active="isActive('username')" /></template>
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
        <el-pagination layout="prev, pager, next, total" :current-page="page.page" :page-size="page.size" :total="filteredPlans.length" @current-change="(p) => page.page = p" />
      </div>
    </section>
  </el-card>

  <PetPickerDialog v-model:visible="petPickerVisible" @select="onPetSelected" />
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus, RefreshRight } from '@element-plus/icons-vue'
import { getRehabPlans } from '../api/services'
import { formatDate } from '../utils/format'
import { useUserStore } from '../stores/user'
import { ROLE, hasRole } from '../utils/roles'
import { useTableFilters } from '../composables/useTableFilters'
import TableActionColumnHeader from './TableActionColumnHeader.vue'
import TableFilterHeader from './TableFilterHeader.vue'
import PetPickerDialog from './PetPickerDialog.vue'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const rows = ref([])
const actionCollapsed = ref(false)
const page = reactive({ page: 1, size: 10 })
const petPickerVisible = ref(false)

const statusOptions = [
  { label: '进行中', value: 'ACTIVE' },
  { label: '已完成', value: 'COMPLETED' },
  { label: '已废弃', value: 'DISCARD' },
]

const { filters, isActive, applyFilter } = useTableFilters({
  petName: { type: 'text' },
  title: { type: 'text' },
  username: { type: 'text' },
  status: { type: 'enum' },
  createTime: { type: 'time' },
})

const filteredPlans = computed(() => applyFilter(rows.value || []))

const displayedRows = computed(() => {
  const start = (page.page - 1) * page.size
  return filteredPlans.value.slice(start, start + page.size)
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

function onPetSelected(pet) {
  router.push({
    path: '/console/medical/rehab/new',
    query: {
      pet: pet.id,
      name: pet.name || '',
      age: pet.age ?? 0,
    },
  })
}

onMounted(() => {
  loadPlans()
})
</script>

<style scoped>
.medical-rehab-subtext {
  display: block;
  margin-top: 3px;
  color: var(--muted);
  font-size: 12px;
  line-height: 1.5;
}
</style>
