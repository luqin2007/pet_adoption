<template>
  <el-card class="profile-card pet-admin-card">
    <template #header>
      <div class="profile-card-header">
        <strong>健康评估</strong>
        <div class="profile-actions">
          <el-button class="warm-btn" :icon="RefreshRight" :loading="loading" @click="loadAssessments"/>
        </div>
      </div>
    </template>

    <section class="pet-admin-section">
      <el-table :data="displayedRows" v-loading="loading" class="user-admin-table" row-key="id">
        <el-table-column min-width="160">
          <template #header><TableFilterHeader label="宠物" :filter="filters.petName" type="text" :active="isActive('petName')" /></template>
          <template #default="{ row }">
            <button class="pet-admin-name-button" type="button" @click="goDetail(row)">{{ row.petName || '未命名' }}</button>
            <span class="medical-health-subtext">{{ row.petType || '宠物' }} · {{ row.petSex || '未知' }} · {{ row.petAge ?? 0 }} 月</span>
          </template>
        </el-table-column>
        <el-table-column label="评分" min-width="180">
          <template #default="{ row }">
            <div class="medical-health-score-line">
              <span>体况 {{ row.scoreBcs ?? 0 }}</span>
              <span>精神 {{ row.scoreMental ?? 0 }}</span>
              <span>食欲 {{ row.scoreAppetite ?? 0 }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="体重" width="100">
          <template #default="{ row }">{{ row.weight ?? '' }} kg</template>
        </el-table-column>
        <el-table-column label="评估摘要" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">{{ row.summary || '' }}</template>
        </el-table-column>
        <el-table-column label="评估人" min-width="120">
          <template #default="{ row }">{{ row.username || '' }}</template>
        </el-table-column>
        <el-table-column label="评估时间" min-width="160">
          <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
        </el-table-column>
        <el-table-column width="40" class-name="action-col">
          <template #header><TableActionColumnHeader title="操作" :collapsed="actionCollapsed" @toggle="actionCollapsed = !actionCollapsed" /></template>
          <template #default="{ row }">
            <div class="table-action-cell">
              <div class="table-action-panel" :class="{ 'is-collapsed': actionCollapsed }">
                <el-button text type="primary" @click="goDetail(row)">查看</el-button>
              </div>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="user-admin-pagination">
        <el-pagination layout="prev, pager, next, total" :current-page="page.page" :page-size="page.size" :total="filteredAssessments.length" @current-change="(p) => page.page = p" />
      </div>
    </section>
  </el-card>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { RefreshRight } from '@element-plus/icons-vue'
import { getHealthAssessments } from '../api/services'
import { useTableFilters } from '../composables/useTableFilters'
import TableActionColumnHeader from './TableActionColumnHeader.vue'
import TableFilterHeader from './TableFilterHeader.vue'

const router = useRouter()
const loading = ref(false)
const rows = ref([])
const actionCollapsed = ref(false)
const page = reactive({ page: 1, size: 10 })

const { filters, isActive, applyFilter } = useTableFilters({
  petName: { type: 'text' },
  assessmentTime: { type: 'time' },
})

const filteredAssessments = computed(() => applyFilter(rows.value || []))

const displayedRows = computed(() => {
  const start = (page.page - 1) * page.size
  return filteredAssessments.value.slice(start, start + page.size)
})

function formatDate(value) {
  if (!value) return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return String(value)
  return date.toLocaleString('zh-CN')
}

function goDetail(row) {
  if (row?.id) router.push(`/console/medical/health/${row.id}`)
}

async function loadAssessments() {
  loading.value = true
  try {
    const result = await getHealthAssessments({ page: 1, size: 500, sort: 'create_time', order: 'desc' })
    rows.value = Array.isArray(result?.records) ? result.records : []
  } catch (error) {
    ElMessage.warning(error?.message || '加载健康评估失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadAssessments()
})
</script>

<style scoped>
.medical-health-cols-search {
  grid-template-columns: 1fr auto;
}

.medical-health-subtext {
  display: block;
  margin-top: 3px;
  color: var(--muted);
  font-size: 12px;
  line-height: 1.5;
}

.medical-health-score-line {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.medical-health-score-line span {
  border: 1px solid rgba(231, 122, 59, 0.2);
  border-radius: 999px;
  background: rgba(255, 253, 249, 0.9);
  color: var(--primary-strong);
  padding: 3px 8px;
  font-size: 12px;
}
</style>
