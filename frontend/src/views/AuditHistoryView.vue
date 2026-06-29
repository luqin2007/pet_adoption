<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { getPetStatusRecords } from '../api/pets'
import { getRescueTaskRecords } from '../api/rescue'
import { getVolunteerShifts } from '../api/volunteer'
import AuditRecordList from '../components/AuditRecordList.vue'

const route = useRoute()
const router = useRouter()

const type = computed(() => route.query.type || '')
const targetId = computed(() => route.query.id || '')

const loading = ref(false)
const records = ref([])
const page = reactive({ page: 1, size: 20 })
const total = ref(0)

const titleMap = { pet: '宠物审核记录', task: '救助任务审核记录', shift: '排班状态记录' }
const title = computed(() => titleMap[type.value] || '审核记录')

const statusLabelMap = computed(() => {
  const petLabels = { WAITING: '待审核', AGAINST: '审核未通过', FINDING: '查找中', DIED: '已死亡/无法救助', TIMEOUT: '超时放弃', SHELTERED: '已收容', HEALTH: '可领养', ADOPTED: '已领养', HOME: '已回家' }
  const taskLabels = { CREATED: '待审核', APPROVED: '审核通过', PROCESSING: '处理中', COMPLETED: '任务完成', DISCARDED: '已废弃' }
  const shiftLabels = { ASSIGNED: '已分配', CONFIRMED: '已确认', IN_PROGRESS: '执行中', COMPLETED: '已完成', CANCELLED: '已取消', ABSENT: '缺勤' }
  return { pet: petLabels, task: taskLabels, shift: shiftLabels }[type.value] || {}
})

async function loadRecords() {
  if (!type.value || !targetId.value) return
  loading.value = true
  try {
    if (type.value === 'pet') {
      const res = await getPetStatusRecords(targetId.value, page)
      records.value = res?.records || res?.data || []
      total.value = res?.total || records.value.length
    } else if (type.value === 'task') {
      const res = await getRescueTaskRecords(targetId.value)
      const list = Array.isArray(res) ? res : res?.records || res?.data || []
      total.value = list.length
      const start = (page.page - 1) * page.size
      records.value = list.slice(start, start + page.size)
    } else if (type.value === 'shift') {
      const res = await getVolunteerShifts({ id: targetId.value })
      const shift = Array.isArray(res?.records) ? res.records[0] : res
      const list = Array.isArray(shift?.statusRecords) ? shift.statusRecords : []
      total.value = list.length
      const start = (page.page - 1) * page.size
      records.value = list.slice(start, start + page.size)
    }
  } catch { /* ignore */ }
  finally { loading.value = false }
}

function handlePageChange(p) {
  page.page = p
  loadRecords()
}

onMounted(loadRecords)
</script>

<template>
  <div class="audit-history-page">
    <div class="audit-history-header">
      <el-button :icon="ArrowLeft" link @click="router.back()">返回</el-button>
      <h2 class="audit-history-title">{{ title }}</h2>
    </div>
    <div v-if="loading" class="audit-history-loading">
      <el-skeleton :rows="6" animated />
    </div>
    <div v-else-if="!records.length" class="audit-history-empty">
      <el-empty description="暂无审核记录" />
    </div>
    <template v-else>
      <AuditRecordList :records="records" :type="type" :target-id="targetId" :status-labels="statusLabelMap" />
      <div v-if="total > page.size" class="audit-history-pagination">
        <el-pagination
          background
          layout="prev, pager, next"
          :total="total"
          :page-size="page.size"
          :current-page="page.page"
          @current-change="handlePageChange"
        />
      </div>
    </template>
  </div>
</template>

<style scoped>
.audit-history-page {
  max-width: 720px;
  margin: 24px auto;
  padding: 0 16px;
}
.audit-history-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 20px;
}
.audit-history-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  margin: 0;
}
.audit-history-loading,
.audit-history-empty {
  padding: 40px 0;
  text-align: center;
}
.audit-history-pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
