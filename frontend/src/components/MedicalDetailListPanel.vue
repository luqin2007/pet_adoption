<template>
  <el-card class="profile-card pet-admin-card">
    <template #header>
      <div class="profile-card-header">
        <strong>病例</strong>
        <span>{{ petName ? `${petName} 的病例` : '所有病例记录' }}</span>
      </div>
    </template>
    <section class="pet-admin-section">
      <section class="filter-panel pet-directory-filter-panel">
        <div class="pet-filter-row medical-first-cols-search">
          <el-input v-model="searchKeyword" clearable placeholder="按摘要搜索" @keyup.enter="searchRecords" />
          <div class="pet-filter-action">
            <el-button class="warm-btn" :icon="Search" :loading="loading" @click="searchRecords">搜索</el-button>
          </div>
        </div>
      </section>
      <el-table :data="displayedRecords" v-loading="loading" class="user-admin-table">
        <el-table-column label="摘要" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            <button class="table-primary-link" type="button" @click="goDetail(row)">{{ row.summary || '未命名病例' }}</button>
          </template>
        </el-table-column>
        <el-table-column label="宠物" width="120">
          <template #default="{ row }">{{ row.name || '—' }}</template>
        </el-table-column>
        <el-table-column label="医生" width="100">
          <template #default="{ row }">{{ row.username || '—' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="row.isCompleted ? 'success' : 'warning'" effect="plain">
              {{ row.isCompleted ? '已完成' : '进行中' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" min-width="160">
          <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
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
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { getMedicalRecords, getMedicalDetails } from '../api/services'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const allRecords = ref([])
const total = ref(0)
const searchKeyword = ref('')
const petId = computed(() => route.query.pet || '')
const petName = computed(() => route.query.name || '')
const page = reactive({ page: 1, size: 10 })

const displayedRecords = computed(() => {
  let list = allRecords.value
  if (searchKeyword.value.trim()) {
    const kw = searchKeyword.value.trim().toLowerCase()
    list = list.filter((r) => (r.summary || '').toLowerCase().includes(kw))
  }
  return list
})

function formatDate(value) {
  if (!value) return '—'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return String(value)
  return date.toLocaleString('zh-CN')
}

function goDetail(row) {
  if (row?.id) router.push(`/medical/detail/${row.id}`)
}

function searchRecords() {
  // filtering is done client-side in displayedRecords
}

function changePage(p) {
  page.page = p
}

async function loadRecords() {
  loading.value = true
  try {
    const recordQuery = { sort: 'create_time', order: 'desc', size: 200 }
    if (petId.value) recordQuery.pet = petId.value
    const recordRes = await getMedicalRecords(recordQuery)
    const recordList = recordRes?.records || []
    const recordIds = recordList.map((r) => r.id).filter(Boolean)

    const detailList = []
    if (recordIds.length) {
      const detailRes = await getMedicalDetails({ record: recordIds, size: 200 })
      const details = detailRes?.records || []
      for (const d of details) {
        const rec = recordList.find((r) => String(r.id) === String(d.recordId))
        detailList.push({ ...d, recordStatus: rec?.status, recordType: rec?.type, petName: rec?.petName })
      }
    }
    allRecords.value = detailList
    total.value = detailList.length
  } catch (error) {
    ElMessage.warning(error?.message || '加载病例失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => loadRecords())
</script>