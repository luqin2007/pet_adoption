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
        <el-table-column width="40" class-name="action-col">
          <template #header><TableActionColumnHeader title="操作" :collapsed="actionCollapsed" @toggle="actionCollapsed = !actionCollapsed" /></template>
          <template #default="{ row }">
            <div class="table-action-cell">
              <div class="table-action-panel" :class="{ 'is-collapsed': actionCollapsed }">
                <el-button v-if="canManageMedical" text type="primary" @click="goTreatment(row)">就诊</el-button>
                <el-button v-if="isDoctor" text type="success" @click="goCaseList(row)">病历</el-button>
              </div>
            </div>
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

  <MedicalRecordCreateDialog
    v-model="createDialogVisible"
    :pet-id="createDialogPetId"
    :pet-name="createDialogPetName"
    :pet-age="createDialogPetAge"
    @created="onRecordCreated"
  />
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { getFirstVisitRegistrations, getMedicalRecords, getMedicalDetails } from '../api/services'
import { useConsoleGuards } from '../composables/useConsoleGuards'
import { ROLE, hasRole } from '../utils/roles'
import TableActionColumnHeader from './TableActionColumnHeader.vue'
import MedicalRecordCreateDialog from './MedicalRecordCreateDialog.vue'

const router = useRouter()
const { canManageMedical, loginRole } = useConsoleGuards()

const isDoctor = computed(() => hasRole(loginRole.value, ROLE.DOCTOR))

const actionCollapsed = ref(false)
const createDialogVisible = ref(false)
const createDialogPetId = ref('')
const createDialogPetName = ref('')
const createDialogPetAge = ref(null)

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

function openCreateDialog(row) {
  createDialogPetId.value = row.petId || ''
  createDialogPetName.value = row.name || ''
  createDialogPetAge.value = row.age ?? null
  createDialogVisible.value = true
}

function onRecordCreated() {
  loadFirstRegistrations()
}

function goFirstRegistrationDetail(row) {
  if (row?.id) router.push(`/medical/first/${row.id}`)
}

function goCaseList(row) {
  if (row?.petId) router.push(`/console/medical/detail-list?pet=${row.petId}&name=${encodeURIComponent(row.name || '')}`)
}

async function goTreatment(row) {
  const petId = row.petId
  if (!petId) {
    ElMessage.warning('无法找到宠物信息')
    return
  }

  try {
    const res = await getMedicalRecords({ pet: petId, page: 1, size: 1, sort: 'create_time', order: 'desc' })
    const latestRecord = res?.records?.[0]

    if (!latestRecord) {
      openCreateDialog(row)
      return
    }

    const { status, id: recordId } = latestRecord

    if (status === 'COMPLETED' || status === 'CANCELED') {
      openCreateDialog(row)
      return
    }

    if (status === 'WAITING' && isDoctor.value) {
      router.push(`/medical/detail/new?recordId=${recordId}`)
      return
    }

    if (status === 'PROCESSING' && isDoctor.value) {
      const detailRes = await getMedicalDetails({ record: [recordId], page: 1, size: 1 })
      const detail = detailRes?.records?.[0]
      if (detail?.id) {
        router.push(`/medical/detail/${detail.id}`)
      } else {
        router.push(`/medical/detail/new?recordId=${recordId}`)
      }
      return
    }

    const detailRes = await getMedicalDetails({ record: [recordId], page: 1, size: 1 })
    const detail = detailRes?.records?.[0]
    if (detail?.id) {
      router.push(`/medical/detail/${detail.id}`)
    } else {
      ElMessage.info('该就诊记录暂无病历详情')
    }
  } catch (error) {
    ElMessage.warning(error?.message || '获取就诊信息失败')
  }
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