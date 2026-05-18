<template>
  <el-card class="profile-card pet-admin-card">
    <template #header>
      <div class="profile-card-header">
        <strong>{{ isVaccineMode ? '疫苗接种' : '驱虫管理' }}</strong>
        <div class="profile-actions">
          <el-button class="warm-btn" :icon="RefreshRight" :loading="loading" @click="loadRecords"/>
        </div>
      </div>
    </template>

    <section class="pet-admin-section">
      <el-table :data="displayedRows" v-loading="loading" class="user-admin-table">
        <el-table-column min-width="150">
          <template #header><TableFilterHeader label="宠物" :filter="filters.petName" type="text" :active="isActive('petName')" /></template>
          <template #default="{ row }">
            <button class="pet-admin-name-button" type="button" @click="goPet(row)">{{ row.petName || '未命名' }}</button>
            <span class="medical-preventive-subtext">{{ row.petType || '宠物' }} · {{ row.petSex || '未知' }} · {{ row.petAge ?? 0 }} 月</span>
          </template>
        </el-table-column>
        <el-table-column :label="isVaccineMode ? '疫苗' : '驱虫药'" min-width="160">
          <template #default="{ row }">
            <strong class="medical-preventive-name">{{ isVaccineMode ? row.vaccineName : row.dewormerName }}</strong>
            <span class="medical-preventive-subtext">{{ isVaccineMode ? row.vaccineIll : dewormerTypeText(row.dewormerType) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="次数" width="90">
          <template #default="{ row }">{{ row.times || '' }}</template>
        </el-table-column>
        <el-table-column label="总次数" width="100">
          <template #default="{ row }">{{ isVaccineMode ? row.vaccineTotal : row.dewormerTotal }}</template>
        </el-table-column>
        <el-table-column label="记录医生" min-width="120">
          <template #default="{ row }">{{ row.username || '' }}</template>
        </el-table-column>
        <el-table-column label="记录时间" min-width="160">
          <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
        </el-table-column>
      </el-table>

      <div class="user-admin-pagination">
        <el-pagination layout="prev, pager, next, total" :current-page="page.page" :page-size="page.size" :total="filteredRecords.length" @current-change="(p) => page.page = p" />
      </div>
    </section>
  </el-card>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { RefreshRight } from '@element-plus/icons-vue'
import { getAllDeworms, getAllVaccines } from '../api/services'
import { useTableFilters } from '../composables/useTableFilters'
import TableFilterHeader from './TableFilterHeader.vue'

const props = defineProps({
  type: {
    type: String,
    default: 'vaccine',
  },
})

const router = useRouter()
const loading = ref(false)
const rows = ref([])
const page = reactive({ page: 1, size: 10 })
const isVaccineMode = computed(() => props.type === 'vaccine')

const { filters, isActive, applyFilter } = useTableFilters({
  petName: { type: 'text' },
  recordTime: { type: 'time' },
})

const filteredRecords = computed(() => applyFilter(rows.value || []))

const displayedRows = computed(() => {
  const start = (page.page - 1) * page.size
  return filteredRecords.value.slice(start, start + page.size)
})

function formatDate(value) {
  if (!value) return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return String(value)
  return date.toLocaleString('zh-CN')
}

function dewormerTypeText(value) {
  const map = { INTERNAL: '体内驱虫', EXTERNAL: '体外驱虫', OTHER: '其他驱虫' }
  return map[value] || value || ''
}

function goPet(row) {
  if (row?.petId) router.push(`/pets/${row.petId}`)
}

async function loadRecords() {
  loading.value = true
  try {
    rows.value = isVaccineMode.value ? await getAllVaccines() : await getAllDeworms()
  } catch (error) {
    ElMessage.warning(error?.message || '加载医疗护理记录失败')
  } finally {
    loading.value = false
  }
}

watch(() => props.type, () => {
  page.page = 1
  loadRecords()
})

onMounted(() => {
  loadRecords()
})
</script>

<style scoped>
.medical-preventive-cols-search {
  grid-template-columns: 1fr auto;
}

.medical-preventive-name {
  display: block;
  color: #5d3927;
}

.medical-preventive-subtext {
  display: block;
  margin-top: 3px;
  color: var(--muted);
  font-size: 12px;
  line-height: 1.5;
}
</style>
