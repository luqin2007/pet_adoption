<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getVolunteerShift } from '../api/volunteer'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const shift = ref(null)

const shiftId = computed(() => String(route.params.id || ''))

const shiftStatusMap = {
  ASSIGNED: '已分配',
  CONFIRMED: '已确认',
  IN_PROGRESS: '执行中',
  COMPLETED: '已完成',
  CANCELLED: '已取消',
  ABSENT: '缺勤',
}

const taskTypeMap = {
  RESCUE: '救助任务',
  DAILY_DUTY: '日常值班',
  FOLLOW_VISIT: '回访任务',
  MEDICAL_SUPPORT: '医疗协助',
  TRANSPORT: '物资运输',
  EVENT: '活动支持',
  OTHER: '其他任务',
}

const statusRecords = computed(() => Array.isArray(shift.value?.statusRecords) ? shift.value.statusRecords : [])

function shiftStatusText(value) {
  return shiftStatusMap[value] || value || ''
}

function statusTagType(value) {
  if (value === 'COMPLETED') return 'success'
  if (value === 'IN_PROGRESS' || value === 'CONFIRMED') return 'primary'
  if (value === 'CANCELLED' || value === 'ABSENT') return 'info'
  return 'warning'
}

function taskTypeText(value) {
  return taskTypeMap[value] || value || ''
}

function formatDate(value) {
  if (!value) return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return String(value)
  return date.toLocaleString('zh-CN')
}

function locationText(row) {
  return [row?.province, row?.city, row?.district, row?.serviceAddress].filter(Boolean).join(' · ')
}

function goBack() {
  router.push({ name: 'console-volunteer-activities' })
}

function goSource() {
  if (shift.value?.taskType === 'RESCUE' && shift.value?.taskSourceId) {
    router.push(`/tasks/${shift.value.taskSourceId}`)
  }
}

async function loadShift() {
  if (!shiftId.value) return
  loading.value = true
  try {
    shift.value = await getVolunteerShift(shiftId.value)
  } catch (error) {
    shift.value = null
    ElMessage.warning(error?.message || '加载排班详情失败')
  } finally {
    loading.value = false
  }
}

watch(shiftId, loadShift, { immediate: true })
</script>

<template>
  <el-card class="profile-card volunteer-shift-detail-card" v-loading="loading">
    <template #header>
      <div class="profile-card-header">
        <strong>排班详情</strong>
      </div>
    </template>

    <section v-if="shift" class="console-detail-shell volunteer-shift-detail-panel">
      <div class="console-detail-toolbar">
        <el-button class="soft-btn" :icon="ArrowLeft" @click="goBack">返回排班</el-button>
        <el-tag :type="statusTagType(shift.status)" effect="plain">{{ shiftStatusText(shift.status) }}</el-tag>
      </div>

      <div class="console-detail-summary">
        <div>
          <span>活动</span>
          <button
            v-if="shift.taskType === 'RESCUE' && shift.taskSourceId"
            class="table-primary-link volunteer-shift-title"
            type="button"
            @click="goSource"
          >
            {{ shift.title || '未命名活动' }}
          </button>
          <strong v-else>{{ shift.title || '未命名活动' }}</strong>
          <p>{{ taskTypeText(shift.taskType) }}</p>
        </div>
        <div>
          <span>志愿者</span>
          <strong>{{ shift.volunteerName || '待指派' }}</strong>
          <p>{{ shift.assignerName ? `排班人：${shift.assignerName}` : '' }}</p>
        </div>
      </div>

      <div class="console-detail-grid">
        <section class="console-detail-section">
          <h3>排班信息</h3>
          <dl class="console-detail-list">
            <div><dt>排班编号</dt><dd>#{{ shift.id }}</dd></div>
            <div><dt>排班状态</dt><dd>{{ shiftStatusText(shift.status) }}</dd></div>
            <div><dt>开始时间</dt><dd>{{ formatDate(shift.startTime) }}</dd></div>
            <div><dt>结束时间</dt><dd>{{ formatDate(shift.endTime) }}</dd></div>
            <div><dt>创建时间</dt><dd>{{ formatDate(shift.createTime) }}</dd></div>
            <div><dt>更新时间</dt><dd>{{ formatDate(shift.updateTime) }}</dd></div>
          </dl>
        </section>

        <section class="console-detail-section">
          <h3>活动内容</h3>
          <dl class="console-detail-list">
            <div><dt>任务类型</dt><dd>{{ taskTypeText(shift.taskType) }}</dd></div>
            <div><dt>来源编号</dt><dd>{{ shift.taskSourceId ? `#${shift.taskSourceId}` : '' }}</dd></div>
            <div><dt>服务地点</dt><dd>{{ locationText(shift) }}</dd></div>
            <div><dt>备注</dt><dd>{{ shift.remark || '' }}</dd></div>
          </dl>
        </section>

        <section v-if="shift.content" class="console-detail-section console-detail-section-wide">
          <h3>任务说明</h3>
          <p class="volunteer-shift-note">{{ shift.content }}</p>
        </section>

        <section class="console-detail-section console-detail-section-wide">
          <h3>状态记录</h3>
          <el-table :data="statusRecords" class="user-admin-table">
            <el-table-column label="原状态" width="120">
              <template #default="{ row }">{{ shiftStatusText(row.statusFrom) }}</template>
            </el-table-column>
            <el-table-column label="新状态" width="120">
              <template #default="{ row }">{{ shiftStatusText(row.statusTo) }}</template>
            </el-table-column>
            <el-table-column label="原因" min-width="220">
              <template #default="{ row }">{{ row.comment || '' }}</template>
            </el-table-column>
            <el-table-column label="时间" min-width="170">
              <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
            </el-table-column>
          </el-table>
        </section>
      </div>
    </section>

    <section v-else-if="!loading" class="pet-admin-section">
      <el-empty description="未找到排班">
        <el-button class="soft-btn" :icon="ArrowLeft" @click="goBack">返回排班</el-button>
      </el-empty>
    </section>
  </el-card>
</template>

<style scoped>
.volunteer-shift-detail-panel {
  display: grid;
  gap: 16px;
}

.volunteer-shift-title {
  padding: 0;
  border: 0;
  background: transparent;
  font: inherit;
  text-align: left;
  cursor: pointer;
}

.volunteer-shift-note {
  margin: 0;
  color: var(--muted);
  line-height: 1.8;
  white-space: pre-wrap;
}
</style>
