<template>
  <el-card class="profile-card pet-admin-card">
    <template #header>
      <div class="profile-card-header">
        <strong>初诊登记</strong>
        <span>记录首次检查结果</span>
      </div>
    </template>
    <section class="pet-admin-section">
      <section class="filter-panel pet-directory-filter-panel">
        <div class="pet-filter-row medical-first-cols-search">
          <el-input
            v-model="firstRegKeyword"
            clearable
            placeholder="按宠物名称搜索"
            @keyup.enter="searchFirstReg"
          />
          <el-date-picker
            v-model="firstRegDateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
          <div class="pet-filter-action">
            <el-button class="warm-btn" :icon="Search" :loading="loadingFirstReg" @click="searchFirstReg">搜索</el-button>
          </div>
        </div>
      </section>
      <el-table :data="firstRegRows" v-loading="loadingFirstReg" class="user-admin-table">
        <el-table-column label="宠物名称" min-width="120" show-overflow-tooltip>
          <template #default="{ row }">
            <button class="table-primary-link" type="button" @click="goFirstRegistrationDetail(row)">{{ row.name || '未命名' }}</button>
          </template>
        </el-table-column>
        <el-table-column label="年龄" width="80">
          <template #default="{ row }">{{ row.age }} 月</template>
        </el-table-column>
        <el-table-column label="类型" width="80">
          <template #default="{ row }">{{ row.type || '—' }}</template>
        </el-table-column>
        <el-table-column label="性别" width="70">
          <template #default="{ row }">{{ row.sex || '—' }}</template>
        </el-table-column>
        <el-table-column label="接诊人" min-width="120">
          <template #default="{ row }">{{ row.username || '—' }}</template>
        </el-table-column>
        <el-table-column label="登记时间" min-width="160">
          <template #default="{ row }">
            {{ row.createTime ? new Date(row.createTime).toLocaleString('zh-CN') : '—' }}
          </template>
        </el-table-column>
      </el-table>
      <div class="user-admin-pagination">
        <el-pagination
          layout="prev, pager, next, total"
          :current-page="firstRegPage.page"
          :page-size="firstRegPage.size"
          :total="firstRegTotal"
          @current-change="changeFirstRegPage"
        />
      </div>
    </section>
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { getFirstVisitRegistrations } from '../api/services'

const router = useRouter()
const loadingFirstReg = ref(false)
const firstRegRows = ref([])
const firstRegTotal = ref(0)
const firstRegKeyword = ref('')
const firstRegDateRange = ref(null)
const firstRegPage = reactive({ page: 1, size: 10 })

async function loadFirstRegistrations() {
  loadingFirstReg.value = true
  try {
    const query = { page: firstRegPage.page, size: firstRegPage.size, sort: 'create_time', order: 'desc' }
    const keyword = firstRegKeyword.value.trim()
    if (keyword) query.name = keyword
    if (firstRegDateRange.value?.[0]) query.date0 = firstRegDateRange.value[0]
    if (firstRegDateRange.value?.[1]) query.date1 = firstRegDateRange.value[1]
    const result = await getFirstVisitRegistrations(query)
    firstRegRows.value = Array.isArray(result?.records) ? result.records : []
    firstRegTotal.value = Number(result?.total || firstRegRows.value.length)
  } catch (error) {
    ElMessage.warning(error?.message || '加载初诊登记失败')
  } finally {
    loadingFirstReg.value = false
  }
}

function searchFirstReg() {
  firstRegPage.page = 1
  loadFirstRegistrations()
}

function changeFirstRegPage(page) {
  firstRegPage.page = page
  loadFirstRegistrations()
}

function goFirstRegistrationDetail(row) {
  if (row?.id) router.push(`/console/medical/first/${row.id}`)
}

onMounted(() => {
  loadFirstRegistrations()
})
</script>

<style scoped>
.medical-first-cols-search {
  grid-template-columns: 1fr 1fr auto;
}
</style>
